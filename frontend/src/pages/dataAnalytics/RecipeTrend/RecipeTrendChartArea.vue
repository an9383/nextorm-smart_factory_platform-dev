<template>
  <q-tabs
    v-model="chartType"
    dense
    class="bg-grey-8 text-white chart-type-tab"
    align="justify"
    narrow-indicator
    @update:modelValue="handleTabChange"
  >
    <q-tab :name="CHART_TYPES.NORMAL" :label="LABELS[CHART_TYPES.NORMAL]" />
    <q-tab :name="CHART_TYPES.DTW" :label="LABELS[CHART_TYPES.DTW]" />
  </q-tabs>

  <q-separator />

  <q-tab-panels v-model="chartType" class="chart-panel">
    <q-tab-panel :name="CHART_TYPES.NORMAL">
      <trend-chart
        class="echart-width-full"
        :datas="chartData"
        :axis-label-format="axisLabelFormat"
        :axis-pointer-label-formatter="axisPointerLabelFormatter"
        :tooltip-formatter="tooltipFormatter"
        :show-legend="false"
      />
    </q-tab-panel>

    <q-tab-panel :name="CHART_TYPES.DTW">
      <trend-chart
        class="echart-width-full"
        :datas="dtwChartData"
        :axis-label-format-function="(value) => `${value}`"
        :axis-pointer-label-formatter="dtwChartAxisPointerLabelFormatter"
        :tooltip-formatter="dtwChartTooltipFormatter"
        :show-legend="false"
      />
    </q-tab-panel>
  </q-tab-panels>
</template>

<script setup>
import TrendChart from 'components/chart/TrendChart.vue'
import { ref, watch } from 'vue'
import { formatDateTime, formatTime } from 'src/common/utils'
import { date } from 'quasar'
import ParameterDataService from 'src/services/parameterData/ParameterDataService'
import useUI from 'src/common/module/ui'

const CHART_TYPES = {
  NORMAL: 'normal',
  DTW: 'dtw',
}

const LABELS = {
  [CHART_TYPES.NORMAL]: '일반 차트',
  [CHART_TYPES.DTW]: 'DTW 차트',
}

const props = defineProps({
  /**
   * 차트 데이터를 검색하기 위한 조건입니다.
   * @type {Object}
   * @property {string} toolId - 선택된 설비의 ID입니다.
   * @property {string} parameterId - 선택된 파라미터의 ID입니다.
   * @property {string} recipeParameterId - 선택된 레시피 파라미터의 ID입니다.
   * @property {string} recipeName - 선택된 레시피의 이름입니다.
   * @property {Date} from - 검색 시작 날짜 및 시간입니다.
   * @property {Date} to - 검색 종료 날짜 및 시간입니다.
   */
  chartDataSearchCondition: {
    type: Object,
    required: true,
  },
})

const ui = useUI()

const chartDataSearchCondition = ref(props.chartDataSearchCondition)

const chartType = ref(CHART_TYPES.NORMAL)
const chartData = ref(null)

const dtwChartData = ref(null)

const axisLabelFormat = '{HH}:{mm}:{ss}'
const roundTo = (number, scale) => (Number.isInteger(number) ? number : parseFloat(number.toFixed(scale || 2)))

const axisPointerLabelFormatter = (params) => {
  const { axisDimension: dimension, value } = params
  if (dimension === 'x') {
    return formatTime(new Date(value))
  }
  return roundTo(value, 2)
}

const tooltipFormatter = (params) => {
  return params
    .map((param) => {
      const startDate = new Date(param.seriesName)
      const time = new Date(param.value[0])
      const tickTime = date.addToDate(startDate, {
        hours: time.getHours(),
        minutes: time.getMinutes(),
        seconds: time.getSeconds(),
        milliseconds: time.getMilliseconds(),
      })
      return `${formatDateTime(tickTime)}: ${param.value[1]}`
    })
    .join('<br/>')
}

const dtwChartAxisPointerLabelFormatter = (params) => {
  const { axisDimension: dimension, value } = params
  if (dimension === 'x') {
    return `${value}`
  }
  return roundTo(value, 2)
}

const dtwChartTooltipFormatter = (params) => {
  return params
    .map((param) => {
      return `${param.seriesName}: ${param.value[1]}`
    })
    .join('<br/>')
}

const fetchAndSetTrendData = async () => {
  const respData = await ParameterDataService.getRecipeTrendData(
    chartDataSearchCondition.value.toolId,
    chartDataSearchCondition.value.parameterId,
    chartDataSearchCondition.value.recipeParameterId,
    chartDataSearchCondition.value.recipeName,
    chartDataSearchCondition.value.from,
    chartDataSearchCondition.value.to,
  )

  chartData.value = respData.map((it) => {
    const date = new Date(it.start)
    const name = formatDateTime(date)

    return {
      name: name,
      trendData: it.items.map((item) => ({
        // item의 time에는 시분초 값만 있기에, 온전한 형태의 Date 객체를 만들기 위해 2000-01-01을 추가
        x: new Date(`2000-01-01 ${item.time}`).getTime(),
        y: item.value,
      })),
    }
  })
}

const fetchAndSetDtwChartData = async () => {
  const respData = await ParameterDataService.getRecipeDtwTrendData(
    chartDataSearchCondition.value.toolId,
    chartDataSearchCondition.value.parameterId,
    chartDataSearchCondition.value.recipeParameterId,
    chartDataSearchCondition.value.recipeName,
    chartDataSearchCondition.value.from,
    chartDataSearchCondition.value.to,
  )

  dtwChartData.value = respData.map((it) => {
    const date = new Date(it.start)
    const name = formatDateTime(date)

    return {
      name: name,
      trendData: it.values.map((value, index) => {
        return {
          x: index,
          y: value,
        }
      }),
    }
  })
}

const withLoading = async (callback) => {
  ui.loading.show()
  try {
    await callback()
  } finally {
    ui.loading.hide()
  }
}

const loadChartData = async (tabName) => {
  await withLoading(async () => {
    if (tabName === CHART_TYPES.DTW) {
      await fetchAndSetDtwChartData()
    } else if (tabName === CHART_TYPES.NORMAL) {
      await fetchAndSetTrendData()
    }
  })
}

const handleTabChange = async (tabName) => {
  await withLoading(async () => {
    if (tabName === CHART_TYPES.DTW && !dtwChartData.value) {
      await fetchAndSetDtwChartData()
    } else if (tabName === CHART_TYPES.NORMAL && !chartData.value) {
      await fetchAndSetTrendData()
    }
  })
}

watch(
  () => props.chartDataSearchCondition,
  async (newCondition) => {
    chartData.value = null
    dtwChartData.value = null
    chartDataSearchCondition.value = newCondition
    await loadChartData(chartType.value)
  },
  {
    immediate: true,
  },
)
</script>

<style scoped>
.chart-type-tab {
  height: 40px;
}

.chart-panel {
  height: calc(100% - 60px);
}
</style>
