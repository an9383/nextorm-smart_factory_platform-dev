<template>
  <q-dialog v-model="modelValue" persistent>
    <q-card class="dialog-container">
      <q-card-section class="bg-secondary text-white">
        <div class="text-h6">{{ popupTitle }}</div>
      </q-card-section>
      <q-card-section>
        <simple-table
          :title="$t('시뮬레이션 대상 데이터')"
          :rows="items"
          :columns="columns"
          row-key="date"
          class="sticky-header"
          :pagination="{
            rowsPerPage: 0,
          }"
          flat
          hide-pagination
          virtual-scroll
        >
          <template v-slot:body-cell-traceAt="props">
            <q-td :props="props">
              {{ $utils.formatDateTime(props.value) }}
            </q-td>
          </template>
          <template v-slot:body-cell-inputData="props">
            <q-td :props="props">
              <q-chip color="secondary" text-color="white">
                {{ `${Object.keys(props.value).length} ${$t('건')}` }}
                <q-tooltip class="text-body1">
                  <q-list>
                    <q-item v-for="key in Object.keys(props.value)" :key="key" class="input-data-item">
                      <span class="text-bold q-mr-md">{{ key }}</span> {{ props.value[key] }}
                    </q-item>
                  </q-list>
                </q-tooltip>
              </q-chip>
            </q-td>
          </template>
        </simple-table>
      </q-card-section>
      <q-separator />
      <q-card-actions class="justify-end">
        <q-btn flat color="negative" :label="$t('닫기')" @click="close" />
        <q-btn v-if="simulationRunnable" flat color="primary" :label="$t('실행')" @click="confirm" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup>
import { computed, defineEmits } from 'vue'
import { t } from '/src/plugins/i18n'
import { storeToRefs } from 'pinia'
import { useAPCStore } from 'src/stores/apc'

const { items, simulationRunnable } = defineProps({
  //traceAt, ...conditions(APC 모델 컨디션 필드), inputData: {}(파라미터)
  items: {
    type: Array,
    required: true,
    default: () => [],
  },
  //Simulation 실행 가능 여부(버튼 노출 처리)
  simulationRunnable: {
    type: Boolean,
    required: false,
    default: true,
  },
})
const emit = defineEmits(['confirm'])
const apcStore = useAPCStore()
const { apcConditions } = storeToRefs(apcStore)

const popupTitle = computed(() => (simulationRunnable ? t('시뮬레이션 실행') : t('시뮬레이션 데이터')))

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

const confirm = () => {
  emit('confirm')
  close()
}

const close = () => {
  emit('update:modelValue', false)
}
</script>

<style scoped lang="scss">
.dialog-container {
  width: 1200px;
  max-width: 1200px;
}
.sticky-header {
  max-height: 480px;
}
.input-data-item {
  min-height: auto;
  padding-top: 5px;
  padding-bottom: 5px;
}
</style>
