<template>
  <div class="full-width" style="position: relative; overflow: visible">
    <div v-if="dpXParamCount < 6">
      <v-chart
        ref="echartRef"
        :option="chartOption"
        autoresize
        class="my-echart"
        style="width: 100%; height: 300px; overflow: visible"
      />
    </div>
    <div v-else>
      <q-banner class="bg-red text-white">
        {{ $t('파라미터가 5개 이하일 경우만 가능합니다.') }}
      </q-banner>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed, nextTick } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { ScatterChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, GraphicComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

use([ScatterChart, GridComponent, TooltipComponent, CanvasRenderer, GraphicComponent])

const props = defineProps({
  correlationChartData: {
    type: Array,
    required: true,
  },
  yParameter: {
    type: Object,
    required: true,
  },
})

const chartOption = ref({})
const echartRef = ref(null)

const layout = {
  gapPx: 8,
  marginPx: 15,
  chartHeight: 270,
  padding: {
    left: 5,
    right: 2,
    top: 2,
    bottom: 5,
  },
  labelStyle: {
    font: '9px Arial',
    fill: '#333',
    textAlign: 'center',
    textVerticalAlign: 'middle',
  },
}

const dpXParamCount = computed(() => props.correlationChartData.filter((d) => d.name !== props.yParameter.name).length)

const normalizeData = (arr) => {
  const min = Math.min(...arr)
  const max = Math.max(...arr)
  return arr.map((v) => (max - min === 0 ? 0 : (v - min) / (max - min)))
}

const pushLabel = ({ text, x, y, rotate = 0, align = 'center', valign = 'middle' }, graphics) => {
  graphics.push({
    type: 'text',
    z: 100,
    zlevel: 10,
    $action: 'merge',
    position: [x, y],
    rotation: rotate,
    style: {
      text,
      font: layout.labelStyle.font,
      fill: layout.labelStyle.fill,
      textAlign: align,
      textVerticalAlign: valign,
    },
  })
}

const getCorScatterChart = async () => {
  if (dpXParamCount.value >= 6) return // 조건 체크

  const datas = props.correlationChartData.filter((d) => d.name !== props.yParameter.name)
  const ascData = [...datas].sort((a, b) => a.name.localeCompare(b.name))
  const descData = [...datas].sort((a, b) => b.name.localeCompare(a.name))
  const size = datas.length

  await nextTick()
  const chartWidth = echartRef.value?.$el?.offsetWidth || 300
  const chartHeight = layout.chartHeight

  const totalMarginWidth = (chartWidth * (layout.padding.left + layout.padding.right)) / 100
  const usableWidth = chartWidth - totalMarginWidth
  const cellWidthPx = (usableWidth - layout.gapPx * (size - 1)) / size

  const totalMarginHeight = (chartHeight * (layout.padding.top + layout.padding.bottom)) / 100
  const usableHeight = chartHeight - totalMarginHeight
  const cellHeightPx = (usableHeight - layout.gapPx * (size - 1)) / size

  const xAxes = []
  const yAxes = []
  const grids = []
  const series = []
  const graphics = []

  let gridIndex = 0

  descData.forEach((xValue, row) => {
    ascData.forEach((yValue, col) => {
      const x = normalizeData(xValue.data.map((v) => v[1]))
      const y = normalizeData(yValue.data.map((v) => v[1]))

      const leftPx = (chartWidth * layout.padding.left) / 100 + col * (cellWidthPx + layout.gapPx)
      const topPx = (chartHeight * layout.padding.top) / 100 + row * (cellHeightPx + layout.gapPx)

      const leftPercent = (leftPx / chartWidth) * 100
      const widthPercent = (cellWidthPx / chartWidth) * 100

      grids.push({
        left: `${leftPercent}%`,
        width: `${widthPercent}%`,
        top: `${topPx}px`,
        height: `${cellHeightPx}px`,
        containLabel: false,
      })

      xAxes.push({
        gridIndex,
        type: 'value',
        min: 0,
        max: 1,
        axisLabel: { show: false },
        axisLine: { show: true, lineStyle: { color: '#999' } },
        axisTick: { show: false },
        splitLine: { show: false },
        silent: true,
      })

      yAxes.push({
        gridIndex,
        type: 'value',
        min: 0,
        max: 1,
        axisLabel: { show: false },
        axisLine: { show: true, lineStyle: { color: '#999' } },
        axisTick: { show: false },
        splitLine: { show: false },
        silent: true,
      })

      series.push({
        type: 'scatter',
        xAxisIndex: gridIndex,
        yAxisIndex: gridIndex,
        data: x.map((xVal, i) => [xVal, y[i]]),
        symbolSize: 3,
        itemStyle: { color: '#1f77b4' },
      })

      if (col === 0) {
        pushLabel(
          {
            text: xValue.name,
            x: layout.marginPx / 2,
            y: topPx + cellHeightPx / 2,
            rotate: Math.PI / 2,
          },
          graphics,
        )
      }

      if (row === size - 1) {
        pushLabel(
          {
            text: yValue.name,
            x: leftPx + cellWidthPx / 2,
            y: topPx + cellHeightPx + layout.marginPx * 0.8,
          },
          graphics,
        )
      }

      gridIndex++
    })
  })

  chartOption.value = {
    animation: false,
    grid: grids,
    xAxis: xAxes,
    yAxis: yAxes,
    series,
    graphic: graphics,
    tooltip: {
      trigger: 'item',
      formatter: (params) => `(${params.data[0].toFixed(3)}, ${params.data[1].toFixed(3)})`,
    },
    left: `${layout.padding.left}%`,
    right: `${layout.padding.right}%`,
    top: `${layout.padding.top}%`,
    bottom: `${layout.padding.bottom}%`,
  }
}

watch(
  () => props.correlationChartData,
  async (newVal) => {
    if (!newVal || newVal.length === 0) return
    if (dpXParamCount.value >= 6) return
    await getCorScatterChart()
  },
  { immediate: true },
)
</script>

<style scoped>
.my-echart div,
.my-echart canvas {
  overflow: visible !important;
}
</style>
