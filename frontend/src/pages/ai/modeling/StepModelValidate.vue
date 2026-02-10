<template>
  <div class="model-validation-chart">
    <div class="full-height full-width">
      <fast-trend-chart
        v-if="model"
        :parameter-ids="parameterIds"
        :from-date="fromDate"
        :to-date="toDate"
        use-inference
        :model-id="modelId"
        @changeChartData="handleChartDataChange"
      />
    </div>
  </div>
</template>

<script setup>
import FastTrendChart from 'pages/ai/modeling/parameterSelectStep/FastTrendChart.vue'
import { onMounted, ref } from 'vue'
import useUI from 'src/common/module/ui'
import AiService from 'src/services/ai/AiService'
import { t } from 'src/plugins/i18n'

const props = defineProps({
  modelId: {
    type: Number,
    required: true,
  },
})

const ui = useUI()
const model = ref(null)
const parameterIds = ref([])
const fromDate = ref(null)
const toDate = ref(null)

const handleChartDataChange = (chartData) => {
  console.log('차트 데이터 변경:', chartData)
}

onMounted(async () => {
  ui.loading.show()

  try {
    const models = await AiService.getModels()
    model.value = models.find((model) => model.id === props.modelId)

    if (model.value) {
      // X 파라미터와 Y 파라미터를 모두 포함하는 배열 생성
      const xParameterIds = model.value.parameterDetails.map((parameter) => {
        return parameter.id
      })

      parameterIds.value = [xParameterIds, model.value.yparameterId]
      fromDate.value = new Date(model.value.from)
      toDate.value = new Date(model.value.to)
    } else {
      ui.notify.warning(t('해당 모델을 찾을 수 없습니다.'))
    }
  } catch (error) {
    console.error('모델 데이터 로드 오류:', error)
    ui.notify.error(t('모델 데이터를 불러오는 중 오류가 발생했습니다.'))
  } finally {
    ui.loading.hide()
  }
})
</script>

<style scoped>
.model-validation-chart {
  height: 90%;
}
</style>
