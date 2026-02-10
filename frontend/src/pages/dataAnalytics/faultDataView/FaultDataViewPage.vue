<template>
  <q-page class="q-pa-sm">
    <q-card class="cust_subPage">
      <q-card-section class="row search_section_wrap">
        <q-item class="col-6 search_left">
          <DateTimeRangeSection v-model:from-date="fromDateTime" v-model:to-date="toDateTime" />
          <q-item-section>
            <filterable-select
              outlined
              v-model="selectedTool"
              :options="toolOptions"
              option-value="id"
              option-label="name"
              :label="$t('설비')"
            >
              <template v-slot:prepend>
                <q-icon name="construction" />
              </template>
            </filterable-select>
          </q-item-section>
        </q-item>
        <q-item class="col-6 search_right">
          <q-item-section>
            <q-btn class="search_btn with_icon_btn sBtn" @click="onSearch">{{ $t('조회') }}</q-btn>
          </q-item-section>
        </q-item>
      </q-card-section>
      <q-card-section>
        <div style="height: calc(100vh - 220px)" class="row">
          <div class="col-7 full-height">
            <bar-chart :datas="trendData" @clickChart="handleBarChartClick" />
          </div>
          <div class="col-5 column full-height">
            <q-card class="col full-height full-width" bordered flat>
              <q-card-section class="full-height full-width">
                <fault-data-table
                  v-if="faultDataTableProps.parameterId"
                  :key="faultDataTableProps.parameterId"
                  :to="faultDataTableProps.to"
                  :from="faultDataTableProps.from"
                  :parameter-id="faultDataTableProps.parameterId"
                  :parameter-name="faultDataTableProps.parameterName"
                  @row-click="handleTableRowClick"
                />
              </q-card-section>
            </q-card>
          </div>
        </div>
      </q-card-section>
    </q-card>
  </q-page>

  <raw-data-chart-dialog
    :selected-fault-row="selectedFaultRow"
    :selected-tool="selectedTool"
    @hide="handleDialogHide"
  />
</template>

<script setup>
import { onMounted, ref } from 'vue'
import ToolService from 'src/services/modeling/ToolService.js'
import { date } from 'quasar'
import BarChart from 'src/components/chart/BarChart.vue'
import useUI from 'src/common/module/ui'
import { pt, t } from 'src/plugins/i18n'
import FaultHistoryService from 'src/services/parameterData/FaultHistoryService'
import FaultDataTable from 'pages/dataAnalytics/faultDataView/FaultDataTable.vue'
import RawDataChartDialog from 'pages/dataAnalytics/faultDataView/RawDataChartDialog.vue'
import DateTimeRangeSection from 'components/form/dateTime/DateTimeRangeSection.vue'

const ui = useUI()

const now = new Date()

const fromDateTime = ref(date.subtractFromDate(Date.now(), { days: 7 }))
const toDateTime = ref(now)
const toolOptions = ref([])
const selectedTool = ref(null)
const trendData = ref([])
const selectedFaultRow = ref(null)

const parameterNameIdMap = ref(new Map())
const faultDataTableProps = ref({
  parameterId: null,
  parameterName: null,
  to: null,
  from: null,
})

const loadTools = async () => {
  toolOptions.value = await ToolService.getTools()
  if (toolOptions.value.length > 0) {
    selectedTool.value = toolOptions.value[0]
  }
}

const handleBarChartClick = async (params) => {
  const { name: parameterName } = params
  const parameterId = parameterNameIdMap.value.get(parameterName)
  const from = new Date(fromDateTime.value)
  const to = new Date(toDateTime.value)

  faultDataTableProps.value = {
    parameterId,
    parameterName,
    from,
    to,
  }
}

const onSearch = async () => {
  if (selectedTool.value == null) {
    ui.notify.warning(t('설비 값은 필수입니다!'))
    return
  }

  const toolId = selectedTool.value.id
  const from = new Date(fromDateTime.value)
  const to = new Date(toDateTime.value)

  const { items } = await FaultHistoryService.getToolParametersFaultCount(toolId, from, to)
  trendData.value = items.map((item) => ({
    category: pt(item.parameterName),
    value: item.count,
  }))

  parameterNameIdMap.value = new Map(items.map((item) => [pt(item.parameterName), item.parameterId]))
  faultDataTableProps.value = {}
}

const handleTableRowClick = (row) => {
  selectedFaultRow.value = row
}

const handleDialogHide = () => {
  selectedFaultRow.value = null
}

onMounted(() => {
  loadTools()
})
</script>
