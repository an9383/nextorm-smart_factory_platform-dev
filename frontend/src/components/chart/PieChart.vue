<template>
  <div class="full-width full-height">
    <ECharts ref="chart" :option="options" @zr:click="handleChartClick" class="q-mt-md" resizable autoresize />
  </div>
</template>

<script setup>
import ECharts from 'vue-echarts'
import { use } from 'echarts/core'
import { PieChart } from 'echarts/charts'
import { CanvasRenderer } from 'echarts/renderers'
import { ref, watch } from 'vue'
import { pickColor } from 'src/common/constant/chart'
import { LegendComponent, TitleComponent, TooltipComponent } from 'echarts/components'

use([PieChart, LegendComponent, TitleComponent, TooltipComponent, CanvasRenderer])

const chart = ref(null)

const props = defineProps({
  datas: {
    type: Array,
    required: false,
    default: () => [],
  },
  title: {
    type: String,
    required: false,
    default: '',
  },
  showLegend: {
    type: Boolean,
    required: false,
    default: false,
  },
  radius: {
    type: String,
    required: false,
    default: '50%',
  },
  tooltipFormatter: {
    type: Function,
    required: false,
    default: null,
  },
})

const emit = defineEmits(['click-chart'])

const options = ref(null)

const handleChartClick = (params) => {
  if (!chart.value) return

  emit('click-chart', { name: params.name, value: params.value })
}

watch(
  () => props.datas,
  (datas) => {
    options.value = {
      title: props.title
        ? {
            text: props.title,
            left: 'center',
            top: 20,
            textStyle: {
              fontSize: 20,
              fontWeight: 'bold',
            },
          }
        : {},
      legend: {
        orient: 'horizontal',
        icon: 'rect',
        show: props.showLegend,
      },
      tooltip: {
        trigger: 'item',
        formatter: props.tooltipFormatter || '{b}: {c} ({d}%)',
      },
      series: [
        {
          type: 'pie',
          radius: props.radius,
          data: datas.map((item) => ({
            value: item.value,
            name: item.name,
            itemStyle: {
              color: item.color || pickColor(),
            },
          })),
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)',
            },
          },
          label: {
            fontSize: 12,
          },
        },
      ],
    }
  },
  { immediate: true },
)
</script>

<style lang="scss" scoped></style>
