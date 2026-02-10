<template>
  <Echarts class="col" :option="options" resizable autoresize />
</template>

<script setup>
import Echarts from 'vue-echarts'
import { defineProps, onMounted, ref, watch } from 'vue'
import SummaryDataService from 'src/services/parameterData/SummaryDataService'
import { t } from '/src/plugins/i18n'
import useUI from 'src/common/module/ui'
import { use } from 'echarts/core'
import { BarChart } from 'echarts/charts'
import { CanvasRenderer } from 'echarts/renderers'
import { GridComponent, LegendComponent, ToolboxComponent, TooltipComponent } from 'echarts/components'

use([BarChart, GridComponent, LegendComponent, ToolboxComponent, TooltipComponent, CanvasRenderer])

const ui = useUI()
const options = ref()

const props = defineProps({
  parameterId: {
    type: Number || String,
    required: true,
  },
  fromYear: {
    type: Number || String,
    required: false,
    default: new Date().getFullYear(),
  },
  toYear: {
    type: Number || String,
    required: false,
    default: new Date().getFullYear(),
  },
})

watch(
  () => [props.parameterId, props.fromYear, props.toYear],
  () => {
    loadChartData(props.parameterId, props.fromYear, props.toYear)
  },
)

onMounted(() => {
  loadChartData(props.parameterId, props.fromYear, props.toYear)
})

const loadChartData = async (parameterId, fromYear, toYear) => {
  if (!parameterId || !fromYear || !toYear) {
    return
  }
  ui.loading.show()
  const summaryData = await SummaryDataService.getMonthlySummaryData(parameterId, fromYear, toYear)
  ui.loading.hide()
  createChart(summaryData)
}

const createChart = (summaryData) => {
  const months = [
    t('1월'),
    t('2월'),
    t('3월'),
    t('4월'),
    t('5월'),
    t('6월'),
    t('7월'),
    t('8월'),
    t('9월'),
    t('10월'),
    t('11월'),
    t('12월'),
  ]
  const xAxisData = months.slice(0, 12)
  const seriesData = {}
  summaryData.forEach((item) => {
    item.years.forEach((yearData) => {
      if (!seriesData[yearData.year]) {
        seriesData[yearData.year] = Array(months.length).fill(null)
      }
      seriesData[yearData.year][item.month - 1] = yearData.value.toFixed(4)
    })
  })
  const allValues = summaryData.flatMap((item) => item.years.map((yearData) => yearData.value))
  const maxValue = (Math.max(...allValues) * 1.05).toFixed(4)

  const labelOption = {
    show: true,
    rotate: 90,
    align: 'left',
    verticalAlign: 'middle',
    position: 'insideBottom',
    distance: 15,
    formatter: '{c}  {name|{a}}',
    fontSize: 16,
    rich: {
      name: {},
    },
  }

  const option = {
    backgroundColor: '#FFFFFF',
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow',
      },
    },
    legend: {
      data: Object.keys(seriesData).map((year) => year.toString()),
    },
    toolbox: {
      show: true,
      orient: 'vertical',
      left: 'right',
      top: 'center',
    },
    xAxis: [
      {
        type: 'category',
        axisTick: { show: false },
        data: xAxisData,
      },
    ],
    yAxis: [
      {
        type: 'value',
        max: maxValue,
      },
    ],
    series: Object.keys(seriesData).map((year) => ({
      name: year,
      type: 'bar',
      label: labelOption,
      emphasis: {
        focus: 'series',
      },
      data: seriesData[year],
    })),
  }
  options.value = option
}
</script>

<style scoped></style>
