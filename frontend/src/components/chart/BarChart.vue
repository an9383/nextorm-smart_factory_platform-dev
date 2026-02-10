<template>
  <div class="full-width full-height">
    <ECharts ref="chart" :option="options" @zr:click="handleChartClick" class="q-mt-md" resizable autoresize />
  </div>
</template>

<script setup>
import ECharts from 'vue-echarts'
import { use } from 'echarts/core'
import { BarChart } from 'echarts/charts'
import { CanvasRenderer } from 'echarts/renderers'
import { ref, watch } from 'vue'
import { pickColor } from 'src/common/constant/chart'
import { DataZoomComponent, GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'

use([BarChart, DataZoomComponent, GridComponent, LegendComponent, TooltipComponent, CanvasRenderer])

const chart = ref(null)
const formatToFiveDecimals = (value) => {
  return Number(value.toFixed(5))
}

const props = defineProps({
  datas: {
    type: Array,
  },
})

const emit = defineEmits(['click-chart'])

const options = ref(null)

const handleChartClick = (params) => {
  if (!chart.value) return

  const pointInPixel = [params.offsetX, params.offsetY]
  const xPixel = pointInPixel[0]

  const option = chart.value.getOption()
  const xAxisData = option.xAxis[0].data

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

  const parameterName = xAxisData[closestIndex]

  emit('click-chart', { name: parameterName })
}

watch(
  () => props.datas,
  (datas) => {
    options.value = {
      legend: {
        orient: 'horizontal',
        icon: 'rect',
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
          return `<b>${params[0].name}</b> : ${params[0].value}`
        },
      },
      grid: [{ top: 30, bottom: 40 }],
      dataZoom: [{ type: 'inside' }],
      xAxis: [
        {
          type: 'category',
          data: datas.map((v) => v.category),
          axisLabel: {
            interval: 0,
          },
        },
      ],
      yAxis: [
        {
          type: 'value',
          min: (value) => formatToFiveDecimals(value.min - (value.max - value.min) * 0.05),
          max: (value) => formatToFiveDecimals(value.max + (value.max - value.min) * 0.05),
        },
      ],
      series: [
        {
          type: 'bar',
          barGap: 0,
          emphasis: {
            focus: 'series',
          },
          data: datas.map((v) => ({
            value: v.value,
            itemStyle: {
              color: v.color || pickColor(),
            },
          })),
        },
      ],
    }
  },
  { immediate: true },
)
</script>

<style lang="scss" scoped></style>
