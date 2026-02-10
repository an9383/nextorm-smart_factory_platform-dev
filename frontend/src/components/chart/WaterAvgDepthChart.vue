<template>
  <div class="card">
    <div class="card_title">{{ $t('평균 깊이') }}</div>
    <div class="card_body">
      <div>{{ $t('현재 평균: 5m') }}</div>
      <ECharts ref="chart" :option="options" class="q-mt-md" :resizable="true" autoresize style="height: 25 0px" />
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
            color: 'rgba(128, 255, 165)',
          },
          {
            offset: 1,
            color: 'rgba(1, 191, 236)',
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
onMounted(() => {
  createData()
})

const monthDatas = ref([])

function createData() {
  monthDatas.value = []
  const terrainCoordinates = []
  for (let i = 0; i < 2500; i++) {
    terrainCoordinates.push(Math.random() * 1) // 임의의 높이 값을 생성합니다.
  }
  monthDatas.value.push([...terrainCoordinates])

  for (let i = 1; i < 12; i++) {
    for (let i = 0; i < 2500; i++) {
      terrainCoordinates[i] = terrainCoordinates[i] + (Math.random() < 0.3 ? -1 : 1 * (Math.random() * 1)) // 임의의 높이 값을 생성합니다.
    }
    monthDatas.value.push([...terrainCoordinates])
  }
}
</script>

<style lang="scss" scoped></style>
