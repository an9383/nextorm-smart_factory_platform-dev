import { computed } from 'vue'

const FRACTION_DIGITS = 1

/**
 * KC Tool Status 위젯을 위한 비즈니스 로직을 제공하는 Composable
 */
export function useKcToolStatus(parameterValues, config) {
  /**
   * 값 포맷팅 함수
   */
  const formatValue = (item) => {
    const value = item?.value
    const dataType = item?.dataType

    // 숫자형 데이터가 아니라면 바로 반환
    if (dataType !== 'INTEGER' && dataType !== 'DOUBLE') {
      return value
    }

    if (value === null || value === '' || isNaN(Number(value))) {
      return '-'
    }

    let numValue = Number(value)
    if (dataType === 'DOUBLE') {
      numValue = Number(Number(value).toFixed(FRACTION_DIGITS))
    }
    if (dataType === 'INTEGER') {
      numValue = Math.floor(numValue)
    }
    return numValue.toLocaleString() + (item.unit || '')
  }

  /**
   * 파라미터 ID로 포맷된 값을 가져오는 함수
   */
  const getFormattedValue = (parameterId) => {
    if (!parameterId) {
      return '-'
    }
    const value = parameterValues.value[parameterId]
    return value ? formatValue(value) : '-'
  }

  /**
   * 파라미터 ID로 원시 값을 가져오는 함수
   */
  const getRawValue = (parameterId) => {
    if (!parameterId) {
      return null
    }
    const value = parameterValues.value[parameterId]
    return value ? value.value : null
  }

  // 설비 연결 상태
  const isToolConnected = computed(() => {
    const statusData = parameterValues.value[config.value.connectionParameterId]
    return statusData?.value === 'UP'
  })

  // 설비 가동 상태
  const isToolRunning = computed(() => {
    const statusData = parameterValues.value[config.value.statusParameterId]
    return statusData?.value === 1
  })

  // Cavity
  const cavity = computed(() => {
    return getRawValue(config.value.cavityParameterId)
  })

  // 계산된 생산 수량
  const calculateProductionCount = computed(() => {
    return getFormattedValue(config.value.calculatedProductionCountParameterId)
  })

  // 계획 대비 생산율
  const productionRate = computed(() => {
    return getFormattedValue(config.value.productionRateAgainstPlanParameterId)
  })

  // 생산 수량
  const productionCount = computed(() => {
    return getFormattedValue(config.value.productionCountParameterId)
  })

  // 제품 ID
  const productId = computed(() => {
    return getRawValue(config.value.productIdParameterId)
  })

  // 사이클 타임
  const cycleTime = computed(() => {
    return getFormattedValue(config.value.cycleTimeParameterId)
  })

  // 현재 공정
  const currentProcess = computed(() => {
    return getFormattedValue(config.value.processNameParameterId)
  })

  // 공정 에러 메시지
  const processErrorMessage = computed(() => {
    return getFormattedValue(config.value.processErrorMessageParameterId)
  })

  /**
   * key: 공정이름, value: 공정별 센서 상태값을 표기하기 위한 정보
   */
  const processSensorParameterMap = computed(() => {
    if (!config.value.processValues || !Array.isArray(config.value.processValues)) {
      return {}
    }

    const processMap = {}
    config.value.processValues.forEach((processItem) => {
      const processName = processItem?.processName?.customValue
      if (processName) {
        processMap[processName] = processItem
      }
    })
    return processMap
  })

  // 현재 공정의 센서 값
  const currentProcessSensorValues = computed(() => {
    const processName = currentProcess.value
    const findMap = processSensorParameterMap.value[processName]
    if (!findMap) {
      return '- / -'
    }

    const settingValueParameterId = findMap?.settingValue?.parameterId
    const settingValue = parameterValues.value[settingValueParameterId]

    const currentValueParameterId = findMap?.currentValue?.parameterId
    const currentValue = parameterValues.value[currentValueParameterId]

    return `${settingValue ? formatValue(settingValue) : '-'} / ${currentValue ? formatValue(currentValue) : '-'}`
  })

  // 설정 모달에 표시할 공정별 데이터
  const processValuesForModal = computed(() => {
    if (!config.value.processValues || !Array.isArray(config.value.processValues)) {
      return []
    }

    return config.value.processValues.map((process) => {
      const settingValueParameterId = process?.settingValue?.parameterId
      const currentValueParameterId = process?.currentValue?.parameterId

      const settingData = parameterValues.value[settingValueParameterId]
      const currentData = parameterValues.value[currentValueParameterId]

      return {
        processName: process?.processName,
        settingValue: process?.settingValue,
        currentValue: process?.currentValue,
        settingDisplayValue: settingData ? formatValue(settingData) : '-',
        currentDisplayValue: currentData ? formatValue(currentData) : '-',
      }
    })
  })

  return {
    formatValue,
    getFormattedValue,
    getRawValue,
    isToolConnected,
    isToolRunning,
    cavity,
    calculateProductionCount,
    productionRate,
    productionCount,
    productId,
    cycleTime,
    currentProcess,
    processErrorMessage,
    processSensorParameterMap,
    currentProcessSensorValues,
    processValuesForModal,
  }
}
