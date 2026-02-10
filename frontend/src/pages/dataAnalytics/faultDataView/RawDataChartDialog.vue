<template>
  <q-dialog v-model="isVisible" @hide="onDialogHide">
    <q-card class="card column">
      <q-card-section class="bg-secondary text-white col-1">
        <div class="text-h6">{{ dialogTitle }}</div>
      </q-card-section>
      <q-card-section class="full-width col-11">
        <div class="row">
          <div class="col-7 chart-div">
            <ECharts ref="chart" :option="options" resizable autoresize @zr:click="onLineChartClick" />
          </div>
          <div class="col-5 q-pl-md">
            <q-card flat class="full-height column colGroup">
              <!-- <q-img v-if="image" :src="image" class="q-pa-md" /> -->
              <div class="col column">
                <div class="titleWrap q-pa-md">
                  <i class="iconImg robot"></i>
                  <span>{{ $t('로봇 촬영 사진') }}</span>
                </div>
                <!-- <q-chip color="primary" text-color="white" icon="image" class="chip-title" outline>
                  {{ $t('로봇 촬영 사진') }}
                </q-chip> -->
                <q-img :src="`/img/ext/photo/${imageInfo.imageValue}`" class="col" fit="contain">
                  <div class="absolute-bottom text-subtitle1 text-center img-caption">{{ imageTraceAt }}</div>
                </q-img>
              </div>
              <div class="col column">
                <div class="titleWrap q-pa-md">
                  <i class="iconImg map-pin"></i>
                  <span>{{ $t('위치') }}</span>
                </div>
                <!-- <q-chip color="primary" text-color="white" icon="location_on" class="chip-title" outline>
                  {{ $t('위치') }}
                </q-chip> -->
                <LeafletMap class="q-py-md col" ref="map" :zoom="zoom" :center="imageLocation">
                  <l-marker :lat-lng="imageLocation" />
                </LeafletMap>
              </div>
            </q-card>
          </div>
        </div>
      </q-card-section>
    </q-card>
  </q-dialog>
</template>

<script setup>
import { date, useDialogPluginComponent } from 'quasar'
import { computed, ref, shallowRef, watch } from 'vue'
import ECharts from 'vue-echarts'
import { LMarker } from '@vue-leaflet/vue-leaflet'
import ParameterDataService from 'src/services/parameterData/ParameterDataService'
import { formatDateTime } from 'src/common/utils'
import { pt } from 'src/plugins/i18n'
import { controlLimitColor, specLimitColor } from 'src/common/constant/chart'
import LeafletMap from 'components/map/LeafletMap.vue'
import { use } from 'echarts/core'
import { LineChart } from 'echarts/charts'
import { CanvasRenderer } from 'echarts/renderers'
import { DataZoomComponent, TooltipComponent } from 'echarts/components'

use([LineChart, DataZoomComponent, TooltipComponent, CanvasRenderer])

const props = defineProps({
  selectedFaultRow: {
    type: Object,
    required: false,
  },
  selectedTool: {
    type: Object,
    required: false,
  },
})

const { onDialogHide } = useDialogPluginComponent()
const isVisible = ref(false)
const options = ref()
const dialogTitle = ref()
const imageInfo = ref()
const map = shallowRef(null)
const zoom = ref(15)
const chart = ref(null)
const imageTraceAt = computed(() => {
  return imageInfo.value?.traceAt ? formatDateTime(new Date(imageInfo.value.traceAt)) : ''
})

const imageLocation = computed(() => {
  return imageInfo.value?.latitudeValue ? [imageInfo.value.latitudeValue, imageInfo.value.longitudeValue] : undefined
})

const formatToFiveDecimals = (value) => {
  return Number(value.toFixed(5))
}

defineEmits([...useDialogPluginComponent.emits])

const createOption = (parameterName, series, specMarkLines, minYValue, maxYValue, markPoints = []) => {
  if (series == null) {
    return null
  }

  return {
    legend: {
      orient: 'horizontal',
      icon: 'rect',
      show: false,
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        animation: false,
        label: {
          backgroundColor: '#ccc',
          borderColor: '#aaa',
          borderWidth: 1,
          shadowBlur: 0,
          shadowOffsetX: 0,
          shadowOffsetY: 0,
          color: '#222',
        },
      },
      formatter: function (params) {
        const param = params[0]
        return `${param.name}<b>${param.seriesName}</b> : ${param.value[1]}`
      },
    },
    dataZoom: [{ type: 'inside' }],
    xAxis: {
      type: 'time',
      axisLabel: {
        rotate: 15,
        formatter: function () {
          return '{yyyy}-{MM}-{dd} {hh}:{mm}'
        },
      },
    },
    yAxis: {
      type: 'value',
      min: formatToFiveDecimals(minYValue - (maxYValue - minYValue) * 0.05),
      max: formatToFiveDecimals(maxYValue + (maxYValue - minYValue) * 0.05),
    },
    series: [
      {
        type: 'line',
        showSymbol: false,
        name: pt(parameterName),
        data: series,
        markLine: {
          data: specMarkLines,
        },
        markArea: {
          data: specMarkLines,
          label: {
            show: true,
            position: 'right',
          },
        },
        markPoint: {
          data: markPoints,
        },
      },
    ],
  }
}

const createMarkLine = (specLineData) => {
  const { startX, endX, y, type } = specLineData
  const label = `${type.toUpperCase()} (${y})`
  const color = type === 'usl' || type === 'lsl' ? specLimitColor : controlLimitColor
  return [
    {
      coord: [startX, y],
      symbol: 'none',
      lineStyle: {
        color: color,
        width: 2,
        type: 'solid',
      },
      itemStyle: {
        borderColor: color,
        color: 'rgba(255, 0, 0, 0.2)',
        borderWidth: 1,
      },
    },
    {
      name: label,
      coord: [endX, y],
      symbol: 'none',
      label: {
        show: true,
      },
    },
  ]
}

const onLineChartClick = async (params) => {
  if (!chart.value) return
  const pointInPixel = [params.offsetX, params.offsetY]
  const xPixel = pointInPixel[0]

  const option = chart.value.getOption()
  const xAxisData = []
  const data = []

  option.series[0].data.forEach((item) => {
    xAxisData.push(item[0])
    data.push(item[1])
  })

  let closestIndex = null
  let minDistance = Number.MAX_VALUE
  xAxisData.forEach((x, index) => {
    const xPos = chart.value.convertToPixel({ xAxisIndex: 0 }, x)

    const distance = Math.abs(xPos - xPixel)

    if (distance < minDistance) {
      minDistance = distance
      closestIndex = index
    }
  })

  if (closestIndex !== null) {
    const xValue = xAxisData[closestIndex]
    const yValue = data[closestIndex]

    const markPointData = [
      {
        coord: [xValue, yValue],
        value: yValue,
        itemStyle: {
          color: '#aaa',
        },
        label: {
          show: true,
          color: 'white',
          fontSize: 10,
          fontWeight: 'bold',
        },
      },
    ]

    options.value.series[0].markPoint.data = markPointData

    const imageData = await ParameterDataService.getImageParameterData(props.selectedTool.id, xValue)
    if (imageData) {
      imageInfo.value = imageData
    } else {
      imageInfo.value = null
    }
  }
}

const createSpecLineData = (type, startX, endX, y) => {
  return {
    type,
    startX,
    endX,
    y,
  }
}

const parseSpecMarkLines = (rawDatas) => {
  const specLineData = []
  let tempLineData = {}

  const parseSpec = (type, rawData) => {
    if (!rawData[type]) {
      return
    }

    const temp = tempLineData[type]
    if (temp) {
      if (temp.y === rawData[type]) {
        temp.endX = rawData.traceAt
      } else {
        specLineData.push(createSpecLineData(type, temp.startX, temp.endX, temp.y))
        tempLineData[type] = createSpecLineData(type, rawData.traceAt, rawData.traceAt, rawData[type])
      }
    } else {
      tempLineData[type] = createSpecLineData(type, rawData.traceAt, rawData.traceAt, rawData[type])
    }
  }

  rawDatas.forEach((rawData) => {
    parseSpec('usl', rawData)
    parseSpec('ucl', rawData)
    parseSpec('lcl', rawData)
    parseSpec('lsl', rawData)
  })

  Object.keys(tempLineData).forEach((type) => {
    const temp = tempLineData[type]
    specLineData.push(createSpecLineData(type, temp.startX, temp.endX, temp.y))
  })

  return specLineData.map(createMarkLine)
}

watch(
  () => props.selectedFaultRow,
  async (faultRow) => {
    if (faultRow != null) {
      const imageData = await ParameterDataService.getImageParameterData(
        props.selectedTool.id,
        props.selectedFaultRow.faultAt,
      )
      if (imageData) {
        imageInfo.value = imageData
      } else {
        imageInfo.value = null
      }

      const { parameterId, faultAt } = faultRow
      const base = new Date(faultAt)
      const from = date.subtractFromDate(base, { minutes: 30 })
      const to = date.addToDate(base, { minutes: 30 })

      const parameterTrendData = await ParameterDataService.getParameterDataTrend(parameterId, from, to)
      const { rawDatas, parameterName } = parameterTrendData

      const series = rawDatas.map((rawData) => [new Date(rawData.traceAt), rawData.value])
      const specMarkLines = parseSpecMarkLines(rawDatas)
      const allYValues = [
        ...series.map((item) => item[1]),
        ...specMarkLines.flatMap((markLine) => markLine.map((line) => line.coord[1])),
      ]
      const minYValue = Math.min(...allYValues)
      const maxYValue = Math.max(...allYValues)

      const initialMarkPoints =
        series.length > 0
          ? [
              {
                coord: [props.selectedFaultRow.faultAt, props.selectedFaultRow.paramValue],
                value: props.selectedFaultRow.paramValue,
                itemStyle: {
                  color: '#aaa',
                },
                label: {
                  show: true,
                  color: 'white',
                  fontSize: 10,
                  fontWeight: 'bold',
                },
              },
            ]
          : []

      dialogTitle.value = `${pt(parameterName)} ( ${formatDateTime(from)} ~ ${formatDateTime(to)} )`
      options.value = createOption(parameterName, series, specMarkLines, minYValue, maxYValue, initialMarkPoints)
      isVisible.value = true
    }
  },
)
</script>

<style lang="scss" scoped>
.card {
  width: 90vw;
  max-width: 90vw;
  /* height: 90vh; */
  max-height: 90vh;
}

.chart-div {
  height: 700px;
}

.img-caption {
  padding: 5px 0px;
}

.colGroup {
  gap: 10px;
  & > div.col {
    border-radius: 4px;
    overflow: hidden;
  }
}
.titleWrap {
  display: flex;
  align-items: center;
  background-color: var(--mainColor);
  i.iconImg {
    display: inline-block;
    width: 18px;
    height: 18px;
    margin-right: 10px;
    background-size: 18px !important;
    &.robot {
      background: url(../../../../public/img/widgets/ecotwin-icon/icon-image.svg) 50% 50% no-repeat;
    }
    &.map-pin {
      background: url(../../../../public/img/widgets/ecotwin-icon/icon-map-pin.svg) 50% 50% no-repeat;
    }
  }
  span {
    font-size: 14px;
    font-weight: bold;
    color: #fff;
  }
  & + .q-img {
    background-color: #ddd;
    .img-caption {
      border-top: 1px solid var(--textColor);
      background-color: rgba(1, 29, 73, 0.4);
    }
  }
}
</style>
