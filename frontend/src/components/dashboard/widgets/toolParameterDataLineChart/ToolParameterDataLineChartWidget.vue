<template>
  <div class="full-width full-height" v-if="hasData">
    <trend-chart :datas="chartData" :show-legend="false" />
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
import TrendChart from 'src/components/chart/TrendChart.vue'
import { date } from 'quasar'

defineProps(widgetProps)

const hasData = ref(false)
const chartData = ref([])

const refresh = async (config) => {
  const { period, parameters } = config
  const parameterIds = parameters.filter((set) => set.id != null).map((set) => set.id)

  if (parameterIds.length === 0) {
    return
  }
  let from, to
  if (period === 'PERIOD') {
    //기간 선택
    to = new Date(config.to)
    from = new Date(config.from)
  } else {
    to = new Date()
    from = date.subtractFromDate(to, { minutes: period })
  }

  const lineChartData = await ParameterDataService.getMultiParameterDataTrend(
    parameterIds,
    from.toISOString(),
    to.toISOString(),
  )

  if (!lineChartData || lineChartData.length === 0) {
    hasData.value = false
    return
  }
  const trendDataArray = lineChartData
    .filter((item) => Array.isArray(item.rawDatas) && item.rawDatas.length > 0)
    .map((item) => {
      const parameterConfig = parameters.find((param) => param.id === item.parameterId)
      return {
        name: parameterConfig?.toolDisplayName || `${item.toolName} - ${item.parameterName}`,
        trendData: item.rawDatas.map((data) => ({
          x: new Date(data.traceAt).getTime(),
          y: data.value,
        })),
      }
    })

  if (trendDataArray.length === 0) {
    hasData.value = false
    return
  }

  hasData.value = true
  chartData.value = trendDataArray
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
