<template>
  <q-card class="row none_border">
    <q-card-section class="col full-height overflow-auto chart-body q-pa-none q-bt" v-if="isMainChart">
      <div>
        <ParameterReportBoxPlotChart
          :period-type="periodType"
          :from-date="fromDate"
          :to-date="toDate"
          :parameter-ids="parameterIds"
          :checked-parameter-ids="checkedParameterIds"
          @click:series="updateChart"
        />
      </div>
      <!-- <q-separator /> -->
      <div ref="subChart">
        <ParameterReportBoxPlotChart
          v-if="isSubChart"
          :period-type="subPeriodType"
          :from-date="subFromDate"
          :to-date="subToDate"
          :parameter-ids="parameterIds"
          :checked-parameter-ids="checkedParameterIds"
          @click:series="convertTrendChartData"
        />
      </div>
      <!-- <q-separator /> -->
      <div ref="trendChart">
        <ParameterReportTrendChart
          v-if="isTrendChart"
          :line-date="lineDate"
          :parameter-ids="parameterIds"
          :checked-parameter-ids="checkedParameterIds"
        />
      </div>
    </q-card-section>
    <q-card-section class="col flex-center" v-else>
      <!-- <q-icon name="mdi-information" size="64px" color="primary" /> -->
      <i><img src="../../../../public/img/widgets/ecotwin-icon/icon-alert-circle.svg" alt="alert" /></i>
      <div class="text-h5 text-center">
        {{ $t('리포트 조회') }}
      </div>
      <div class="text-body-2">
        {{ $t('조회조건을 설정한 후 조회 버튼을 클릭하여 조회해 주세요.') }}
      </div>
    </q-card-section>
  </q-card>
</template>

<script setup>
import { defineProps, nextTick, ref, watch } from 'vue'
import { scroll } from 'quasar'
import ParameterReportTrendChart from 'pages/waterQuality/parameterReport/ParameterReportTrendChart.vue'
import ParameterReportBoxPlotChart from 'pages/waterQuality/parameterReport/ParameterReportBoxPlotChart.vue'

const isMainChart = ref(false)
const isSubChart = ref(false)
const isTrendChart = ref(false)

const parameterIds = ref()
const subToDate = ref()
const subFromDate = ref()
const subPeriodType = ref()
const lineDate = ref()

const subChart = ref(null)
const trendChart = ref(null)
const { getScrollTarget, setVerticalScrollPosition } = scroll

const props = defineProps({
  parameters: {
    type: Array,
    required: true,
    default: () => [],
  },
  checkedParameterIds: {
    type: Array,
    required: false,
  },
  toDate: {
    type: Date,
    required: true,
  },
  fromDate: {
    type: Date,
    required: true,
  },
  periodType: {
    type: String,
    validator(value) {
      return ['DAILY', 'HOURLY'].includes(value)
    },
  },
})

watch(
  () => props.parameters,
  () => {
    getChartData()
  },
  { deep: true },
)

const clearChart = () => {
  isTrendChart.value = false
  isSubChart.value = false
}

const convertTrendChartData = (data) => {
  parameterIds.value = data.parameterIds
  lineDate.value = data.toDate
  isTrendChart.value = true
  moveScroll(trendChart.value)
}

const updateChart = (param) => {
  isTrendChart.value = false
  const periodTypeDaily = 'DAILY'
  if (props.periodType === periodTypeDaily) {
    parameterIds.value = param.parameterIds
    subPeriodType.value = param.periodType
    subFromDate.value = param.fromDate
    subToDate.value = param.toDate
    isSubChart.value = true
    moveScroll(subChart.value)
  } else {
    param.lineDate = param.toDate
    convertTrendChartData(param)
  }
}

const moveScroll = (chart) => {
  nextTick(() => {
    const target = getScrollTarget(chart)
    const offset = chart.offsetTop
    const duration = 300
    setVerticalScrollPosition(target, offset, duration)
  })
}

const getChartData = () => {
  clearChart()
  parameterIds.value = props.parameters.map((parameter) => parameter.id)
  isMainChart.value = true
}
</script>

<style lang="scss" scoped>
.chart-body {
  max-height: calc(100vh - 200px);
  & > div + div {
    border-top: 1px dashed #ddd;
  }
  &:deep .q-table__container {
    margin-top: 10px;
    border-top: 1px solid #ddd;
  }
}

.flex-center {
  height: 80vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 4px;
  i {
    width: 40px;
    height: 40px;
    margin-bottom: 10px;
    img {
      width: 100%;
      height: 100%;
    }
  }
  div {
    color: var(--textColor);
  }
}
</style>
