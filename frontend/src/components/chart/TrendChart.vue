<template>
  <v-chart ref="chart" class="col" :option="options" resizable autoresize @BrushEnd="handleBrushEnd" />
</template>

<script setup>
import VChart from 'vue-echarts'
import { LineChart } from 'echarts/charts'
import { CanvasRenderer } from 'echarts/renderers.js'
import {
  BrushComponent,
  DataZoomComponent,
  GridComponent,
  LegendComponent,
  MarkAreaComponent,
  MarkLineComponent,
  MarkPointComponent,
  TitleComponent,
  ToolboxComponent,
  TooltipComponent,
} from 'echarts/components'
import { use } from 'echarts/core'
import { ref, watch } from 'vue'
import { formatDate } from 'src/common/utils'
import { controlLimitColor, getColor, specLimitColor } from 'src/common/constant/chart'

use([
  LineChart,
  DataZoomComponent,
  GridComponent,
  LegendComponent,
  TitleComponent,
  ToolboxComponent,
  TooltipComponent,
  CanvasRenderer,
  BrushComponent,
  MarkLineComponent,
  MarkAreaComponent,
  MarkPointComponent,
])

/**
 * datas : [
 *  name : series 이름(ex: 파라미터 명),
 *  color: series color,
 *  trendData: [   //차트 데이터 배열
 *    {
 *       x: x축데이터
 *       y: y축 데이터
 *    }
 *  ]
 * ]
 */
const props = defineProps({
  datas: {
    type: Array,
    required: false,
    default: () => [],
  },
  showLegend: {
    type: Boolean,
    required: false,
    default: true,
  },
  legendClickable: {
    type: Boolean,
    required: false,
    default: true,
  },
  showSpecLine: {
    type: Boolean,
    required: false,
    default: false,
  },
  mouseZoom: {
    type: Boolean,
    required: false,
    default: () => true,
  },
  axisLabelFormat: {
    type: String,
    required: false,
    default: null,
  },
  axisLabelFormatFunction: {
    type: Function,
    required: false,
    default: null,
  },
  axisPointerLabelFormatter: {
    type: Function,
    required: false,
    default: null,
  },
  tooltipFormatter: {
    type: Function,
    required: false,
    default: null,
  },
  useBrush: {
    type: Boolean,
    required: false,
    default: false,
  },
})

const formatToFiveDecimals = (value) => {
  return Number(value.toFixed(5))
}

const createOption = (datas) => {
  if (datas == null) {
    return null
  }

  let isAllDateSame = false
  let specMarkLines
  let minYValue
  let maxYValue
  if (datas.length > 0 && datas[0].trendData.length > 0) {
    const firstDate = new Date(datas[0]?.trendData[0]?.x || new Date())
    const lastDate = new Date(datas[0].trendData[datas[0].trendData.length - 1].x)
    isAllDateSame = formatDate(firstDate) === formatDate(lastDate)

    const series = datas[0].trendData.map((rawData) => [new Date(rawData.x), rawData.y])
    specMarkLines = parseSpecMarkLines(datas[0].trendData)
    const allYValues = [
      ...series.map((item) => item[1]),
      ...specMarkLines.flatMap((markLine) => markLine.map((line) => line.coord[1])),
    ]
    minYValue = allYValues.reduce((max, current) => Math.min(max, current), Infinity)
    maxYValue = allYValues.reduce((max, current) => Math.max(max, current), -Infinity)
  }

  let axiosPointerLabel = {
    backgroundColor: '#ccc',
    borderColor: '#aaa',
    borderWidth: 1,
    shadowBlur: 0,
    shadowOffsetX: 0,
    shadowOffsetY: 0,
    color: '#222',
  }

  if (props.axisPointerLabelFormatter) {
    axiosPointerLabel.formatter = props.axisPointerLabelFormatter
  }

  let toolBox = {}
  let brush = null
  if (props.useBrush) {
    toolBox = {
      feature: {
        brush: {
          type: ['lineX', 'clear'],
          title: {
            lineX: '범위 선택',
            clear: '범위 선택 해제',
          },
        },
      },
    }

    brush = {
      toolBox: ['lineX', 'clear'],
      xAxisIndex: 'all',
      brushLink: 'all',
    }
  }

  return {
    toolbox: toolBox,
    brush,
    backgroundColor: '#FFFFFF',
    legend: {
      orient: 'horizontal',
      icon: 'rect',
      show: props.showLegend && datas.length > 1,
      selectedMode: props.legendClickable,
    },
    tooltip: {
      trigger: 'axis',
      order: 'seriesAsc',
      axisPointer: {
        type: 'cross',
        animation: false,
        label: axiosPointerLabel,
      },
      formatter: props.tooltipFormatter || null,
    },
    grid: [{ top: 30, bottom: 40 }],
    dataZoom: [{ type: 'inside', zoomOnMouseWheel: props.mouseZoom }],
    xAxis: {
      type: 'time',
      axisLabel: {
        rotate: isAllDateSame ? 35 : 15,
        formatter: function (value, index) {
          if (props.axisLabelFormat) {
            return props.axisLabelFormat
          }

          if (props.axisLabelFormatFunction) {
            return props.axisLabelFormatFunction(value, index)
          }

          if (isAllDateSame) {
            return '{HH}:{mm}'
          } else {
            return '{yyyy}-{MM}-{dd} {HH}:{mm}'
          }
        },
        interval: 'auto',
      },
    },
    yAxis: {
      type: 'value',
      min: props.showSpecLine
        ? formatToFiveDecimals(minYValue - (maxYValue - minYValue) * 0.05)
        : (value) => formatToFiveDecimals(value.min - (value.max - value.min) * 0.05),
      max: props.showSpecLine
        ? formatToFiveDecimals(maxYValue + (maxYValue - minYValue) * 0.05)
        : (value) => formatToFiveDecimals(value.max + (value.max - value.min) * 0.05),
    },
    series: datas.map((data, index) => ({
      type: 'line',
      showSymbol: false,
      name: data.name,
      itemStyle: {
        color: data.color || getColor(index),
      },
      lineStyle: {
        width: 2.5,
      },
      data: data.trendData.map((v) => [v.x, v.y]),
      label: {
        formatter: function () {
          return '적용 위치 테스트'
        },
      },
      markLine: {
        data: specMarkLines,
      },
      markArea: {
        data: props.showSpecLine ? specMarkLines : {},
        label: {
          show: true,
          position: 'right',
        },
      },
    })),
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
        temp.endX = rawData.x
      } else {
        specLineData.push(createSpecLineData(type, temp.startX, temp.endX, temp.y))
        tempLineData[type] = createSpecLineData(type, rawData.x, rawData.x, rawData[type])
      }
    } else {
      tempLineData[type] = createSpecLineData(type, rawData.x, rawData.x, rawData[type])
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

const createMarkLine = (specLineData) => {
  const { startX, endX, y, type } = specLineData
  const label = `${type.toUpperCase()} (${y})`
  const color = type === 'usl' || type === 'lsl' ? specLimitColor : controlLimitColor
  return [
    {
      coord: [startX, y],
      symbol: 'circle',
      lineStyle: {
        color: color,
        width: 1,
        type: 'dashed',
      },
      itemStyle: {
        borderColor: color,
        color: 'rgba(255, 0, 0, 0.2)',
        borderWidth: 0.5,
        borderType: 'dashed',
      },
    },
    {
      name: label,
      coord: [endX, y],
      symbol: 'circle',
      label: {
        show: true,
      },
    },
  ]
}

const createSpecLineData = (type, startX, endX, y) => {
  return {
    type,
    startX,
    endX,
    y,
  }
}

const emit = defineEmits(['selectedDataRange'])

const handleBrushEnd = (param) => {
  const from = new Date(param.areas[0].coordRange[0])
  const to = new Date(param.areas[0].coordRange[1])
  emit('selectedDataRange', { from, to })
}

const chart = ref(null)
const options = ref(createOption(props.datas))

watch(
  () => props.datas,
  (datas) => {
    options.value = createOption(datas)
  },
)
</script>
