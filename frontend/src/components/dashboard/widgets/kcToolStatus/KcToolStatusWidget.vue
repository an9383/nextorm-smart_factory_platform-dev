<template>
  <div>
    <!-- 설비 상태 표시 컴포넌트 -->
    <ToolStatusDisplay
      :show-settings-display-modal-button="true"
      @open-product-modal="openProductNameModal"
      @open-settings-modal="openSettingsModal"
    />

    <!-- 제품명 변경 모달 -->
    <ProductChangeModal
      v-if="showProductNameModal"
      :product-id="toolStatus.productId.value"
      @save="handleProductSave"
      @close="handleProductNameClose"
    />

    <!-- 설정 모달 -->
    <ToolSettingsDisplayModal v-model="showSettingsModal" @open-product-modal="openProductNameModal" />
  </div>
</template>

<script setup>
import { computed, defineProps, onUnmounted, provide, ref, watch } from 'vue'
import { useWidgetRefresh, widgetProps } from 'src/common/module/widgetCommon'
import ParameterDataService from 'src/services/parameterData/ParameterDataService'
import ParameterService from 'src/services/modeling/ParameterService'
import ProductChangeModal from './ProductChangeModal.vue'
import ToolStatusDisplay from './ToolStatusDisplay.vue'
import ToolSettingsDisplayModal from './ToolSettingsDisplayModal.vue'
import { useKcToolStatus } from './useKcToolStatus'
import EqmsResourceService from 'src/services/EqmsResourceService'
import useUI from 'src/common/module/ui'

const props = defineProps({
  ...widgetProps,
  widgetId: {
    type: String,
    required: true,
  },
  dashboardWidgetId: {
    type: Number,
    required: false,
  },
})

const columnsPerTable = ref(10) // 설정 모달에서 공정별 데이터 표시를 위한 테이블 열 수

const showProductNameModal = ref(false)
const showSettingsModal = ref(false)

// key: parameterId, value: parameterData
const parameterValues = ref({})
const statusParameterToolName = ref('-')
const productIdMetaValue = ref('') // 제품 파라미터의 메타값

/**
 * 타수 임계값 초과 알림 관련 코드
 */
const { notify } = useUI()
const cycleCountCheckTimer = ref(null)
const CYCLE_COUNT_THRESHOLD = 100000 // 타수 임계값
const CYCLE_COUNT_THRESHOLD_OVER_CHECK_INTERVAL = 5 * 60 * 1000 // 5분

const widgetConfig = computed(() => props?.config || {})

// Composable을 사용하여 모든 계산된 값 생성
const toolStatus = useKcToolStatus(parameterValues, widgetConfig)

// Provide로 하위 컴포넌트에 데이터 제공
const toolStatusContext = computed(() => ({
  ...toolStatus,
  statusParameterToolName: statusParameterToolName.value,
  productIdMetaValue: productIdMetaValue.value,
  columnsPerTable: columnsPerTable.value,
}))

provide('toolStatusContext', toolStatusContext)

const loadStatusParameterToolName = async () => {
  //파라미터 정보 + 툴 + 위치 정보 조회
  const config = widgetConfig.value
  if (!config) {
    return
  }

  const { statusParameterId } = config
  if (!statusParameterId) {
    statusParameterToolName.value = '-'
    return
  }

  const result = await ParameterService.getToolsByParameters([statusParameterId])
  if (result.length > 0) {
    statusParameterToolName.value = result[0].toolName
  } else {
    statusParameterToolName.value = '-'
  }
}

// 제품명 모달 열기
const openProductNameModal = () => {
  showProductNameModal.value = true
}

// 설정 모달 열기
const openSettingsModal = () => {
  showSettingsModal.value = true
}

// 제품 변경 및 관련 메타데이터 정보 변경
const handleProductSave = async (newProduct) => {
  const { productIdParameterId, cavityParameterId, averageProductionCountParameterId } = widgetConfig.value
  await ParameterService.modifyMetaDataParameters([
    { id: productIdParameterId, value: newProduct.id },
    { id: cavityParameterId, value: newProduct.cavity },
    { id: averageProductionCountParameterId, value: newProduct.averageProductionCount },
  ])
  showProductNameModal.value = false
  await refresh(widgetConfig.value)
}

// 제품명 모달 닫기
const handleProductNameClose = () => {
  showProductNameModal.value = false
}

// 타수 임계값 초과 여부 확인, 초과한 경우 알람 표시
const checkCycleCountThresholdOver = async () => {
  const productId = toolStatusContext.value?.productId.value
  const moldProducts = await EqmsResourceService.getMoldProducts()
  const product = moldProducts.find((p) => p.id === productId)
  if (!product) {
    return
  }
  const cycleCount = product.cycleCount
  if (cycleCount >= CYCLE_COUNT_THRESHOLD) {
    notify.warning(
      `${statusParameterToolName.value}의 제품 "${product.name}"의 타수가 ${CYCLE_COUNT_THRESHOLD}회를 초과했습니다.`,
      0,
      {
        closeButton: true,
        actions: [
          {
            icon: 'close',
            color: 'white',
            noDismiss: true,
          },
        ],
      },
    )
  }
}

// 타수 임계값 초과 여부 주기적 확인 (setTimeout을 이용해 interval 같이 동작하도록 구현)
const repeatCycleCountCheck = async () => {
  await checkCycleCountThresholdOver()
  cycleCountCheckTimer.value = setTimeout(async () => {
    await repeatCycleCountCheck()
  }, CYCLE_COUNT_THRESHOLD_OVER_CHECK_INTERVAL)
}

const refresh = async (config) => {
  const {
    dataInterval,
    statusParameterId,
    productionCountParameterId,
    cycleTimeParameterId,
    processNameParameterId,
    processErrorMessageParameterId,
    connectionParameterId,
    processValues,
    productIdParameterId,
    cavityParameterId,
    averageProductionCountParameterId,
    calculatedProductionCountParameterId,
    productionRateAgainstPlanParameterId,
    columnsPerTable: configColumnsPerTable,
  } = config

  if (configColumnsPerTable) {
    columnsPerTable.value = configColumnsPerTable
  }

  // 새로운 config 구조에서 파라미터 ID들을 추출
  const parameterIds = [
    statusParameterId,
    productionCountParameterId,
    cycleTimeParameterId,
    processNameParameterId,
    processErrorMessageParameterId,
    connectionParameterId,
    productIdParameterId,
    cavityParameterId,
    averageProductionCountParameterId,
    calculatedProductionCountParameterId,
    productionRateAgainstPlanParameterId,
    ...processValues.flatMap((process) => [process.settingValue?.parameterId, process.currentValue?.parameterId]),
  ]

  // 중복 제거
  const uniqueParameterIds = [...new Set(parameterIds.filter((id) => id))]
  if (uniqueParameterIds.length === 0) {
    parameterValues.value = {}
    return
  }

  const recentDataArray = await ParameterDataService.getLatestDataWithinPeriod(uniqueParameterIds, dataInterval)
  const productIdParameter = await ParameterService.getParameterById(productIdParameterId)
  productIdMetaValue.value = productIdParameter?.metaValue || ''

  // 배열을 parameterId를 key로 하는 객체로 변환
  parameterValues.value = recentDataArray.reduce((acc, data) => {
    if (data?.parameterId) {
      acc[data.parameterId] = data
    }
    return acc
  }, {})
}
watch(
  () => props.config,
  () => loadStatusParameterToolName(),
  { deep: true, immediate: true },
)

watch(
  () => toolStatusContext.value?.productId.value,
  async (newValue, oldValue) => {
    // 제품ID 값이 최초 할당되는 시점에 동작
    if (!oldValue && newValue) {
      await repeatCycleCountCheck()
      return
    }

    if (newValue !== oldValue) {
      // 제품ID 값이 변경되는 시점에 이미 존재하는 타이머 초기화
      if (cycleCountCheckTimer.value) {
        clearTimeout(cycleCountCheckTimer.value)
      }
      await repeatCycleCountCheck()
    }
  },
)

useWidgetRefresh(refresh)

onUnmounted(() => {
  if (cycleCountCheckTimer.value) {
    clearTimeout(cycleCountCheckTimer.value)
  }
})
</script>

<style lang="scss" scoped>
/* 기본 컨테이너 스타일만 유지 */
</style>
