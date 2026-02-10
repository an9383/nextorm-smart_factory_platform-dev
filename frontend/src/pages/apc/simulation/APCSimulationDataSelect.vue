<template>
  <q-card flat>
    <q-card-section class="q-pa-none">
      <q-form ref="form">
        <div class="row">
          <q-item class="col-2">
            <q-input
              v-model="modelInfo.modelName"
              :label="$t('모델명')"
              :rules="[$rules.required]"
              class="input-required"
              disable
            />
          </q-item>
          <!-- APC 조건으로 동적 구성 -->
          <q-item v-for="item in apcConditions" class="col-2" :key="item.key">
            <q-input
              v-model="modelInfo[item.key]"
              :label="item.name"
              :rules="[$rules.required]"
              class="input-required"
              disable
            />
          </q-item>
        </div>
        <div class="row">
          <q-item class="col-2">
            <q-input
              v-model="modelInfo.version"
              :label="$t('버전')"
              :rules="[$rules.required]"
              class="input-required"
              disable
            />
            <q-icon name="calculate" color="green" size="md" class="q-mt-xs">
              <q-tooltip class="formula-tooltip">
                <Formula :workspace-json-string="modelInfo.formulaWorkspace"></Formula>
              </q-tooltip>
            </q-icon>
          </q-item>
          <q-item class="col-4">
            <DateTimeRangeSection v-model:from-date="searchPeriod.from" v-model:to-date="searchPeriod.to" />
          </q-item>
          <q-item>
            <q-btn class="search_btn with_icon_btn sBtn" @click="loadTargetData">{{ $t('조회') }}</q-btn>
          </q-item>
        </div>
      </q-form>
    </q-card-section>
    <q-card-section>
      <simple-table
        v-model:selected="selectedTargetDataRows"
        :rows="targetDataRows"
        :columns="columns"
        row-key="traceAt"
        color="amber"
        selection="multiple"
        class="sticky-header"
        :pagination="{
          rowsPerPage: 0,
        }"
        flat
        hide-pagination
        virtual-scroll
      >
        <template v-slot:body="props">
          <q-tr class="cursor-pointer" :props="props" @click="props.selected = !props.selected">
            <q-td v-for="col in props.cols" :key="col.name" :props="props">
              <template v-if="col.name === 'traceAt'">
                {{ $utils.formatDateTime(col.value) }}
              </template>
              <template v-else-if="col.name === 'inputData'">
                <q-chip color="secondary" text-color="white">
                  {{ `${Object.keys(col.value).length} ${$t('건')}` }}
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
    <q-card-section align="right">
      <q-btn @click="moveToBack" class="sBtn negative">
        <q-icon name="output" size="xs" class="q-mr-xs"></q-icon>
        {{ $t('나가기') }}
      </q-btn>
      <q-btn @click="showConfirmPopup" class="sBtn q-ml-sm">
        <q-icon name="play_arrow" size="xs" class="q-mr-xs"></q-icon>
        {{ $t('시뮬레이션 실행') }}
      </q-btn>
    </q-card-section>
  </q-card>
  <APCSimulationDataConfirmPopup
    v-if="isShowConfirmPopup"
    v-model="isShowConfirmPopup"
    :items="selectedTargetDataRows"
    @confirm="runSimulation"
  />
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { date } from 'quasar'
import { t } from 'src/plugins/i18n'
import useUI from 'src/common/module/ui'
import { useAPCStore } from 'src/stores/apc'
import DateTimeRangeSection from '/src/components/form/dateTime/DateTimeRangeSection.vue'
import Formula from 'pages/apc/model/Formula.vue'
import APCSimulationDataConfirmPopup from './APCSimulationDataConfirmPopup.vue'
import APCSimulationService from '/src/services/apc/APCSimulationService'
import ApcModelService from '/src/services/apc/ApcModelService'
import { apcConditionDelimiterToObject, splitConditionAndInputData } from '/src/common/apc/apcUtil'

const { apcModelVersionId } = defineProps({
  apcModelVersionId: {
    type: Number,
    required: true,
    default: null,
  },
})
const emit = defineEmits(['simulation-started'])

const router = useRouter()
const ui = useUI()
const apcStore = useAPCStore()
const { apcConditions } = storeToRefs(apcStore)

const searchPeriod = ref({
  from: date.subtractFromDate(Date.now(), { days: 1 }),
  to: Date.now(),
})

onMounted(() => {
  if (!apcModelVersionId) {
    ui.alert(t('APC 모델'), t('선택된 APC 모델이 없습니다. 모델을 선택 해 주세요.'), t('확인'), {
      persistent: true,
    }).onOk(() => router.replace('/apc/model'))
    return
  }
  loadApcModelVersion()
})

const modelInfo = ref({})
const loadApcModelVersion = async () => {
  const apcModelVersion = await ApcModelService.getApcModelVersion(apcModelVersionId)
  const apcConditions = await apcConditionDelimiterToObject(apcModelVersion.conditions)
  modelInfo.value = {
    ...apcConditions,
    modelName: apcModelVersion.modelName,
    version: apcModelVersion.version,
    formulaWorkspace: apcModelVersion.formulaWorkspace,
  }
}

const columns = computed(() => [
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
])
const selectedTargetDataRows = ref([])
const targetDataRows = ref([])

const loadTargetData = async () => {
  ui.loading.show()
  selectedTargetDataRows.value = []
  targetDataRows.value = []
  const { from, to } = searchPeriod.value
  const targetData = await APCSimulationService.getSimulationTargetData(from, to)
  ui.loading.hide()
  targetDataRows.value = await Promise.all(
    targetData.map(async (it) => {
      const { condition, inputData } = await splitConditionAndInputData(it.data)
      return { traceAt: it.traceAt, ...condition, inputData }
    }),
  )
}

const moveToBack = () => {
  ui.confirm(
    t('시뮬레이션 종료'),
    t('시뮬레이션을 종료하고 이전 페이지로 돌아가시겠습니까?'),
    t('이전 페이지로 이동'),
    t('취소'),
  ).onOk(() => router.back())
}

const isShowConfirmPopup = ref(false)

const showConfirmPopup = () => {
  if (selectedTargetDataRows.value.length === 0) {
    ui.notify.warning(t('시뮬레이션을 실행할 데이터를 선택 해 주세요.'))
    return
  }
  isShowConfirmPopup.value = true
}

const runSimulation = async () => {
  const apcTargetDatas = selectedTargetDataRows.value.map(({ traceAt, inputData, ...condition }) => {
    return {
      traceAt,
      data: {
        ...condition,
        ...inputData,
      },
    }
  })
  ui.loading.show()
  try {
    const apcSimulation = await APCSimulationService.runApcSimulation(apcModelVersionId, apcTargetDatas)
    emit('simulation-started', apcSimulation.id)
  } finally {
    ui.loading.hide()
  }
}
</script>
<style scoped lang="scss">
.sticky-header {
  max-height: 490px;
}
.input-data-item {
  min-height: auto;
  padding-top: 5px;
  padding-bottom: 5px;
}
</style>
