<template>
  <div class="card">
    <div class="card_title">{{ $t('평균 수질 Spec') }}</div>
    <div class="card_body">
      <div class="spec_select">
        <q-chip v-model:selected="faultSelect" color="red" text-color="white" icon="cake">
          {{ $t('ph_units') }}
        </q-chip>
        <q-chip v-model:selected="faultSelect" color="red" text-color="white" icon="cake"> {{ $t('hdo_sat') }}</q-chip>
        <q-chip v-model:selected="faultSelect" color="red" text-color="white" icon="cake">
          {{ $t('chl_ug_l') }}
        </q-chip>
      </div>
      <div class="chart_container">
        <ECharts ref="chart" :option="options" class="q-mt-md" :resizable="true" autoresize />
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import ECharts from 'vue-echarts'
import { use } from 'echarts/core'
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

const props = defineProps({ datas: { type: Array } })

const faultSelect = ref('')

let xAxis = []
let yAxis = []
let series = []
const options = ref({
  xAxis: xAxis,
  yAxis: yAxis,
  series: series,
})
watch(
  () => props.datas,
  () => {
    xAxis = {
      type: 'category',
      data: ['1', '2', '3', '4', '5', '6', '7'],
    }
    yAxis = {
      type: 'value',
    }
    series = [
      {
        data: [150, 230, 224, 218, 135, 147, 260],
        type: 'line',
      },
      {
        data: [250, 250, 240, 250, 250, 250, 250],
        type: 'line',
        step: 'start',
        color: 'red',
        showSymbol: false,
      },
    ]
    options.value = {
      xAxis: xAxis,
      yAxis: yAxis,
      series: series,
      grid: [{ top: 10, bottom: 40 }],
    }
  },
  { immediate: true, deep: true },
)

onMounted(() => {})
</script>

<style lang="scss" scoped>
.chart_container {
  width: calc(100% - 10px);
  height: calc(100% - 50px);
  overflow-y: hidden;
  overflow-x: hidden;
}

.spec_select {
  display: flex;
  width: 100%;

  & {
    width: 100%;
  }

  overflow: auto;
}
</style>
