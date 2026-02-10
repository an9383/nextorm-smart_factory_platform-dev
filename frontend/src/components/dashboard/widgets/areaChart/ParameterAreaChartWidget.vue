<template>
  <div class="full-width full-height">
    <ECharts ref="chart" :option="options" :resizable="true" autoresize />
  </div>
</template>

<script setup>
import { defineProps, ref, watch } from 'vue'
import ParameterDataService from '/src/services/parameterData/ParameterDataService'
import { useWidgetRefresh, widgetProps } from '/src/common/module/widgetCommon'
import { pt } from 'src/plugins/i18n'
import ECharts from 'vue-echarts'
import { graphic, use } from 'echarts/core'
import { LineChart } from 'echarts/charts'
import { CanvasRenderer } from 'echarts/renderers'
import {
  DataZoomComponent,
  GraphicComponent,
  GridComponent,
  TitleComponent,
  TooltipComponent,
} from 'echarts/components'

use([LineChart, GraphicComponent, DataZoomComponent, GridComponent, TitleComponent, TooltipComponent, CanvasRenderer])

defineProps(widgetProps)

//데이터 조회(refresh)
const chartData = ref(null)

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
    bottom: '5%',
    top: '5%',
    containLabel: true,
  },
  xAxis: [
    {
      boundaryGap: false,
      data: [],
    },
  ],
  yAxis: [
    {
      type: 'value',
    },
  ],
  series: [
    {
      type: 'line',
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
            color: '#C2FEFE',
          },
          {
            offset: 1,
            color: '#71C2F5',
          },
        ]),
      },
      emphasis: {
        focus: 'series',
      },
      data: [],
    },
  ],
})

watch(
  () => chartData.value,
  (value) => {
    options.value = {
      ...options.value,
      xAxis: [
        {
          ...options.value.xAxis[0],
          data: value.map((m) => m.date),
        },
      ],
      series: [
        {
          ...options.value.series[0],
          name: pt(chartData.value.name),
          data: value.map((m) => m.value),
        },
      ],
    }
  },
)

const refresh = async (config) => {
  const { parameterId, operator, timeCriteria, period } = config

  let from, to
  if (period === 'PERIOD') {
    //기간 선택
    to = new Date(config.to)
    from = new Date(config.from)
  }

  const statisticsData = await ParameterDataService.getParameterStatistics(
    parameterId,
    operator,
    timeCriteria,
    from,
    to,
  )
  chartData.value = statisticsData.chartItems
  chartData.value.name = statisticsData.name
}
useWidgetRefresh(refresh)
</script>
