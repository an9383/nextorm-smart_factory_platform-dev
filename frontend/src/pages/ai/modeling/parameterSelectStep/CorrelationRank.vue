<template>
  <div class="full-width" style="position: relative; width: 100%; height: 250px; overflow: auto">
    <q-table
      class="full-width"
      :rows="rows"
      :columns="columns"
      row-key="id"
      selection="multiple"
      v-model:selected="selectedRankRows"
      :hide-bottom="true"
      :pagination="{
        rowsPerPage: 0,
      }"
    >
    </q-table>
    <div
      v-if="isLoading"
      class="absolute flex flex-center bg-white bg-opacity-70"
      style="top: 0; left: 0; width: 100%; height: 100%; z-index: 10; pointer-events: all"
    >
      <q-spinner size="40px" color="primary" />
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { pt } from 'src/plugins/i18n'
import AiService from 'src/services/ai/AiService'

const { t } = useI18n()

const props = defineProps({
  xParameterIds: {
    type: Array,
    required: true,
  },
  yParameter: {
    type: Object,
    required: true,
  },
  fromDate: {
    type: Date,
    required: true,
  },
  toDate: {
    type: Date,
    required: true,
  },
})

const selectedRankRows = ref([])
const rows = ref([])
const isLoading = ref(false)

const columns = [
  { name: 'name', align: 'left', label: t('파라미터명'), field: 'name', format: (val) => pt(val) },
  { name: 'rank', align: 'left', label: t('순위'), field: 'rank' },
  { name: 'point', align: 'left', label: t('상관관계점수'), field: 'point' },
]

const emits = defineEmits(['update:selected'])

watch(selectedRankRows, (newVal) => {
  emits('update:selected', newVal)
})

// 랭킹 데이터 가져오기
const getRankData = async () => {
  isLoading.value = true

  const correlationResult = await AiService.getCorrelation({
    yParameterId: props.yParameter.id,
    xParameterIds: props.xParameterIds,
    from: props.fromDate,
    to: props.toDate,
  })

  // rows에 데이터 설정
  rows.value = correlationResult.map((v) => ({
    id: v.parameterId,
    name: v.parameterName,
    rank: v.rank,
    point: v.corCoefficient,
  }))

  isLoading.value = false
}

// props 변경 감지
watch(
  [() => props.xParameterIds, () => props.yParameter, () => props.fromDate, () => props.toDate],
  async () => {
    selectedRankRows.value = []
    await getRankData()
  },
  { immediate: true },
)
</script>
