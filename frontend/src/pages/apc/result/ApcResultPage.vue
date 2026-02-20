<template>
  <q-page class="q-pa-sm">
    <q-card class="cust_subPage">
      <q-card-section class="search_section_wrap">
        <q-form ref="form" class="search_form">
          <div class="row">
            <q-item v-for="item in apcConditions" :key="item.key" class="col-2 q-pa-xs">
              <q-input v-model="formData[item.key]" :label="item.name" />
            </q-item>
          </div>
          <div class="row">
            <q-item class="col-4 q-pa-xs">
              <DateTimeRangeSection v-model:from-date="searchPeriod.from" v-model:to-date="searchPeriod.to" />
            </q-item>
            <q-item>
              <q-checkbox class="q-pr-sm" v-model="isIncludeSimulation" :label="$t('시뮬레이션 포함')"></q-checkbox>
              <q-btn class="search_btn with_icon_btn sBtn" @click="onSearch">{{ $t('조회') }}</q-btn>
            </q-item>
          </div>
        </q-form>
      </q-card-section>
      <q-card-section class="col-auto">
        <simple-table
          :rows="rows"
          :columns="columns"
          color="amber"
          row-key="id"
          v-model:selected="selected"
          selection="single"
          @click="onClickRow"
        >
          <template v-slot:body="props">
            <q-tr class="cursor-pointer" :props="props" @click="props.selected = !props.selected">
              <q-td v-for="col in props.cols" :key="col.name" :props="props">
                <template v-if="col.name === 'formula'">
                  <q-icon v-if="col.value" name="calculate" color="green" size="md">
                    <q-tooltip class="formula-tooltip">
                      <Formula :workspace-json-string="col.value" />
                    </q-tooltip>
                  </q-icon>
                </template>
                <template v-else-if="col.name === 'status'">
                  <q-chip :color="APC_STATUS[col.value].color" text-color="white" :icon="APC_STATUS[col.value].icon">
                    <q-tooltip v-if="col.value === 'ERROR'" class="text-body1"
                      >{{ getErrorMsgByCode(props.row.errorCode) }}
                    </q-tooltip>
                    {{ $t(APC_STATUS[col.value].text) }}
                  </q-chip>
                </template>
                <template v-else-if="col.name === 'requestData'">
                  <q-chip color="teal" text-color="white">
                    {{ Object.keys(col.value).length + $t('건') }}
                    <q-tooltip class="text-body1" v-if="Object.keys(col.value).length !== 0">
                      <q-item v-for="key in Object.keys(col.value)" :key="key" class="input-data-item">
                        <span class="text-bold q-mr-md">{{ key }}</span> {{ col.value[key] }}
                      </q-item>
                    </q-tooltip>
                  </q-chip>
                </template>
                <template v-else-if="col.name === 'modelName'">
                  <div v-if="props.row.modelCondition" class="flex-center q-pt-xs">
                    <q-icon class="q-pb-xs" name="list_alt" color="grey-6" size="sm" />
                    <span>{{ col.value }}</span>
                    <q-tooltip class="text-body1">
                      <q-item
                        v-for="(key, i) in Object.keys(props.row.modelCondition)"
                        :key="key"
                        class="input-data-item"
                      >
                        <span class="text-bold q-mr-md">{{ apcConditions[i].name }}</span>
                        {{ props.row.modelCondition[key] }}
                      </q-item>
                    </q-tooltip>
                  </div>
                  <div v-else></div>
                </template>
                <template v-else>
                  {{ col.value }}
                </template>
              </q-td>
            </q-tr>
          </template>
        </simple-table>
      </q-card-section>
      <q-slide-transition>
        <q-card-section v-if="selected[0]?.status === 'SUCCESS'" class="q-mt-md result-table-section">
          <simple-table
            class="result-table"
            :title="$t('결과 데이터')"
            :columns="expandColumn"
            :rows="resultRows"
            :pagination="{
              rowsPerPage: 0,
            }"
            hide-pagination
          />
        </q-card-section>
      </q-slide-transition>
    </q-card>
  </q-page>
</template>

<script setup>
import DateTimeRangeSection from 'components/form/dateTime/DateTimeRangeSection.vue'
import { storeToRefs } from 'pinia'
import { useAPCStore } from 'src/stores/apc'
import { computed, ref } from 'vue'
// eslint-disable-next-line no-unused-vars
import { date } from 'quasar'
import { useI18n } from 'vue-i18n'
import Formula from 'pages/apc/model/Formula.vue'
import { apcConditionDelimiterToObject, DELIMITER, splitConditionAndInputData } from 'src/common/apc/apcUtil'
import useUI from 'src/common/module/ui'
import ApcResultService from 'src/services/apc/ApcResultService'
import { formatDateTime } from 'src/common/utils'
import { APC_STATUS, getErrorMsgByCode } from 'src/common/apc/apcConstant'

const apcStore = useAPCStore()
const { t } = useI18n()
const { apcConditions } = storeToRefs(apcStore)
const ui = useUI()

//const now = new Date()
const searchPeriod = ref({
  from: '2024-11-07 09:56',
  to: '2024-11-07 15:24',
})

// const now = new Date()
// const searchPeriod = ref({
//   from: date.subtractFromDate(now, { days: 7 }),
//   to: now,
// })

const defaultFormData = computed(() =>
  apcConditions.value.reduce((prev, curr) => ({ ...prev, [curr.key]: undefined }), {}),
)

const formData = ref(defaultFormData.value)
const rows = ref([])
const resultRows = ref([])
const selected = ref([])
const isIncludeSimulation = ref(true)

const columns = computed(() => [
  //Apc 조건으로 컬럼 동적 구성
  {
    name: 'createAt',
    align: 'left',
    label: t('일시'),
    field: 'createAt',
    sortable: true,
    format: (val) => formatDateTime(val),
  },
  ...apcConditions.value.map((it) => ({
    name: it.key,
    align: 'left',
    label: t(it.name),
    field: it.key,
    sortable: true,
  })),
  { name: 'status', align: 'center', label: t('상태'), field: 'status', sortable: true },
  { name: 'requestData', align: 'center', label: t('데이터'), field: 'requestData', sortable: true },
  { name: 'modelName', align: 'center', label: t('모델명'), field: 'modelName', sortable: true },
  { name: 'modelVersion', align: 'center', label: t('버전'), field: 'modelVersion', sortable: true },
  { name: 'formula', align: 'center', label: t('계산식'), field: 'formula', sortable: true },
])

const expandColumn = [
  { name: 'resultKey', align: 'left', label: t('키'), field: 'resultKey' },
  { name: 'resultValue', align: 'left', label: t('값'), field: 'resultValue' },
]

const onSearch = async () => {
  const condition = apcConditions.value.map((cond) => formData.value[cond.key] || '*').join(DELIMITER)
  const from = searchPeriod.value.from
  const to = searchPeriod.value.to

  ui.loading.show()
  const res = await ApcResultService.getApcRequestStatusList(condition, from, to, isIncludeSimulation.value)
  ui.loading.hide()
  if (!res?.length) {
    ui.notify.warning(t('조회조건에 맞는 데이터가 없습니다.'))
    rows.value = []
    selected.value = []
    return
  }

  rows.value = await Promise.all(
    res.map(async (item) => {
      const { condition, inputData } = await splitConditionAndInputData(item.requestMap)
      return {
        id: item.id,
        formula: item.formula,
        modelVersion: item.modelVersion,
        status: item.status,
        createAt: item.createAt,
        requestData: inputData,
        errorCode: item.errorCode,
        modelName: item.modelName,
        modelCondition:
          item.modelCondition !== null ? await apcConditionDelimiterToObject(item.modelCondition) : item.modelCondition,
        ...condition,
      }
    }),
  )
  selected.value = []
}

const onClickRow = async () => {
  if (!selected.value[0] || selected.value[0].status !== 'SUCCESS') return
  resultRows.value = await ApcResultService.getApcResultData(selected.value[0].id)
}
</script>

<style lang="scss" scoped>
.eco-mainContainer .q-card__section.search_section_wrap {
  height: auto;
}

.formula-tooltip {
  overflow: visible;
  min-width: 0;
  min-height: 0;
  padding: 0;
}

.result-table-section {
  width: 50% !important;
  background-color: #fff;
  border-radius: 4px !important;
  border: 1px solid #ddd !important;
}

.input-data-item {
  min-height: auto;
  padding-top: 5px;
  padding-bottom: 5px;
}
</style>
