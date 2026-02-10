<template>
  <div class="full-width full-height" v-if="hasData">
    <bar-chart :datas="chartData" />
  </div>
  <div v-else class="col no-data">
    <div style="text-align: center">
      <q-icon class="flex-center" name="mdi-database-remove" size="50px" color="primary" />
      <div class="text-body-2 q-my-sm">
        {{ $t('조회된 데이터가 없습니다.') }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps, ref } from 'vue'
import { useWidgetRefresh, widgetProps } from 'src/common/module/widgetCommon'
import ParameterDataService from 'src/services/parameterData/ParameterDataService'
import BarChart from 'src/components/chart/BarChart.vue'

defineProps(widgetProps)

const hasData = ref(false)
const chartData = ref([])

const refresh = async (config) => {
  const { dataInterval, parameters } = config

  const parameterIds = parameters.filter((set) => set.id != null).map((set) => set.id)

  if (parameterIds.length === 0) {
    return
  }
  const outputData = await ParameterDataService.getLatestDataWithinPeriod(parameterIds, dataInterval)

  if (!outputData || outputData.length === 0) {
    hasData.value = false
    return
  }

  hasData.value = true

  // 생산량 기준으로 내림차순 정렬 (적은 것부터 많은 것 순 - 가로 막대 차트용)
  const sortedData = [...outputData].sort((a, b) => b.value - a.value)

  // 색상 배열
  const colors = [
    '#42A5F5', // 파랑
    '#EF5350', // 빨강
    '#66BB6A', // 초록
    '#FFA726', // 주황
    '#AB47BC', // 보라
    '#29B6F6', // 하늘색
    '#EC407A', // 분홍
    '#26A69A', // 청록
    '#FFEE58', // 노랑
    '#FF7043', // 주황빨강
  ]

  // BarChart 컴포넌트가 필요한 형식으로 변환
  chartData.value = sortedData.map((item, index) => {
    // parameters 배열에서 해당 id에 대한 toolDisplayName 찾기
    const parameterConfig = parameters.find((param) => param.id === item.parameterId)
    // toolDisplayName가 있으면 사용, 없으면 toolName 사용
    const displayName = parameterConfig?.toolDisplayName || item.toolName

    return {
      category: displayName,
      value: item.value,
      color: colors[index % colors.length],
    }
  })
}

useWidgetRefresh(refresh)
</script>

<style scoped>
.no-data {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}
</style>
