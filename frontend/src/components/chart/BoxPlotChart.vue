<template>
  <div class="full-width full-height">
    <Echarts @click="seriesClick" id="echart" ref="chart" :option="options" class="full-width" autoresize />
  </div>
</template>
<script setup>
import { onMounted, ref, watch } from 'vue'
import Echarts from 'vue-echarts'
import { pt } from 'src/plugins/i18n'
import { use } from 'echarts/core'
import { BoxplotChart } from 'echarts/charts'
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
  BoxplotChart,
  DataZoomComponent,
  GridComponent,
  LegendComponent,
  TitleComponent,
  ToolboxComponent,
  TooltipComponent,
  CanvasRenderer,
])
const chart = ref()
const options = ref()
const props = defineProps({
  boxPlotData: {
    type: Array,
  },
})
const emit = defineEmits(['click:series'])

onMounted(() => {
  createChart(props.boxPlotData)
})

watch(
  () => props.boxPlotData,
  (data) => {
    createChart(data)
  },
)
const createChart = (data) => {
  const xValueSet = Array.from(new Set(data.reduce((prev, curr) => prev.concat(curr.seriesData.map((it) => it.x)), [])))

  data.map((it) => it.seriesData.x)
  chart.value.clear()
  options.value = {
    legend: {
      selectedMode: false,
    },
    tooltip: {
      trigger: 'item',
      axisPointer: {
        type: 'shadow',
      },
    },
    xAxis: {
      type: 'category',
      data: xValueSet,
      boundaryGap: true,
      nameGap: 30,
      splitArea: {
        show: true,
      },
      splitLine: {
        show: true,
      },
    },
    yAxis: {
      type: 'value',
      splitArea: {
        show: false,
      },
    },
    dataZoom: [
      {
        type: 'inside',
      },
    ],
    series: data.map((v) => ({
      type: 'boxplot',
      name: pt(v.name),
      data: v.seriesData.map((it) => {
        if (!xValueSet.includes(it.x)) {
          return [null, null, null, null, null]
        }
        const { min, q1, median, q3, max } = it.y
        return [min, q1, median, q3, max]
      }),
    })),
  }
}

const seriesClick = (e) => {
  emit('click:series', { seriesName: e.seriesName, name: e.name })
}
</script>

<style scoped>
#echart {
  height: 400px;
}
</style>
