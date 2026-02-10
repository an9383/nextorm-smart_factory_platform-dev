<template>
  <div class="column col chart-area full-height full-width">
    <q-inner-loading :showing="visibleLoading" dark />
    <v-chart
      resizable
      autoresize
      ref="chartRef"
      :option="chartOption"
      @Datazoom="handleDatazoom"
      @BrushEnd="handleBrushEnd"
    />
    <div class="summary-label" v-if="summaryLabel">{{ summaryLabel }}</div>
    <q-toggle
      class="normalize-label"
      v-model="showNormalizeData"
      :label="t('정규화')"
      @update:modelValue="handleNormalizeToggle"
    />
  </div>
</template>

<script setup>
import VChart from 'vue-echarts'
import { BarChart, LineChart } from 'echarts/charts'
import { CanvasRenderer } from 'echarts/renderers.js'
import {
  BrushComponent,
  DataZoomComponent,
  GridComponent,
  LegendComponent,
  TitleComponent,
  ToolboxComponent,
  TooltipComponent,
} from 'echarts/components'
import { use } from 'echarts/core'
import { date } from 'quasar'
import { DATE_TIME_FORMAT } from 'src/common/constant/format'
import { computed, onMounted, ref, watch } from 'vue'
import SummaryDataService from 'src/services/parameterData/SummaryDataService'
import { pt } from 'src/plugins/i18n'
import { getColor } from 'src/common/constant/chart'
import { useI18n } from 'vue-i18n'
import _ from 'lodash'
import AiService from 'src/services/ai/AiService'

use([
  LineChart,
  BarChart,
  CanvasRenderer,
  LegendComponent,
  TooltipComponent,
  GridComponent,
  DataZoomComponent,
  ToolboxComponent,
  TitleComponent,
  BrushComponent,
])

const ZOOM_TOOL_TITLE = 'zoom'
const GRID_WIDTH_PERCENT = 90
const CHART_BASE_OPTION = {
  legend: {
    orient: 'horizontal',
    icon: 'rect',
  },
  title: {
    text: '',
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
    formatter: (points) => {
      const summaryType = captureTrendData.value.type?.toLowerCase() || ''
      if (summaryType.includes('raw')) {
        const pointTime = points[0].axisValueLabel
        return [
          pointTime,
          ...Array.from(new Set(points.map((point) => `${point.marker} ${point.seriesName}: ${point.value[1]}`))),
        ].join('<br/>')
      }
      return null
    },
  },
  toolbox: {
    feature: {
      dataZoom: {
        yAxisIndex: 'none',
        brushStyle: {
          color: 'rgba(0, 0, 0, 0.4)',
        },
        icon: {
          back: 'svg://',
          zoom: 'svg://M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z',
        },
        title: {
          zoom: ZOOM_TOOL_TITLE,
        },
      },
      myZoomOut: {
        show: false,
        title: 'zoom out',
        icon: 'svg://M15.5,14h-.79l-.28-.27C15.41,12.59,16,11.11,16,9.5,16,5.91,13.09,3,9.5,3S3,5.91,3,9.5,5.91,16,9.5,16c1.61,0,3.09-.59,4.23-1.57l.27.28v.79l5,4.99L20.49,19l-4.99-5zm-6,0C7.01,14,5,11.99,5,9.5S7.01,5,9.5,5,14,7.01,14,9.5,11.99,14,9.5,14zM7,9h5v1H7z',
        onclick: async (...params) => {
          await handleZoomOutClick(params)
        },
      },
    },
  },
  grid: [
    {
      top: 30,
      bottom: 40,
      left: '5%',
      right: '5%',
      width: `${GRID_WIDTH_PERCENT}%`,
    },
  ],
  dataZoom: [
    {
      type: 'inside',
      zoomOnMouseWheel: false,
      xAxisIndex: 0,
      filterMode: 'filter',
    },
  ],
  xAxis: {
    type: 'time',
    axisLabel: {
      rotate: 12,
      formatter: (value) => date.formatDate(value, DATE_TIME_FORMAT),
    },
  },
  yAxis: [
    {
      type: 'value',
    },
    {
      show: false,
      type: 'value',
    },
  ],
  series: [],
}

const props = defineProps({
  parameterIds: {
    type: Array,
    required: true,
  },
  fromDate: {
    type: Date,
    required: true,
  },
  toDate: {
    type: Date,
    required: true,
  },
  useBrush: {
    type: Boolean,
    required: false,
    default: false,
  },
  useInference: {
    // raw 데이터 조회 단계에서 추론 데이터를 함께 표시할지 여부
    type: Boolean,
    required: false,
    default: false,
  },
  modelId: {
    // useInference가 true일 때, 추론 데이터 조회를 위한 모델 ID
    type: Number,
    required: false,
  },
})

const emit = defineEmits({
  changeChartData: (chartDataList) => chartDataList,
  /**
   *
   * @param from: Date
   * @param to: Date
   * @param data: Array<{name: string, data: Array<{time: Date, value: number}>}>
   * @returns {{from: Date, to: Date, data: Array<{name: string, data: Array<{time: Date, value: number}>}>}}
   */
  selectedDataRange: ({ from, to, data }) => ({ from, to, data }),
})

const { t } = useI18n()

const visibleLoading = ref(false)
const showNormalizeData = ref(false)

const chartRef = ref(null)
const chartOption = ref({ ...CHART_BASE_OPTION })

const captureTrendData = ref({
  type: null,
  parameters: [],
})

const seriesColorMap = ref(new Map())
const zoomHistory = ref([])

const summaryLabel = computed(() => {
  const type = captureTrendData.value.type
  if (!type) {
    return null
  }
  return t('요약시간 단위') + ' : ' + t(type)
})

const showLoading = () => (visibleLoading.value = true)
const hideLoading = () => (visibleLoading.value = false)

const withLoading = async (taskCallback) => {
  try {
    showLoading()
    await taskCallback()
  } finally {
    hideLoading()
  }
}

const handleDatazoom = async (event) => {
  const batch = event.batch[0]

  const { startValue, endValue } = batch

  await withLoading(async () => {
    const from = new Date(startValue)
    const to = new Date(endValue)

    const result = await getChartData(from, to)
    zoomHistory.value = [
      ...zoomHistory.value,
      {
        type: result.type,
        from: from,
        to: to,
      },
    ]

    updateChartSeries(from, to, trendDataToSeries(result.parameters))
  })
}

const handleZoomOutClick = async () => {
  if (zoomHistory.value.length <= 1) {
    return
  }
  await withLoading(async () => {
    const lastHistoryParent = zoomHistory.value[zoomHistory.value.length - 2]
    const { from, to } = lastHistoryParent

    const result = await getChartData(from, to)
    zoomHistory.value = zoomHistory.value.filter((v, index) => index !== zoomHistory.value.length - 1)

    updateChartSeries(from, to, trendDataToSeries(result.parameters))
  })
}

const getChartData = async (from, to) => {
  const result = await SummaryDataService.getFastTrendData(from, to, props.parameterIds, getChartWidth())

  const { type } = result
  if (props.useInference && type.toLowerCase().includes('raw')) {
    try {
      const inferenceResult = await AiService.getInferenceData(props.modelId, from, to)
      result.parameters.push({
        name: `${inferenceResult.parameterName} (${t('추론')})`,
        items: inferenceResult.items,
      })
    } catch (error) {
      const message = error?.response?.data || ''
      console.error('추론 실패: ', message)
    }
  }

  captureTrendData.value = result
  return result
}

const getChartWidth = () => {
  return parseInt(chartRef.value.getWidth() * (GRID_WIDTH_PERCENT * 0.01))
}

const cloneDeep = (object) => {
  return _.cloneDeep(object)
}

const initChartOption = (xMinDate, xMaxDate, seriesData) => {
  emit('changeChartData', seriesData)

  const option = cloneDeep(CHART_BASE_OPTION)

  option.xAxis = {
    ...cloneDeep(option.xAxis),
    min: xMinDate,
    max: xMaxDate,
  }

  seriesData.forEach((v, index) => {
    if (seriesColorMap.value.has(v.name) === false) {
      seriesColorMap.value.set(v.name, getColor(index))
    }
  })

  if (props.useBrush) {
    const toolBox = {
      ...option.toolbox,
      feature: {
        ...option.toolbox.feature,
        brush: {
          type: ['lineX'],
          title: {
            lineX: '범위 선택',
          },
        },
        myBrushClear: {
          show: true,
          title: '범위 선택 해제',
          icon: 'path://M22,14.7l30.9,31 M52.9,14.7L22,45.7 M4.7,16.8V4.2h13.1 M26,4.2h7.8 M41.6,4.2h7.8 M70.3,16.8V4.2H57.2 M4.7,25.9v8.6 M70.3,25.9v8.6 M4.7,43.2v12.6h13.1 M26,55.8h7.8 M41.6,55.8h7.8 M70.3,43.2v12.6H57.2',
          onclick: async () => {
            clearBrush()
          },
        },
      },
    }

    const brush = {
      toolBox: ['lineX', 'clear'],
      xAxisIndex: 'all',
      brushLink: 'all',
    }

    option.toolbox = toolBox
    option.brush = brush
  }

  option.legend = {
    ...cloneDeep(option.legend),
    data: seriesData.map((v) => pt(v.name)),
  }

  option.series = seriesData.map((v) => ({
    type: 'line',
    showSymbol: false,
    largeThreshold: 1000,
    emphasis: {
      disabled: true,
    },
    itemStyle: {
      color: seriesColorMap.value.get(v.name),
    },
    name: v.name,
    data: v.data,
  }))

  chartOption.value = option
  activeZoomMode()
}

const updateChartSeries = (xMinDate, xMaxDate, seriesData) => {
  emit('changeChartData', seriesData)

  const xAxis = {
    min: xMinDate,
    max: xMaxDate,
  }

  const series = seriesData.map((v) => ({
    type: 'line',
    showSymbol: false,
    largeThreshold: 1000,
    emphasis: {
      disabled: true,
    },
    itemStyle: {
      color: seriesColorMap.value.get(v.name),
    },
    name: v.name,
    data: v.data,
  }))

  const legends = seriesData.map((v) => pt(v.name))

  // 기존 옵션을 유지하면서 series만 업데이트
  const instance = chartRef.value.chart
  instance.setOption(
    {
      toolbox: {
        feature: {
          myZoomOut: {
            show: zoomHistory.value.length > 1,
          },
        },
      },
      legend: {
        data: legends,
      },
      dataZoom: [
        {
          start: xMinDate,
          end: xMaxDate,
        },
      ],
      xAxis,
      series,
    },
    {
      replaceMerge: ['series'],
    },
  )
}

const trendDataToSeries = (trendDataList) => {
  if (showNormalizeData.value) {
    return trendDataList.map((v) => {
      const max = Math.max(...v.items.map((item) => item.value))
      const min = Math.min(...v.items.map((item) => item.value))

      const toNomarlizeValue = (value) => {
        if (max === min) {
          return 0
        }
        return (value - min) / (max - min)
      }

      return {
        name: pt(v.name),
        data: v.items.map((item) => [item.time, toNomarlizeValue(item.value)]),
      }
    })
  }

  return trendDataList.map((v) => {
    return {
      name: pt(v.name),
      data: v.items.map((item) => [item.time, item.value]),
    }
  })
}

const handleNormalizeToggle = async () => {
  if (zoomHistory.value.length === 0) {
    return
  }

  await withLoading(async () => {
    const currentZoom = zoomHistory.value[zoomHistory.value.length - 1]
    const { from, to } = currentZoom

    updateChartSeries(from, to, trendDataToSeries(captureTrendData.value.parameters))
  })
}

const handleBrushEnd = (param) => {
  const [fromTimestamp, toTimestamp] = param.areas[0].coordRange

  const from = new Date(fromTimestamp)
  const to = new Date(toTimestamp)

  const selectedRangeData = captureTrendData.value.parameters.map((parameterData) => {
    const rangeData = []
    for (const item of parameterData.items) {
      const itemTimestamp = new Date(item.time).getTime()
      if (itemTimestamp > toTimestamp) {
        break
      }
      if (itemTimestamp >= fromTimestamp) {
        rangeData.push({ time: new Date(item.time), value: item.value })
      }
    }
    return {
      name: parameterData.name,
      data: rangeData,
    }
  })
  emit('selectedDataRange', { from, to, data: selectedRangeData })
}

const initData = async () => {
  await withLoading(async () => {
    const { fromDate, toDate } = props
    const result = await getChartData(fromDate, toDate)

    zoomHistory.value = [
      {
        type: result.type,
        from: fromDate,
        to: toDate,
      },
    ]
    initChartOption(fromDate, toDate, trendDataToSeries(result.parameters))
  })
}

const activeZoomMode = () => {
  const zoomActiveDispatch = {
    type: 'takeGlobalCursor',
    key: 'dataZoomSelect',
    dataZoomSelectActive: true,
  }
  // 지연시간을 주지 않으면 제대로 동작하지 않음
  setTimeout(() => {
    chartRef.value.chart.dispatchAction(zoomActiveDispatch)
  }, 100)
}

const clearBrush = () => {
  const instance = chartRef.value.chart
  instance.dispatchAction({
    type: 'brush',
    areas: [],
  })
}

watch([() => props.parameterIds, () => props.fromDate, () => props.toDate], async () => {
  await initData()
})

//최초 조회 시 데이터 로드를 위한 onMounted
onMounted(async () => {
  await initData()
  chartRef.value.chart.getZr().on('click', (event) => {
    const target = event.target
    // 줌 툴을 클릭했을 때, 브러시로 선택된 영역이 있다면 초기화 한다
    if (target && ZOOM_TOOL_TITLE === target.__title) {
      clearBrush()
    }
  })
})
</script>

<style lang="scss">
.chart-area {
  position: relative;
  padding-top: 25px;

  .summary-label {
    position: absolute;
    right: 120px;
    top: -4px;
    padding: 4px 10px;
    border-radius: 4px;
    background-color: var(--mainColor);
    color: #fff;
  }

  .normalize-label {
    position: absolute;
    right: 10px;
    top: -8px;
    padding: 4px 10px;
    border-radius: 4px;
  }
}
</style>
