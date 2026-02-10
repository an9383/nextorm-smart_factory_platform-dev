<template>
  <q-card flat>
    <q-card-section class="q-pa-none flex">
      <span class="text-subtitle2 text-weight-bold q-mx-md q-mt-xs">{{ $t('진행상황') }}</span>
      <q-linear-progress
        size="25px"
        :value="progressStatus.value"
        :color="progressStatus.value === 1 ? 'primary' : 'secondary'"
        class="col"
      >
        <div class="absolute-full flex flex-center">
          <q-badge color="white" text-color="primary" class="text-weight-bold" :label="progressStatus.percent" />
        </div>
      </q-linear-progress>
      <q-btn v-if="progressStatus.value < 1" class="sBtn q-mx-md negative" @click="cancelSimulation">
        <q-icon name="close" size="xs" class="q-mr-xs"></q-icon>
        {{ $t('취소') }}
      </q-btn>
    </q-card-section>
    <q-card-section>
      <simple-table
        :rows="simulationDataRows"
        :columns="simulationDataColumns"
        row-key="id"
        selection="single"
        v-model:selected="progressSelectedRow"
        hide-pagination
        :pagination="{
          rowsPerPage: 0,
        }"
      >
        <template v-slot:body="props">
          <q-tr
            :class="isRowClickable(props.row.status) ? 'cursor-pointer' : 'cursor-not-allowed'"
            :props="props"
            @click="
              () => {
                if (isRowClickable(props.row.status)) {
                  props.selected = true
                  loadSimulationRequestReuslt(props.row.id)
                }
              }
            "
          >
            <q-td v-for="col in props.cols" :key="col.name" :props="props">
              <template v-if="col.name === 'traceAt'">
                {{ $utils.formatDateTime(col.value) }}
              </template>
              <template v-else-if="col.name === 'status'">
                <q-chip :color="APC_STATUS[col.value]?.color" text-color="white">
                  <q-spinner-dots v-if="col.value === 'RUNNING'" color="primary" size="sm" class="q-mr-sm" />
                  <q-icon v-else :name="APC_STATUS[col.value]?.icon" class="q-mr-sm"></q-icon>
                  {{ APC_STATUS[col.value]?.text }}
                  <q-tooltip v-if="col.value === 'ERROR'">{{ getErrorMsgByCode(props.row.errorCode) }}</q-tooltip>
                </q-chip>
              </template>
              <template v-else-if="col.name === 'inputData'">
                <q-chip color="secondary" text-color="white">
                  {{ `${col.value ? Object.keys(col.value).length : 0} ${$t('건')}` }}
                  <q-tooltip class="text-body1">
                    <q-list>
                      <q-item v-for="key in Object.keys(col.value)" :key="key" class="input-data-item">
                        <span class="text-bold q-mr-md">{{ key }}</span> {{ col.value[key] }}
                      </q-item>
                    </q-list>
                  </q-tooltip>
                </q-chip>
              </template>
              <template v-else>
                {{ col.value }}
              </template>
            </q-td>
          </q-tr>
        </template>
      </simple-table>
    </q-card-section>
    <q-card-section>
      <div class="title_wrap">
        <span class="text-subtitle2 text-weight-bold">{{ $t('시뮬레이션 결과') }}</span>
      </div>
      <div class="row">
        <div class="col-6">
          <simple-table
            :rows="resultRows"
            :columns="resultColumns"
            row-key="resultKey"
            selection="none"
            flat
            hide-pagination
            :pagination="{
              rowsPerPage: 0,
            }"
          ></simple-table>
        </div>
      </div>
    </q-card-section>
  </q-card>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { storeToRefs } from 'pinia'
import { useInterval } from 'quasar'
import { t } from 'src/plugins/i18n'
import useUI from 'src/common/module/ui'
import { useAPCStore } from 'src/stores/apc'
import APCSimulationService from '/src/services/apc/APCSimulationService'
import { splitConditionAndInputData } from '/src/common/apc/apcUtil'
import { APC_STATUS, getErrorMsgByCode } from 'src/common/apc/apcConstant'

const { apcSimulationId } = defineProps({
  apcSimulationId: {
    type: Number,
    required: true,
    default: null,
  },
})

const finishStatuses = Object.entries(APC_STATUS)
  // eslint-disable-next-line no-unused-vars
  .filter(([key, value]) => value.isFinishStatus)
  .map(([key]) => key)

const ui = useUI()
const apcStore = useAPCStore()
const { apcConditions } = storeToRefs(apcStore)
const progressSelectedRow = ref([])
const simulationDataColumns = [
  {
    name: 'traceAt',
    align: 'left',
    label: t('일시'),
    field: 'traceAt',
  },
  //Apc 조건으로 컬럼 동적 구성
  ...apcConditions.value.map((it) => ({
    name: it.key,
    align: 'left',
    label: t(it.name),
    field: it.key,
    sortable: true,
  })),
  { name: 'inputData', align: 'left', label: t('데이터'), field: 'inputData' },
  { name: 'status', align: 'left', label: t('상태'), field: 'status' },
]

const simulationDataRows = ref([])

const progressStatus = computed(() => {
  if (simulationDataRows.value.length === 0) {
    return {
      value: 0,
      percent: 0,
    }
  }
  const completeCount = simulationDataRows.value.filter((it) => {
    return finishStatuses.includes(it.status)
  }).length
  const value = completeCount / simulationDataRows.value.length
  const percent = (value * 100).toFixed(2) + ' %'
  return {
    value,
    percent,
  }
})

const POLLING_INTERVAL = 1 * 1000
const { registerInterval, removeInterval } = useInterval()

onMounted(() => {
  startPolling()
})

onBeforeUnmount(() => {
  stopPolling()
})

const startPolling = () => {
  loadApcSimulation()
  registerInterval(loadApcSimulation, POLLING_INTERVAL)
}

const stopPolling = () => {
  removeInterval()
}

const loadApcSimulation = async () => {
  const apcSimulation = await APCSimulationService.getApcSimulation(apcSimulationId)
  simulationDataRows.value = await Promise.all(
    apcSimulation.apcSimulationDatas.map(async (it) => {
      const { condition, inputData } = await splitConditionAndInputData(it.data)
      return { id: it.id, traceAt: it.traceAt, status: it.status, errorCode: it.errorCode, ...condition, inputData }
    }),
  )

  const remainCount = apcSimulation.apcSimulationDatas.filter((it) => !finishStatuses.includes(it.status)).length
  if (remainCount === 0) {
    stopPolling()
  }
}

const isRowClickable = (status) => {
  return status === 'SUCCESS'
}

const resultColumns = [
  {
    name: 'resultKey',
    align: 'left',
    label: t('키'),
    field: 'resultKey',
  },
  {
    name: 'resultValue',
    align: 'left',
    label: t('값'),
    field: 'resultValue',
    sortable: true,
  },
]

const resultRows = ref([])

const loadSimulationRequestReuslt = async (apcSimulationDataId) => {
  const requestResult = await APCSimulationService.getApcSimulationDataRequestResult(apcSimulationDataId)
  resultRows.value = requestResult
}

const cancelSimulation = () => {
  ui.confirm(t('시뮬레이션 취소'), t('시뮬레이션 작업을 취소 하시겠습니까?'), t('취소'), t('닫기')).onOk(async () => {
    ui.loading.show()
    await APCSimulationService.cancelApcSimulation(apcSimulationId)
    loadApcSimulation()
    ui.loading.hide()
  })
}
</script>
