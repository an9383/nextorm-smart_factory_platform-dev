<template>
  <div class="column col full-height">
    <div class="chart-wrapper">
      <!-- 반원 게이지 차트 영역 (ECharts) -->
      <div class="chart-container">
        <v-chart :option="chartOption" autoresize class="gauge-chart" ref="chartRef" />
      </div>

      <!-- 추가 정보 -->
      <div class="info-row">
        <div class="info-item">
          <div class="info-label">{{ $t('현재값') }}</div>
          <div class="info-value">{{ currentValue.toLocaleString() }}</div>
        </div>
        <div class="info-item">
          <div class="info-label">{{ $t('최대값') }}</div>
          <div class="info-value">{{ maxValue.toLocaleString() }}</div>
        </div>
      </div>
    </div>
    <q-resize-observer @resize="onResize" />
  </div>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'
import { useWidgetRefresh, widgetProps } from 'src/common/module/widgetCommon'
import ParameterDataService from 'src/services/parameterData/ParameterDataService'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { GaugeChart } from 'echarts/charts'
import { CanvasRenderer } from 'echarts/renderers'

use([GaugeChart, CanvasRenderer])

const props = defineProps(widgetProps)

const parameterData = ref(null)
const chartRef = ref(null)
const FRACTION_DIGITS = 1

// 생산율 임계값 및 색상 정의 (퍼센트 기준)
// GOOD: 70% 이상 (녹색), FAIR: 40~70% (노란색), POOR: 40% 미만 (빨간색)
const RATE_THRESHOLDS = {
  GOOD: 70,
  FAIR: 40,
}

const RATE_COLORS = {
  GOOD: '#4caf50',
  FAIR: '#ffc107',
  POOR: '#f44336',
}

const GAUGE_CONFIG = {
  RADIUS: '100%',
  CENTER: ['50%', '65%'],
  LINE_WIDTH: 20,
  VALUE_FONT_SIZE: 30,
  UNIT_FONT_SIZE: 16,
  VALUE_OFFSET: [0, '12%'],
}

const widgetConfig = computed(() => props?.config || {})

// 현재값 (파라미터 값)
const currentValue = computed(() => {
  if (!parameterData.value) return 0
  const value = Number(parameterData.value.value)
  return isNaN(value) ? 0 : value
})

// 최대값
const maxValue = computed(() => {
  const max = Number(widgetConfig.value.maxValue)
  return isNaN(max) || max <= 0 ? 100 : max
})

// 생산율 계산 (현재값 / 최대값 * 100)
const productionRate = computed(() => {
  if (currentValue.value === 0 || maxValue.value === 0) {
    return 0
  }
  const rate = (currentValue.value / maxValue.value) * 100
  return Math.min(rate, 100) // 최대 100%로 제한
})

// 생산율에 따른 동적 색상
const rateColor = computed(() => {
  const rate = productionRate.value
  if (rate >= RATE_THRESHOLDS.GOOD) return RATE_COLORS.GOOD
  if (rate >= RATE_THRESHOLDS.FAIR) return RATE_COLORS.FAIR
  return RATE_COLORS.POOR
})

// 게이지 값 포맷터
const formatGaugeValue = (value) => {
  return productionRate.value === 0 ? '-' : `{value|${value.toFixed(FRACTION_DIGITS)}}{unit|%}`
}

// 게이지 detail 설정
const gaugeDetailConfig = computed(() => ({
  formatter: formatGaugeValue,
  rich: {
    value: {
      fontSize: GAUGE_CONFIG.VALUE_FONT_SIZE,
      fontWeight: 'bold',
      color: '#333',
    },
    unit: {
      fontSize: GAUGE_CONFIG.UNIT_FONT_SIZE,
      color: '#333',
      padding: [0, 0, 0, 3],
    },
  },
  offsetCenter: GAUGE_CONFIG.VALUE_OFFSET,
}))

// ECharts 반원 게이지 차트 옵션
const chartOption = computed(() => ({
  series: [
    {
      type: 'gauge',
      startAngle: 180,
      endAngle: 0,
      min: 0,
      max: 100,
      radius: GAUGE_CONFIG.RADIUS,
      center: GAUGE_CONFIG.CENTER,
      axisLine: {
        lineStyle: {
          width: GAUGE_CONFIG.LINE_WIDTH,
          color: [
            [productionRate.value / 100, rateColor.value],
            [1, '#e0e0e0'],
          ],
        },
      },
      pointer: { show: false },
      axisTick: { show: false },
      splitLine: { show: false },
      axisLabel: { show: false },
      detail: gaugeDetailConfig.value,
      data: [{ value: productionRate.value }],
    },
  ],
}))

const refresh = async (config) => {
  const { parameterId, dataInterval = 30 } = config

  if (!parameterId) {
    parameterData.value = null
    return
  }
  const recentData = await ParameterDataService.getLatestDataWithinPeriod([parameterId], dataInterval)
  parameterData.value = recentData.find((d) => d.parameterId === parameterId) || null
}

const onResize = () => {
  nextTick(() => {
    if (chartRef.value) {
      chartRef.value.resize()
    }
  })
}

useWidgetRefresh(refresh)
</script>

<style scoped>
.chart-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  width: 100%;
  height: 100%;
}

.chart-container {
  position: relative;
  width: 100%;
  flex: 1 1 auto;
  min-height: 100px;
  overflow: hidden;
}

.gauge-chart {
  width: 100%;
  height: 100%;
}

.info-row {
  display: flex;
  justify-content: space-around;
  align-items: center;
  width: 100%;
  margin-top: -1.5rem;
  gap: 2rem;
  padding: 0.8rem 0;
}

.info-item {
  flex: 1;
  text-align: center;
  padding: 0;
}

.info-label {
  font-size: 1rem;
  color: #666;
  margin-bottom: 0.8rem;
  line-height: 1.2;
}

.info-value {
  font-size: 1rem;
  font-weight: 600;
  color: #333;
  line-height: 1.2;
}
</style>
