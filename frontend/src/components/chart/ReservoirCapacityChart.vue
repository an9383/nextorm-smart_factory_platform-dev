<template>
  <div class="card">
    <div class="card_title">{{ $t('저수용량') }}</div>
    <div class="card_body">
      <div>{{ $t('현재 용량: 5t') }}</div>
      <ECharts ref="barchart" :option="options" class="q-mt-md" :resizable="true" autoresize style="height: 25 0px" />
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import ECharts from 'vue-echarts'
import { graphic, use } from 'echarts/core'
import { LineChart } from 'echarts/charts'
import { CanvasRenderer } from 'echarts/renderers'
import {
  DataZoomComponent,
  GridComponent,
  LegendComponent,
  TitleComponent,
  ToolboxComponent,
  TooltipComponent,
} from 'echarts/components'

use([
  LineChart,
  DataZoomComponent,
  GridComponent,
  LegendComponent,
  TitleComponent,
  ToolboxComponent,
  TooltipComponent,
  CanvasRenderer,
])

const options = ref({
  color: ['#80FFA5'],
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'cross',
      label: {
        backgroundColor: '#6a7985',
      },
    },
  },

  grid: {
    left: '3%',
    right: '4%',
    bottom: '20%',
    top: '5%',
    containLabel: true,
  },
  xAxis: [
    {
      type: 'category',
      boundaryGap: false,
      data: ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12'],
      // axisLabel: {
      //   textStyle: {
      //     color: "white",
      //   },
      // },
    },
  ],
  yAxis: [
    {
      type: 'value',
      // axisLabel: {
      //   textStyle: {
      //     color: "white",
      //   },
      // },
    },
  ],
  series: [
    {
      name: 'Deep',
      type: 'line',
      // stack: "Total",
      smooth: true,
      lineStyle: {
        width: 0,
      },
      showSymbol: false,
      areaStyle: {
        opacity: 0.8,
        color: new graphic.LinearGradient(0, 0, 0, 1, [
          {
            offset: 0,
            color: '#D6E0FD',
          },
          {
            offset: 1,
            color: '#D1ECFD',
          },
        ]),
      },
      emphasis: {
        focus: 'series',
      },
      data: [4.4, 4.5, 4.6, 4.7, 4.3, 4.4, 4.5, 5, 5.1, 4.8, 4.6, 4.1],
    },
  ],
})
onMounted(() => {})
</script>

<style lang="scss" scoped></style>
