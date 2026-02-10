<template>
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
            <q-btn class="search_btn with_icon_btn sBtn" @click="onSearch">{{ $t('조회') }}</q-btn>
          </q-item>
        </div>
      </q-form>
    </q-card-section>
    <q-card-section class="col-auto">
      <simple-table
        ref="tableRef"
        bordered
        :rows="rows"
        :columns="columns"
        row-key="name"
        color="amber"
        selection="single"
        v-model:selected="selected"
      >
        <template v-slot:body="props">
          <q-tr :props="props">
            <q-td v-for="col in props.cols" :key="col.name" :props="props">
              <template v-if="col.name === 'createAt'">
                {{ $utils.formatDateTime(col.value) }}
              </template>
              <template v-else-if="col.name === 'formula'">
                <q-icon name="calculate" color="green" size="md">
                  <q-tooltip v-if="!!col.value" class="formula-tooltip">
                    <Formula :workspace-json-string="col.value" />
                  </q-tooltip>
                </q-icon>
              </template>
              <template v-else-if="col.name === 'status'">
                <q-chip
                  class="cursor-pointer"
                  clickable
                  @click.stop="onStatusClick(props.row)"
                  :color="APC_STATUS[col.value].color"
                  text-color="white"
                  :icon="APC_STATUS[col.value].icon"
                >
                  {{ $t(APC_STATUS[col.value].text) }}
                </q-chip>
              </template>
              <template v-else-if="col.name === 'dataCount'">
                <div v-if="col.value > 0" class="cursor-pointer" @click="showConfirmPopup(props.row)">
                  <u>{{ col.value }}</u>
                </div>
                <div v-else>{{ col.value }}</div>
              </template>
              <template v-else-if="col.name === 'createBy'">
                <user-avatar :user-id="col.value" />
              </template>
              <template v-else>
                {{ col.value }}
              </template>
            </q-td>
          </q-tr>
        </template>
      </simple-table>
    </q-card-section>
  </q-card>
  <APCSimulationDataConfirmPopup
    v-if="isShowConfirmPopup"
    v-model="isShowConfirmPopup"
    :items="confirmPopupItems"
    :simulationRunnable="false"
  />
</template>

<script setup>
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { date } from 'quasar'
import { storeToRefs } from 'pinia'
import { useAPCStore } from 'src/stores/apc'
import DateTimeRangeSection from 'src/components/form/dateTime/DateTimeRangeSection.vue'
import APCSimulationService from 'src/services/apc/APCSimulationService'
import APCSimulationDataConfirmPopup from './APCSimulationDataConfirmPopup.vue'
import UserAvatar from 'src/components/common/UserAvatar.vue'
import Formula from '../model/Formula.vue'
import { useRouter } from 'vue-router'
import useUI from 'src/common/module/ui'
import { DELIMITER, apcConditionDelimiterToObject, splitConditionAndInputData } from 'src/common/apc/apcUtil'
import { APC_STATUS } from 'src/common/apc/apcConstant'

const ui = useUI()
const router = useRouter()
const { t } = useI18n()
const apcStore = useAPCStore()
const { apcConditions } = storeToRefs(apcStore)
const tableRef = ref()

const now = new Date()
const searchPeriod = ref({
  from: date.subtractFromDate(now, { days: 1 }),
  to: now,
})

const defaultFormData = computed(() =>
  apcConditions.value.reduce((prev, curr) => ({ ...prev, [curr.key]: undefined }), {}),
)

const formData = ref(defaultFormData.value)

const columns = computed(() => [
  //Apc 조건으로 컬럼 동적 구성
  ...apcConditions.value.map((it) => ({
    name: it.key,
    align: 'left',
    label: t(it.name),
    field: it.key,
    filterable: true,
  })),
  { name: 'formula', align: 'center', label: t('계산식'), field: 'formula', sortable: true },
  { name: 'version', align: 'left', label: t('버전'), field: 'version', sortable: true },
  { name: 'status', align: 'left', label: t('상태'), field: 'status', sortable: true },
  { name: 'dataCount', align: 'left', label: t('데이터 수'), field: 'dataCount', sortable: true },
  { name: 'createBy', align: 'left', label: t('실행자'), field: 'createBy', sortable: true },
  { name: 'createAt', align: 'left', label: t('실행일시'), field: 'createAt', sortable: true },
])

const rows = ref([])
const selected = ref([])

const onSearch = async () => {
  const condition = apcConditions.value.map((cond) => formData.value[cond.key] || '*').join(DELIMITER)
  const from = searchPeriod.value.from
  const to = searchPeriod.value.to

  ui.loading.show()
  const res = await APCSimulationService.getSimulationList(condition, from, to)
  ui.loading.hide()
  if (!res?.length) {
    ui.notify.warning(t('조회조건에 맞는 데이터가 없습니다.'))
    rows.value = []
  } else {
    rows.value = await Promise.all(
      res.map(async (item) => ({
        id: item.apcSimulationId,
        formula: item.formula,
        version: item.version,
        status: item.status,
        dataCount: item.apcSimulationDatas.length,
        createBy: item.createBy,
        createAt: item.createAt,
        apcSimulationDatas: item.apcSimulationDatas,
        ...(await apcConditionDelimiterToObject(item.condition)),
      })),
    )
  }
  tableRef.value.clearColumnFilter()
}

const isShowConfirmPopup = ref(false)
const confirmPopupItems = ref([])
const showConfirmPopup = async (row) => {
  confirmPopupItems.value = await Promise.all(
    row.apcSimulationDatas.map(async ({ data, ...rest }) => {
      const { condition, inputData } = await splitConditionAndInputData(data)
      return {
        ...rest,
        ...condition,
        inputData,
      }
    }),
  )
  isShowConfirmPopup.value = true
}

const onStatusClick = (row) => {
  router.push({ path: `/apc/simulation/${row.id}` })
}
</script>

<style lang="scss" scoped>
.formula-tooltip {
  overflow: visible;
  min-width: 0;
  min-height: 0;
  padding: 0;
}
.eco-mainContainer .q-card__section.search_section_wrap {
  height: auto;
}
</style>
