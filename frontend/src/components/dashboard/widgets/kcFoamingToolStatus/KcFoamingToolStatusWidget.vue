<template>
  <div>
    <!-- 설비 상태 표시 컴포넌트 -->
    <FoamingToolStatusDisplay
      :is-connected="isToolConnected()"
      :is-running="isToolRunning()"
      :tool-name="statusParameterToolName"
      :production-count="productionCount"
      :material-setting-value="materialSettingValue"
      :material-current-value="materialCurrentValue"
      :cycle-time="cycleTime"
      :current-process="currentProcess"
      :process-error="processErrorMessage"
      :sensor-values="currentProcessSensorValues"
      :show-settings-display-modal-button="true"
      @open-settings-modal="openSettingsModal"
    />

    <!-- 설정 모달 -->
    <KcFoamingToolSettingsDisplayModal
      v-model="showSettingsModal"
      :is-connected="isToolConnected()"
      :is-running="isToolRunning()"
      :tool-name="statusParameterToolName"
      :production-count="productionCount"
      :material-setting-value="materialSettingValue"
      :material-current-value="materialCurrentValue"
      :cycle-time="cycleTime"
      :current-process="currentProcess"
      :process-error="processErrorMessage"
      :sensor-values="currentProcessSensorValues"
      :process-values="processValuesForModal"
      :columns-per-table="columnsPerTable"
    />
  </div>
</template>

<script setup>
import { computed, defineProps, ref, watch } from 'vue'
import { useWidgetRefresh, widgetProps } from 'src/common/module/widgetCommon'
import ParameterDataService from 'src/services/parameterData/ParameterDataService'
import ParameterService from 'src/services/modeling/ParameterService'
import FoamingToolStatusDisplay from './FoamingToolStatusDisplay.vue'
import KcFoamingToolSettingsDisplayModal from './KcFoamingToolSettingsDisplayModal.vue'

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

const FRACTION_DIGITS = 1

const columnsPerTable = ref(10) // 설정 모달에서 공정별 데이터 표시를 위한 테이블 열 수

const showSettingsModal = ref(false)

// key: parameterId, value: parameterData
const parameterValues = ref({})
const statusParameterToolName = ref('-')

const widgetConfig = computed(() => props?.config || {})

const productionCount = computed(() => {
  const { productionCountParameterId } = widgetConfig.value
  if (!productionCountParameterId) {
    return '-'
  }
  const value = parameterValues.value[productionCountParameterId]
  return value ? formatValue(value) : '-'
})

const materialSettingValue = computed(() => {
  const { materialSettingParameterId } = widgetConfig.value
  if (!materialSettingParameterId) {
    return '-'
  }
  const value = parameterValues.value[materialSettingParameterId]
  return value ? formatValue(value) : '-'
})

const materialCurrentValue = computed(() => {
  const { materialCurrentParameterId } = widgetConfig.value
  if (!materialCurrentParameterId) {
    return '-'
  }
  const value = parameterValues.value[materialCurrentParameterId]
  return value ? formatValue(value) : '-'
})

const cycleTime = computed(() => {
  const { cycleTimeParameterId } = widgetConfig.value
  if (!cycleTimeParameterId) {
    return '-'
  }
  const value = parameterValues.value[cycleTimeParameterId]
  return value?.value ? `${value.value}초` : '-'
})

const currentProcess = computed(() => {
  const { processNameParameterId } = widgetConfig.value
  if (!processNameParameterId) {
    return '-'
  }
  const value = parameterValues.value[processNameParameterId]
  return value ? formatValue(value) : '-'
})

/**
 * key: 공정이름, value: 공정별 센서 상태값을 표기하기 위한 정보
 */
const processSensorParameterMap = computed(() => {
  if (!widgetConfig.value.processValues || !Array.isArray(widgetConfig.value.processValues)) {
    return {}
  }

  const processMap = {}
  props.config.processValues.forEach((processItem) => {
    const processName = processItem?.processName?.customValue
    if (processName) {
      processMap[processName] = processItem
    }
  })
  return processMap
})

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

const processErrorMessage = computed(() => {
  const { processErrorMessageParameterId } = widgetConfig.value
  if (!processErrorMessageParameterId) {
    return '-'
  }
  const value = parameterValues.value[processErrorMessageParameterId]
  return value ? formatValue(value) : '-'
})

// 설정 모달에 표시할 공정별 데이터
const processValuesForModal = computed(() => {
  if (!widgetConfig.value.processValues || !Array.isArray(widgetConfig.value.processValues)) {
    return []
  }

  return widgetConfig.value.processValues.map((process) => {
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

// eslint-disable-next-line no-unused-vars
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

// eslint-disable-next-line no-unused-vars
const formatting = (item) => {
  const rawDate = item?.traceAt
  if (!rawDate) return ''

  const date = new Date(rawDate)
  if (isNaN(date.getTime())) return '-'

  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const day = date.getDate()
  const hours = date.getHours()
  const minutes = date.getMinutes()
  const seconds = date.getSeconds()

  return `${year}-${month < 10 ? '0' + month : month}-${day < 10 ? '0' + day : day} ${hours < 10 ? '0' + hours : hours}:${minutes < 10 ? '0' + minutes : minutes}:${seconds < 10 ? '0' + seconds : seconds}`
}

const isToolConnected = () => {
  const config = widgetConfig.value
  if (!config) {
    return false
  }
  const statusData = parameterValues.value[config.connectionParameterId]
  return statusData?.value === 'UP'
}

const isToolRunning = () => {
  const config = widgetConfig.value
  if (!config) {
    return false
  }
  const statusData = parameterValues.value[config.statusParameterId]
  return statusData?.value === 1
}

// 설정 모달 열기
const openSettingsModal = () => {
  showSettingsModal.value = true
}

const refresh = async (config) => {
  const {
    dataInterval,
    statusParameterId,
    productionCountParameterId,
    materialSettingParameterId,
    materialCurrentParameterId,
    cycleTimeParameterId,
    processNameParameterId,
    processErrorMessageParameterId,
    connectionParameterId,
    processValues,
    columnsPerTable: configColumnsPerTable,
  } = config

  if (configColumnsPerTable) {
    columnsPerTable.value = configColumnsPerTable
  }

  // 새로운 config 구조에서 파라미터 ID들을 추출
  const parameterIds = [
    statusParameterId,
    productionCountParameterId,
    materialSettingParameterId,
    materialCurrentParameterId,
    cycleTimeParameterId,
    processNameParameterId,
    processErrorMessageParameterId,
    connectionParameterId,
    ...processValues.flatMap((process) => [process.settingValue?.parameterId, process.currentValue?.parameterId]),
  ]

  // 중복 제거
  const uniqueParameterIds = [...new Set(parameterIds.filter((id) => id))]
  if (uniqueParameterIds.length === 0) {
    parameterValues.value = {}
    return
  }

  const recentDataArray = await ParameterDataService.getLatestDataWithinPeriod(uniqueParameterIds, dataInterval)
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

useWidgetRefresh(refresh)
</script>

<style lang="scss" scoped>
/* 기본 컨테이너 스타일만 유지 */
</style>
