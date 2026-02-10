<template>
  <q-card class="cust_subPage">
    <q-card-section class="search_section_wrap">
      <q-form ref="form" class="search_form">
        <div class="row">
          <q-item v-for="item in apcConditions" class="col-2 q-px-xs" :key="item.key">
            <q-input
              v-model="modelInfo[item.key]"
              :label="item.name"
              :rules="[$rules.required]"
              class="input-required"
              disable
            />
          </q-item>
          <q-item class="q-px-xs">
            <q-icon class="cursor-pointer" name="list" color="green" size="md" @click="showModelPopUp" />
          </q-item>
        </div>
        <div class="row">
          <q-item class="col-2 q-px-xs">
            <q-input
              v-model="modelInfo.modelName"
              :label="$t('모델명')"
              :rules="[$rules.required]"
              class="input-required"
              disable
            />
          </q-item>
          <q-item class="col-2 q-px-xs">
            <filterable-select
              ref="versionSelect"
              v-model="modelInfo.version"
              :label="$t('버전')"
              :options="modelVersions"
              :disable="!formulaWorkspace"
              option-value="id"
              option-label="version"
              :rules="[$rules.required]"
              class="input-required"
              emit-value
              map-options
            >
              <template v-slot:option="props">
                <q-item clickable @click="selectVersion(props.opt)">
                  <q-item-section>{{ props.label }}</q-item-section>
                  <description-tooltip :description="props.opt.description" />
                </q-item>
              </template>
            </filterable-select>
          </q-item>
          <q-item class="q-px-xs">
            <q-icon name="calculate" color="green" size="md">
              <q-tooltip v-if="formulaWorkspace.workspace" class="formula-tooltip">
                <Formula :workspace-json-string="formulaWorkspace.workspace"></Formula>
              </q-tooltip>
            </q-icon>
          </q-item>
          <q-item class="col-4 q-px-xs">
            <DateTimeRangeSection v-model:from-date="searchPeriod.from" v-model:to-date="searchPeriod.to" />
          </q-item>
          <q-item>
            <q-checkbox class="q-pr-sm" v-model="isIncludeSimulation" :label="$t('시뮬레이션 포함')"></q-checkbox>
            <q-btn class="search_btn with_icon_btn sBtn" @click="loadApcResultTrend">{{ $t('조회') }}</q-btn>
          </q-item>
        </div>
      </q-form>
      <apc-model-list-popup v-if="isShowModelPopup" v-model="isShowModelPopup" @select-value="onSelectApcValue" />
    </q-card-section>
    <q-card-section class="column col">
      <div class="btn-table-view" v-if="isShowTrendChart">
        <q-btn dense @click="toggleTable" icon="table_chart" class="sBtn float-right">
          <span class="q-pl-sm">{{ isTableView === true ? $t('테이블 닫기') : $t('테이블 보기') }}</span>
        </q-btn>
      </div>
      <div class="q-ma-none column col">
        <trend-chart v-if="isShowTrendChart" :datas="trendChartData" :mouse-zoom="false" class="echart-width-full" />
      </div>
    </q-card-section>
  </q-card>

  <q-card class="q-mt-md" v-if="isTableView">
    <q-slide-transition>
      <q-card-section>
        <div ref="table">
          <simple-table :rows="rows" :columns="columns" color="amber">
            <template v-slot:header="props">
              <q-tr class="bordered bg-teal-2">
                <q-th rowspan="2" class="bordered">{{ props.cols[0].label }}</q-th>
                <q-th :colspan="columnData.input.length" class="bordered">Input</q-th>
                <q-th :colspan="columnData.output.length" class="bordered"> Output</q-th>
              </q-tr>
              <q-tr :props="props">
                <q-th v-for="col in props.cols.slice(1)" :key="col.name" :props="props" class="bordered bg-teal-2">
                  {{ col.label }}
                </q-th>
              </q-tr>
            </template>
          </simple-table>
        </div>
      </q-card-section>
    </q-slide-transition>
  </q-card>
</template>

<script setup>
import { computed, nextTick, ref } from 'vue'
import { storeToRefs } from 'pinia'
import { date } from 'quasar'
import { useI18n } from 'vue-i18n'
import useUI from 'src/common/module/ui'
import { useAPCStore } from 'src/stores/apc'
import DateTimeRangeSection from 'src/components/form/dateTime/DateTimeRangeSection.vue'
import ApcModelListPopup from 'pages/apc/result/ApcModelListPopup.vue'
import ApcResultService from 'src/services/apc/ApcResultService'
import TrendChart from 'components/chart/TrendChart.vue'
import { formatDateTime } from 'src/common/utils'
import { splitConditionAndInputData } from 'src/common/apc/apcUtil'
import ApcModelService from 'src/services/apc/ApcModelService'
import DescriptionTooltip from 'components/common/DescriptionTooltip.vue'
import Formula from 'pages/apc/model/Formula.vue'

const { t } = useI18n()
const ui = useUI()
const apcStore = useAPCStore()
const { apcConditions } = storeToRefs(apcStore)

const now = new Date()
const searchPeriod = ref({
  from: date.subtractFromDate(now, { days: 7 }),
  to: now,
})

const form = ref(null)
const isShowModelPopup = ref(false)
const modelInfo = ref({})
const modelVersions = ref([])
const isShowTrendChart = ref(false)
const isTableView = ref(false)
const isIncludeSimulation = ref(false)
const versionSelect = ref(null)
const rows = ref({})
const table = ref()

const inputPostfix = '(input)'
const outputPostfix = '(output)'

const columns = computed(() => [
  { name: 'createAt', align: 'center', label: t('일시'), field: 'createAt', format: (val) => formatDateTime(val) },

  ...columnData.value.input.map((it) => ({
    name: it,
    label: it.replace(inputPostfix, ''),
    field: it,
  })),
  ...columnData.value.output.map((it) => ({
    name: it,
    label: it.replace(outputPostfix, ''),
    field: it,
  })),
])

const loadApcResultTrend = async () => {
  if (modelInfo.value.version === undefined) {
    ui.notify.warning(t('필수값이 입력되지 않았습니다.'))
    return
  }
  const trendResultDatas = await getResultParameterTrendData()
  if (trendResultDatas.length === 0) {
    ui.notify.warning(t('조회된 데이터가 없습니다'))
    clearChart()
    return
  }
  await createRowData(trendResultDatas)
  isShowTrendChart.value = true
  isTableView.value = false
}
const columnData = computed(() => {
  const input = Object.keys(rows.value[0]).filter((it) => it.includes('(input)'))
  const output = Object.keys(rows.value[0]).filter((it) => it.includes('(output)'))
  return { input, output }
})

const createRowData = async (trendResultDatas) => {
  const inputPostfix = '(input)'
  const outputPostfix = '(output)'

  rows.value = (
    await Promise.all(
      trendResultDatas.map(async (trendResultData) => {
        const splitTrendResultData = await splitConditionAndInputData(trendResultData.requestData)
        const inputChartData = splitTrendResultData.inputData
        const outputChartData = trendResultData.requestResult
        const requestData = Object.keys(inputChartData).reduce((prev, curr) => {
          prev[curr + inputPostfix] = inputChartData[curr]
          return prev
        }, {})
        const resultData = Object.keys(outputChartData).reduce((prev, curr) => {
          prev[curr + outputPostfix] = outputChartData[curr]
          return prev
        }, {})
        return {
          createAt: trendResultData.createAt,
          ...requestData,
          ...resultData,
        }
      }),
    )
  ).sort((a, b) => a.createAt.localeCompare(b.createAt))
}
const trendChartData = computed(() => {
  const reduce = rows.value?.reduce((prev, curr) => {
    const dataKeys = Object.keys(curr).filter((key) => key !== 'createAt')
    dataKeys.forEach((dataKey) => {
      if (prev[dataKey] === undefined) {
        prev[dataKey] = {
          name: dataKey,
          trendData: [{ x: curr.createAt, y: curr[dataKey] }],
        }
      } else {
        prev[dataKey].trendData.push({ x: curr.createAt, y: curr[dataKey] })
      }
    })
    return prev
  }, {})
  return Object.keys(reduce).map((key) => reduce[key])
})

const getResultParameterTrendData = async () => {
  ui.loading.show()
  const trendResultDatas = await ApcResultService.getResultParameterTrendData(
    modelInfo.value.version,
    searchPeriod.value.from,
    searchPeriod.value.to,
    isIncludeSimulation.value,
  )
  ui.loading.hide()
  return trendResultDatas
}

const onSelectApcValue = (apcModelValue) => {
  clearChart()
  setInputFieldValue(apcModelValue)
  loadApcModelVersion(apcModelValue.apcModelId)
}

const setInputFieldValue = async (apcModelValue) => {
  const splitApcModelValue = await splitConditionAndInputData(apcModelValue)
  modelInfo.value = {
    ...splitApcModelValue.condition,
    version: splitApcModelValue.inputData.versionId,
    modelName: splitApcModelValue.inputData.modelName,
  }
}

const clearChart = () => {
  rows.value = []
  trendChartData.value = []
  isShowTrendChart.value = false
  isTableView.value = false
}

const loadApcModelVersion = async (apcModelId) => {
  const apcModelVersions = await ApcModelService.getModelVersionsByModelId(apcModelId)
  modelVersions.value = apcModelVersions.map((apcModelVersion) => {
    return {
      id: apcModelVersion.id,
      version: apcModelVersion.isActive ? apcModelVersion.version + ' (Active)' : apcModelVersion.version,
      workspace: apcModelVersion.workspace,
      description: apcModelVersion.description,
    }
  })
}

const formulaWorkspace = computed(() => {
  const modelVersion = filteredModelVersion(modelInfo.value?.version)

  return {
    workspace: modelVersion?.workspace,
    description: modelVersion?.description,
  }
})

const filteredModelVersion = (versionId) => {
  return modelVersions.value.find((value) => value.id === versionId)
}

const toggleTable = () => {
  isTableView.value = !isTableView.value
  if (isTableView.value) {
    moveScroll()
  }
}
const moveScroll = () => {
  nextTick(() => {
    table.value.scrollIntoView({ behavior: 'smooth' })
  })
}
const selectVersion = (option) => {
  modelInfo.value.version = option.id
  if (versionSelect.value && versionSelect.value.hidePopup) {
    versionSelect.value.hidePopup()
  }
}

const showModelPopUp = () => {
  isShowModelPopup.value = true
}
</script>
<style lang="scss" scoped>
.q-card__section.search_section_wrap {
  height: auto;
}

:deep(.q-table) {
  border-collapse: collapse !important; /* 보더가 겹치지 않도록 설정 */
}

.bordered {
  border: 1px solid black !important;
}

.formula-tooltip {
  overflow: visible;
  min-width: 0;
  min-height: 0;
  padding: 0;
}
</style>
