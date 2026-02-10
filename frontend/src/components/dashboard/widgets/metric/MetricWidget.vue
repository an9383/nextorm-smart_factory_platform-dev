<template>
  <div class="metric-widget">
    <div v-if="metricsData.length === 0" class="flex items-center justify-center" style="min-height: 200px">
      <div class="text-grey-6 text-center">
        <q-icon name="info" size="48px" class="q-mb-md" />
        <div class="text-body1">{{ $t('메트릭 데이터가 설정되지 않았습니다.') }}</div>
      </div>
    </div>
    <div v-else class="metrics-container">
      <div
        v-for="(metric, index) in metricsData"
        :key="index"
        class="metric-item"
        :style="{ ...getMetricItemStyle, backgroundColor: getMetricColor(metric) }"
      >
        <div class="metric-label" :style="{ fontSize: labelFontSize }">{{ metric.label }}</div>
        <div class="metric-value" :style="{ fontSize: valueFontSize }">
          <template v-if="!hasNoValue(metric.operatingStatus)">
            <span v-if="isOperating(metric)">
              {{ formatMetricValue(metric.metricValue, metric.unit) }}
            </span>
            <span v-else class="not-available">N/A</span>
          </template>
          <template v-else>
            {{ formatMetricValue(metric.metricValue, metric.unit) }}
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, defineProps, ref } from 'vue'
import { useWidgetRefresh, widgetProps } from 'src/common/module/widgetCommon'
import ParameterDataService from 'src/services/parameterData/ParameterDataService'
import ParameterService from 'src/services/modeling/ParameterService'

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

const metricsData = ref([])
const parameterInfoCache = ref(new Map()) // 파라미터 정보 캐시 (parameterId -> parameter info)

const widgetConfig = computed(() => props?.config || {})

// 0을 제외한 falsy 값 체크 (null, undefined, 빈 문자열)
const hasNoValue = (value) => {
  return value === null || value === undefined || value === ''
}

// 글씨 크기 설정
const labelFontSize = computed(() => `${widgetConfig.value.labelFontSize || 12}px`)
const valueFontSize = computed(() => `${widgetConfig.value.valueFontSize || 16}px`)

// 한 줄에 표시할 메트릭 개수에 따라 아이템 너비 계산
const getMetricItemStyle = computed(() => {
  const itemsPerRow = widgetConfig.value.itemsPerRow || 12
  const gapSize = 8 // gap 크기 (px) - 이전 4px의 2배
  const totalGapWidth = gapSize * (itemsPerRow - 1) // 총 gap 너비
  return {
    width: `calc(${100 / itemsPerRow}% - ${totalGapWidth / itemsPerRow}px)`,
    minWidth: '80px',
  }
})

// 메트릭 색상 결정 함수
const getMetricColor = (metric) => {
  const { metricValue, colorThresholds, operatingStatus } = metric

  // 비가동 상태인 경우 어두운 회색
  if (!hasNoValue(operatingStatus)) {
    const status = parseFloat(operatingStatus)
    if (status !== 1) {
      return '#424242' // N/A 색상 (어두운 회색)
    }
  }

  if (hasNoValue(metricValue)) {
    return '#424242' // 값이 없는 경우도 어두운 회색
  }

  if (!colorThresholds) {
    return '#4caf50' // 기본 녹색
  }

  const value = parseFloat(metricValue)
  if (isNaN(value)) {
    return '#424242'
  }

  const { redThreshold, orangeThreshold } = colorThresholds

  if (redThreshold !== null && redThreshold !== undefined && value <= redThreshold) {
    return '#f44336' // 빨강
  }
  if (orangeThreshold !== null && orangeThreshold !== undefined && value <= orangeThreshold) {
    return '#ff9800' // 주황
  }
  return '#4caf50' // 녹색
}

// 메트릭 값 포맷팅
const formatMetricValue = (value, unit) => {
  if (hasNoValue(value)) {
    return 'N/A'
  }

  const numValue = parseFloat(value)
  if (isNaN(numValue)) {
    return value
  }

  // 단위와 함께 표시 (소수점 1자리)
  return `${numValue.toFixed(1)}${unit || ''}`
}

// 가동상태 확인 (1이면 가동 중)
const isOperating = (metric) => {
  const status = metric.operatingStatus
  if (hasNoValue(status)) {
    return true // 가동상태 파라미터가 없으면 항상 값 표시
  }
  return parseFloat(status) === 1
}

const refresh = async (config) => {
  const { dataInterval, metrics, colorThresholds } = config

  if (!metrics || metrics.length === 0) {
    metricsData.value = []
    return
  }

  // 파라미터 ID 수집 (중복 제거)
  const parameterIds = new Set()
  const metricParameterIds = []
  metrics.forEach((metric) => {
    if (metric.metricParameterId) {
      parameterIds.add(metric.metricParameterId)
      metricParameterIds.push(metric.metricParameterId)
    }
    if (metric.operatingStatusParameterId) {
      parameterIds.add(metric.operatingStatusParameterId)
    }
  })

  if (parameterIds.size === 0) {
    metricsData.value = []
    return
  }

  // 파라미터 정보 조회 (캐시되지 않은 것만)
  const uncachedParameterIds = metricParameterIds.filter((id) => !parameterInfoCache.value.has(id))
  if (uncachedParameterIds.length > 0) {
    try {
      const parameterInfoList = await ParameterService.getParameters({ id: uncachedParameterIds })
      parameterInfoList.forEach((paramInfo) => {
        if (paramInfo) {
          parameterInfoCache.value.set(paramInfo.id, paramInfo)
        }
      })
    } catch (error) {
      console.error('파라미터 정보 조회 실패:', error)
    }
  }

  // 최신 데이터 조회
  const recentDataArray = await ParameterDataService.getLatestDataWithinPeriod(Array.from(parameterIds), dataInterval)

  // 데이터를 parameterId를 key로 하는 객체로 변환
  const parameterValues = recentDataArray.reduce((acc, data) => {
    if (data?.parameterId) {
      acc[data.parameterId] = data
    }
    return acc
  }, {})

  // 메트릭 데이터 구성
  metricsData.value = metrics.map((metric) => {
    const metricData = parameterValues[metric.metricParameterId]
    const operatingData = parameterValues[metric.operatingStatusParameterId]
    const parameterInfo = parameterInfoCache.value.get(metric.metricParameterId)

    return {
      label: metric.label || '-',
      metricValue: metricData?.value ?? null,
      operatingStatus: operatingData?.value ?? null,
      colorThresholds: colorThresholds || null,
      unit: parameterInfo?.unit || '', // 파라미터의 unit 사용, 없으면 빈 문자열
    }
  })
}

useWidgetRefresh(refresh)
</script>

<style lang="scss" scoped>
.metric-widget {
  width: 100%;
  height: 100%;
  overflow: auto;
  padding: 8px;
}

.metrics-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.metric-item {
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  color: white;
}

.metric-label {
  text-align: center;
  padding: 8px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.metric-value {
  text-align: center;
  padding: 8px;
  font-weight: 600;
  min-height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;

  .not-available {
    font-weight: 500;
    opacity: 0.9;
  }
}
</style>
