<template>
  <div class="card">
    <div class="card_title">{{ $t('평균 수질') }}</div>
    <div class="card_body">
      <!-- <div>현재 평균: 5m</div> -->
      <div class="chart_container">
        <ECharts
          ref="chart"
          :option="options"
          class="q-mt-md"
          :resizable="true"
          autoresize
          :style="{ height: chartHeight + 'px' }"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import ECharts from 'vue-echarts'
import { use } from 'echarts/core'
import { ScatterChart } from 'echarts/charts'
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
  ScatterChart,
  DataZoomComponent,
  GridComponent,
  LegendComponent,
  TitleComponent,
  ToolboxComponent,
  TooltipComponent,
  CanvasRenderer,
])

const props = defineProps({ datas: { type: Array } })

const data = ref([])
const chartHeight = ref(200)
let months = []
// prettier-ignore
const params = [];
const title = []
const singleAxis = []
const series = []
const options = ref({
  tooltip: {
    position: 'top',
  },
  title: title,
  singleAxis: singleAxis,
  series: series,
})
watch(
  () => props.datas,
  (newVal) => {
    params.value = []
    months = []
    newVal[0].datas.forEach((data) => {
      months.push(data.month)
    })
    const paramCount = newVal.length
    const height = 90
    newVal.forEach(function (param, idx) {
      params.value.push(param.name)
      title.push({
        textBaseline: 'middle',
        top: ((idx + 0.5) * height) / paramCount + '%',
        text: param.name,
        textStyle: {
          fontSize: 12,
          fontWeight: 'normal',
        },
      })
      singleAxis.push({
        left: 100,
        type: 'category',
        boundaryGap: false,
        data: months,
        top: (idx * height) / paramCount + 5 + '%',
        height: height / paramCount - 10 + '%',
        axisLabel: {
          interval: 2,
        },
      })
      series.push({
        singleAxisIndex: idx,
        coordinateSystem: 'singleAxis',
        type: 'scatter',
        data: [],
        symbolSize: function (dataItem) {
          return dataItem[1] * 4
        },
      })
      param.datas.forEach((data) => {
        series[series.length - 1].data.push([data.month, data.value])
      })
    })
    data.value = []
    // data.value.forEach(function (dataItem) {
    //   series[dataItem[0]].data.push([dataItem[1], dataItem[2]]);
    // });

    options.value = {
      tooltip: {
        position: 'top',
      },
      title: title,
      singleAxis: singleAxis,
      series: series,
    }
  },
  { immediate: true, deep: true },
)

onMounted(() => {})
</script>

<style lang="scss" scoped>
.chart_container {
  width: calc(100% - 10px);
  height: calc(100% - 20px);
  overflow-y: hidden;
  overflow-x: hidden;
}
</style>
