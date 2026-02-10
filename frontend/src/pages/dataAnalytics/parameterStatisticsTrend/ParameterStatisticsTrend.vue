<template>
  <q-page class="q-pa-sm">
    <q-card class="cust_subPage">
      <q-card-section class="row search_section_wrap">
        <q-item class="col-11 search_left grid-0504-full">
          <DateTimeRangeSection v-model:from-date="fromDateTime" v-model:to-date="toDateTime" />
          <q-item-section>
            <ToolSelectBox v-model="selectedTool" @update:modelValue="handleUpdateToolSelected" />
          </q-item-section>
          <q-item-section>
            <ParameterSelectBox
              v-model="selectedParameters"
              :tool-id="selectedToolId"
              :type="[PARAMETER_TYPE.TRACE]"
              :dataTypes="[PARAMETER_DATA_TYPE.NUMERIC_TYPES]"
              max-values="5"
              multiple
            />
          </q-item-section>
          <q-item-section>
            <filterable-select
              outlined
              v-model="selectedDataType"
              :options="summaryDataKinds"
              option-value="id"
              :option-label="(item) => $t(item.name)"
              :label="$t('데이터')"
            >
              <template v-slot:prepend>
                <q-icon name="analytics" />
              </template>
            </filterable-select>
          </q-item-section>
        </q-item>
        <q-item class="col-1 search_right">
          <q-item-section>
            <q-btn class="search_btn with_icon_btn sBtn" @click="onSearch">{{ $t('조회') }}</q-btn>
          </q-item-section>
        </q-item>
      </q-card-section>
      <q-card-section class="column col flex">
        <div class="column col chart-area">
          <v-chart
            resizable
            autoresize
            ref="summaryChart"
            :option="summaryChartOption"
            @Datazoom="handleDatazoom"
            @Finished="handleRenderFinished(CHART_TYPE.SUMMARY)"
          />
          <div class="summary-label" v-if="summaryLabel">{{ summaryLabel }}</div>
        </div>

        <div v-show="rawChartOptions" class="column col chart-area">
          <v-chart
            resizable
            autoresize
            ref="rawChart"
            :option="rawChartOptions"
            @Finished="handleRenderFinished(CHART_TYPE.RAW)"
          />
          <div class="summary-label" v-if="rawChartOptions">{{ t('원시 데이터') }}</div>
        </div>
      </q-card-section>
    </q-card>
  </q-page>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { date } from 'quasar'
import useUI from 'src/common/module/ui'
import { pt, t } from 'src/plugins/i18n'
import { DATE_TIME_FORMAT } from 'src/common/constant/format'
import VChart from 'vue-echarts'
import { BarChart, LineChart } from 'echarts/charts'
import { CanvasRenderer } from 'echarts/renderers.js'
import {
  DataZoomComponent,
  GridComponent,
  LegendComponent,
  TitleComponent,
  ToolboxComponent,
  TooltipComponent,
} from 'echarts/components'
import { use } from 'echarts/core'
import SummaryDataService from 'src/services/parameterData/SummaryDataService'
import MetaDataService from 'src/services/modeling/MetaDataService'
import { PARAMETER_DATA_TYPE, PARAMETER_TYPE } from 'src/common/constant/parameter'
import { getColor } from 'src/common/constant/chart'
import DateTimeRangeSection from 'components/form/dateTime/DateTimeRangeSection.vue'
import ToolSelectBox from 'components/form/ToolSelectBox.vue'
import ParameterSelectBox from 'components/form/ParameterSelectBox.vue'

const CHART_TYPE = {
  SUMMARY: 'summary',
  RAW: 'raw',
}
const GRID_WIDTH_PERCENT = 90
const CHART_BASE_OPTION = {
  legend: {
    orient: 'horizontal',
    icon: 'rect',
  },
  title: {
    text: '',
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'cross',
      animation: false,
      label: {
        backgroundColor: '#ccc',
        borderColor: '#aaa',
        borderWidth: 1,
        shadowBlur: 0,
        shadowOffsetX: 0,
        shadowOffsetY: 0,
        color: '#222',
      },
    },
  },
  toolbox: {
    feature: {
      dataZoom: {
        yAxisIndex: 'none',
        brushStyle: {
          color: 'rgba(0, 0, 0, 0.4)',
        },
        icon: {
          zoom: 'path://',
          back: 'svg://M15.5,14h-.79l-.28-.27C15.41,12.59,16,11.11,16,9.5,16,5.91,13.09,3,9.5,3S3,5.91,3,9.5,5.91,16,9.5,16c1.61,0,3.09-.59,4.23-1.57l.27.28v.79l5,4.99L20.49,19l-4.99-5zm-6,0C7.01,14,5,11.99,5,9.5S7.01,5,9.5,5,14,7.01,14,9.5,11.99,14,9.5,14zM7,9h5v1H7z',
        },
      },
    },
  },
  grid: [{ top: 30, bottom: 40, left: '5%', right: '5%', width: `${GRID_WIDTH_PERCENT}%` }],
  dataZoom: [
    {
      type: 'inside',
      zoomOnMouseWheel: false,
      xAxisIndex: 0,
      filterMode: 'filter',
    },
  ],
  xAxis: {
    type: 'time',
    axisLabel: {
      formatter: (value) => date.formatDate(value, DATE_TIME_FORMAT),
    },
  },
  yAxis: [
    {
      type: 'value',
    },
    {
      show: false,
      type: 'value',
    },
  ],
  series: [],
}

use([
  LineChart,
  BarChart,
  CanvasRenderer,
  LegendComponent,
  TooltipComponent,
  GridComponent,
  DataZoomComponent,
  ToolboxComponent,
  TitleComponent,
])

const ui = useUI()

const fromDateTime = ref()
const toDateTime = ref()

const summaryDataKinds = ref([])

const selectedDataType = ref(null)
const selectedTool = ref(null)
const selectedParameters = ref([])

const summaryChart = ref(null)
const summaryChartOption = ref(CHART_BASE_OPTION)

const rawChart = ref(null)
const rawChartOptions = ref(null)

const zoomHistory = ref([])

const summaryType = ref(null)
const summaryLabel = computed(() => {
  if (!summaryType.value) {
    return null
  }
  return t('요약시간 단위') + ' : ' + t(summaryType.value)
})

const selectedToolId = computed(() => {
  return selectedTool.value ? selectedTool.value.id : null
})

const handleRenderFinished = (chartType) => {
  const zoomActiveDispatch = {
    type: 'takeGlobalCursor',
    key: 'dataZoomSelect',
    dataZoomSelectActive: true,
  }

  if (chartType === CHART_TYPE.SUMMARY) {
    summaryChart.value.dispatchAction(zoomActiveDispatch)
  } else {
    rawChart.value.dispatchAction(zoomActiveDispatch)
  }
}

const getSummaryData = async (from, to) => {
  return await SummaryDataService.getTrendData(
    from,
    to,
    selectedParameters.value.map((v) => v.id),
    getChartWidth(),
    selectedDataType.value.id,
  )
}

const checkIsRollbackZoom = (batch, zoomHistory) => {
  if (zoomHistory.length === 1) {
    return false
  }

  const { start, end } = batch
  if (start === 0 && end === 100) {
    return true
  }

  const { startValue, endValue } = batch
  const lastHistory = zoomHistory[zoomHistory.length - 1]

  return (
    new Date(startValue).getTime() <= new Date(lastHistory.from).getTime() ||
    new Date(endValue).getTime() >= new Date(lastHistory.to).getTime()
  )
}

const getChartWidth = () => {
  return parseInt(summaryChart.value.getWidth() * (GRID_WIDTH_PERCENT * 0.01))
}

const checkIgnoreZoom = (batch, zoomHistoryLength) => {
  const { start, end } = batch
  return start === 0 && end === 100 && zoomHistoryLength === 1
}

const handleDatazoom = async (event) => {
  const batch = event.batch[0]
  if (checkIgnoreZoom(batch, zoomHistory.value.length)) {
    return
  }

  const { startValue, endValue } = batch
  const lastHistory = zoomHistory.value[zoomHistory.value.length - 1]

  const isRollbackZoom = checkIsRollbackZoom(batch, zoomHistory.value)
  const isNotRollbackZoom = isRollbackZoom === false

  // 줌 롤백이 아니고, 현재 차트의 데이터가 raw 데이터인 경우
  if (isNotRollbackZoom && lastHistory.type === 'raw') {
    // 줌 데이터도 raw 데이터이므로, zoomHistory에 추가만 하고 return
    zoomHistory.value = [
      ...zoomHistory.value,
      {
        type: 'raw',
        prevType: 'raw',
        from: new Date(startValue).toISOString(),
        to: new Date(endValue).toISOString(),
      },
    ]
    return
  }

  // 줌 롤백을 하는데, 마지막 히스토리의 부모가 raw 데이터인 경우.
  if (isRollbackZoom && lastHistory.prevType === 'raw') {
    // 가장 마지막 히스토리만 지우고, 롤백은 echart에 맡긴다
    zoomHistory.value = zoomHistory.value.filter((v, index) => index !== zoomHistory.value.length - 1)
    return
  }

  const instance = summaryChart.value.chart
  instance.showLoading()

  let result
  let min
  let max

  if (isRollbackZoom) {
    const lastHistoryParent = zoomHistory.value[zoomHistory.value.length - 2]
    min = lastHistoryParent.from
    max = lastHistoryParent.to

    result = await getSummaryData(new Date(lastHistoryParent.from), new Date(lastHistoryParent.to))
    zoomHistory.value = zoomHistory.value.filter((v, index) => index !== zoomHistory.value.length - 1)
  } else {
    const from = new Date(startValue)
    const to = new Date(endValue)
    min = from.toISOString()
    max = to.toISOString()

    result = await getSummaryData(from, to)
    zoomHistory.value = [
      ...zoomHistory.value,
      {
        type: result.type,
        prevType: lastHistory.type,
        from: from.toISOString(),
        to: to.toISOString(),
      },
    ]
  }

  summaryType.value = result.type
  updateChartOptions(min, max, result.parameters)
  instance.hideLoading()
}

const loadSummaryDataKinds = async () => {
  const summaryDataKindsResp = await MetaDataService.getSummaryDataKinds()
  summaryDataKinds.value = summaryDataKindsResp.items

  if (summaryDataKinds.value.length > 0) {
    selectedDataType.value = summaryDataKinds.value[0]
  }
}

const handleUpdateToolSelected = async () => {
  selectedParameters.value = []
}

const updateChartOptions = (xMinIsoString, xMaxIsoString, parameterDataList) => {
  summaryChartOption.value.xAxis = {
    ...summaryChartOption.value.xAxis,
    min: xMinIsoString,
    max: xMaxIsoString,
  }

  const chartColors = parameterDataList.reduce((prev, curr, index) => {
    prev[curr.name] = getColor(index)
    return prev
  }, {})

  summaryChartOption.value.series = parameterDataList.map((v) => ({
    type: 'line',
    showSymbol: false,
    largeThreshold: 1000,
    emphasis: {
      disabled: true,
    },
    itemStyle: {
      color: chartColors[v.name],
    },
    name: pt(v.name),
    data: v.items.map((v) => [v.time, v.value]),
  }))

  const existRawData = parameterDataList.some((it) => it.rawItems.length !== 0)
  if (!existRawData) {
    rawChartOptions.value = null
    return
  }

  if (existRawData) {
    const rawSeries = parameterDataList
      .filter((it) => it.rawItems.length > 0)
      .map((v) => ({
        type: 'line',
        showSymbol: false,
        largeThreshold: 1000,
        emphasis: {
          disabled: true,
        },
        itemStyle: {
          color: chartColors[v.name],
        },
        name: `${pt(v.name)}`,
        data: v.rawItems.map((v) => [v.time, v.value]),
      }))

    rawChartOptions.value = {
      ...CHART_BASE_OPTION,
      xAxis: {
        ...CHART_BASE_OPTION.xAxis,
        min: xMinIsoString,
        max: xMaxIsoString,
      },
      series: rawSeries,
    }
  }
}

const onSearch = async () => {
  if (selectedParameters.value.length === 0) {
    ui.notify.warning(t('파라미터 값은 필수입니다!'))
    return
  }

  const from = new Date(fromDateTime.value)
  const to = new Date(toDateTime.value)

  ui.loading.show()
  summaryChart.value.clear()
  const summaryData = await getSummaryData(from, to)
  ui.loading.hide()

  const isAllEmpty = !summaryData.parameters.some((it) => it.items.length > 0)
  if (isAllEmpty) {
    ui.notify.warning(t('조회조건에 맞는 데이터가 없습니다.'))
    return
  }

  zoomHistory.value = [
    {
      type: summaryData.type,
      prevType: null,
      from: from.toISOString(),
      to: to.toISOString(),
    },
  ]
  summaryType.value = summaryData.type
  updateChartOptions(from.toISOString(), to.toISOString(), summaryData.parameters)
}

onMounted(async () => {
  await loadSummaryDataKinds()
})
</script>

<style lang="scss">
.chart-area {
  position: relative;

  .summary-label {
    position: absolute;
    right: 40px;
    top: -4px;
    padding: 4px 10px;
    border-radius: 4px;
    background-color: var(--mainColor);
    color: #fff;
  }
}
</style>
