<template>
  <q-dialog v-model="showDialog" persistent class="full-width">
    <q-card class="dialog-container full-width column no-wrap justify-between">
      <q-card-section class="bg-secondary text-white full-width">
        <div class="text-h6">{{ dialogTitle }}</div>
      </q-card-section>
      <q-card-section class="full-width chart-area">
        <trend-chart :datas="faultDataList" show-spec-line />
      </q-card-section>
      <q-card-actions class="justify-between q-mt-auto">
        <span class="field-required-indicate">{{ $t('필수 입력 필드') }}</span>
        <div>
          <q-btn flat color="negative" :label="$t('닫기')" @click="emit('close')" />
        </div>
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup>
import { computed, defineEmits, onBeforeMount, ref } from 'vue'
import TrendChart from 'components/chart/TrendChart.vue'
import { useI18n } from 'vue-i18n'
import { date } from 'quasar'
import { DATE_TIME_FORMAT } from 'src/common/constant/format'
import ParameterDataService from 'src/services/parameterData/ParameterDataService'

const showDialog = true

const props = defineProps({
  parameterId: {
    type: Number,
    required: true,
  },
  faultAt: {
    type: Date,
    required: true,
  },
})
const emit = defineEmits(['close'])

const { t } = useI18n()

const faultDataList = ref([])
const dialogTitle = computed(() => {
  const timeString = date.formatDate(props.faultAt, DATE_TIME_FORMAT)
  return `${t('이상 데이터 시간기준 차트')} (${t('선택시간')}: ${timeString})`
})

const fetchFaultData = async () => {
  const { parameterId, faultAt } = props

  const from = new Date(new Date(faultAt).getTime() - 5 * 60 * 1000)
  const to = new Date(new Date(faultAt).getTime() + 5 * 60 * 1000)

  const result = await ParameterDataService.getParameterDataTrend(parameterId, from, to)

  faultDataList.value = [
    {
      trendData: result.rawDatas.map((item) => {
        return {
          ...item,
          x: item.traceAt,
          y: Number(item.value),
        }
      }),
    },
  ]
}

onBeforeMount(() => {
  fetchFaultData()
})
</script>

<style scoped lang="scss">
.dialog-container {
  width: 60vw;
  max-width: 60vw;

  height: 80vh;
  max-height: 80vh;
}

.chart-area {
  width: 100%;
  height: 80%;
}
</style>
