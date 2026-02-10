<template>
  <div class="full-width full-height chart_container">
    <ECharts v-if="isChartDataLoaded" ref="chart" :option="options" :resizable="true" autoresize />
  </div>
</template>

<script setup>
import { defineProps, ref, watch } from 'vue'
import { useWidgetRefresh, widgetProps } from 'src/common/module/widgetCommon'
import { pt } from 'src/plugins/i18n'
import ECharts from 'vue-echarts'
import ParameterDataService from 'src/services/parameterData/ParameterDataService'
import { getColor } from 'src/common/constant/chart'
import { use } from 'echarts/core'
import { ScatterChart } from 'echarts/charts'
import { CanvasRenderer } from 'echarts/renderers'
import { DataZoomComponent, SingleAxisComponent, TitleComponent, TooltipComponent } from 'echarts/components'

use([ScatterChart, DataZoomComponent, SingleAxisComponent, TitleComponent, TooltipComponent, CanvasRenderer])

defineProps(widgetProps)

const isChartDataLoaded = ref(false)
//데이터 조회(refresh)
const chartData = ref([])

const options = ref({
  tooltip: {
    position: 'top',
  },
  title: [],
  singleAxis: [],
  series: [],
})

watch(
  () => chartData.value,
  (newValue) => {
    const title = []
    const singleAxis = []
    const series = []
    let dates = []
    newValue.forEach((data) => {
      dates = dates.concat(data.chartItems.map((item) => item.date))
    })
    dates = Array.from(new Set(dates))
    dates.sort()

    const paramCount = newValue.length
    const height = 90

    newValue.forEach((data, idx) => {
      const totalData = data.chartItems.reduce((acc, item) => acc + item.value, 0)
      const symbolSizeRatio = height * 0.01
      title.push({
        textBaseline: 'middle',
        top: ((idx + 0.5) * height) / paramCount + '%',
        text: pt(data.name),
        textStyle: {
          fontSize: 12,
          fontWeight: 'normal',
        },
      })
      singleAxis.push({
        left: 100,
        type: 'time',
        axisLabel: {
          interval: 2,
          rotate: 15,
          formatter: function () {
            return '{yyyy}-{MM}-{dd}'
          },
        },
        boundaryGap: false,
        data: dates,
        top: (idx * height) / paramCount + 5 + '%',
        height: height / paramCount - 10 + '%',
      })
      series.push({
        singleAxisIndex: idx,
        coordinateSystem: 'singleAxis',
        type: 'scatter',
        itemStyle: {
          color: getColor(idx),
        },
        data: [],
        symbolSize: function (dataItem) {
          if (dataItem[1] === 0) {
            return 0
          }
          const symbolSize = (dataItem[1] / totalData) * 100 * symbolSizeRatio
          return symbolSize
        },
      })
      data.chartItems.forEach((chartItem) => {
        series[series.length - 1].data.push([chartItem.date, chartItem.value])
      })
    })

    options.value = {
      tooltip: {
        position: 'top',
      },
      title: title,
      singleAxis: singleAxis,
      series: series,
    }

    if (!isChartDataLoaded.value) {
      isChartDataLoaded.value = true
    }
  },
)

const refresh = async (config) => {
  const { parameterIds, operator, timeCriteria, period } = config

  let from, to
  if (period === 'PERIOD') {
    //기간 선택
    to = new Date(config.to)
    from = new Date(config.from)
  }

  const results = await Promise.all(
    parameterIds.map((parameterId) =>
      ParameterDataService.getParameterStatistics(parameterId, operator, timeCriteria, from, to),
    ),
  )
  chartData.value = results
}
useWidgetRefresh(refresh)
</script>
<style lang="scss" scoped>
.chart_container {
  width: calc(100% - 10px);
  height: calc(100% - 20px);
  overflow-y: hidden;
  overflow-x: hidden;
}
</style>
