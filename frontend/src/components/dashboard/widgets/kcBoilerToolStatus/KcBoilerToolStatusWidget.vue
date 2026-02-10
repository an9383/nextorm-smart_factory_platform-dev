<template>
  <div>
    <!-- 설비 상태 표시 컴포넌트 -->
    <BoilerToolStatusDisplay
      :is-connected="isToolConnected()"
      :is-running="isToolRunning()"
      :tool-name="statusParameterToolName"
      :detect-alarm="detectAlarm"
      :current-temperature="currentTemperature"
      :current-pressure="currentPressure"
      :show-settings-display-modal-button="true"
      @open-settings-modal="openSettingsModal"
    />

    <!-- 설정 모달 -->
    <KcBoilerToolSettingsDisplayModal
      v-model="showSettingsModal"
      :is-connected="isToolConnected()"
      :is-running="isToolRunning()"
      :tool-name="statusParameterToolName"
      :detect-alarm="detectAlarm"
      :current-temperature="currentTemperature"
      :current-pressure="currentPressure"
      :status-parameters="widgetConfig.statusParameters"
      :parameter-values="parameterValues"
    />
  </div>
</template>

<script setup>
import { computed, defineProps, ref, watch } from 'vue'
import { useWidgetRefresh, widgetProps } from 'src/common/module/widgetCommon'
import ParameterDataService from 'src/services/parameterData/ParameterDataService'
import ParameterService from 'src/services/modeling/ParameterService'
import BoilerToolStatusDisplay from './BoilerToolStatusDisplay.vue'
import KcBoilerToolSettingsDisplayModal from './KcBoilerToolSettingsDisplayModal.vue'
import { pt } from 'src/plugins/i18n'

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
const detectAlarm = ref(false) // 알람 감지 상태

const currentTemperature = computed(() => {
  const { currentTemperatureParameterId } = widgetConfig.value
  if (!currentTemperatureParameterId) {
    return '-'
  }
  const value = parameterValues.value[currentTemperatureParameterId]
  return value ? formatValue(value) : '-'
})

const currentPressure = computed(() => {
  const { currentPressureParameterId } = widgetConfig.value
  if (!currentPressureParameterId) {
    return '-'
  }
  const value = parameterValues.value[currentPressureParameterId]
  return value ? formatValue(value) : '-'
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
    processNameParameterId,
    processErrorMessageParameterId,
    connectionParameterId,
    currentTemperatureParameterId,
    currentPressureParameterId,
    statusParameters,
    columnsPerTable: configColumnsPerTable,
  } = config

  if (configColumnsPerTable) {
    columnsPerTable.value = configColumnsPerTable
  }

  // 새로운 config 구조에서 파라미터 ID들을 추출
  const parameterIds = [
    statusParameterId,
    productionCountParameterId,
    processNameParameterId,
    processErrorMessageParameterId,
    connectionParameterId,
    currentTemperatureParameterId,
    currentPressureParameterId,
    ...statusParameters.map((it) => it.parameterId),
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
      acc[data.parameterId] = {
        ...data,
        name: pt(data.name),
      }
    }
    return acc
  }, {})

  // 에러 알람이 활성화 된 것이 있는지 검사
  const alarmParameters = computed(() => statusParameters.filter((param) => param.type === 'alarm'))
  const detectAlarmCount = alarmParameters.value
    .map((param) => parameterValues.value[param.parameterId])
    .filter(Boolean)
    .map((it) => it.value)
    .reduce((acc, cur) => acc + cur, 0)

  detectAlarm.value = detectAlarmCount > 0
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
