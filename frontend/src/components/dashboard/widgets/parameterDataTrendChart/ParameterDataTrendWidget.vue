<template>
  <div class="full-width full-height" v-if="isNoData">
    <trend-chart :datas="chartData" />
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
import ParameterDataService from '/src/services/parameterData/ParameterDataService'
import TrendChart from 'components/chart/TrendChart.vue'
import { useWidgetRefresh, widgetProps } from '/src/common/module/widgetCommon'
import { date } from 'quasar'
import { pt } from '/src/plugins/i18n'

defineProps(widgetProps)

//데이터 조회(refresh)
const chartData = ref(null)
const isNoData = ref(false)

const refresh = async (config) => {
  const { period, parameterIds } = config

  let from, to
  if (period === 'PERIOD') {
    //기간 선택
    to = new Date(config.to)
    from = new Date(config.from)
  } else {
    to = new Date().toISOString()
    from = date.subtractFromDate(to, { minutes: period }).toISOString()
  }

  const parametersTrendData = await ParameterDataService.getMultiParameterDataTrend(parameterIds, from, to)
  chartData.value = parametersTrendData.map((v) => ({
    name: pt(v.parameterName),
    trendData: v.rawDatas.map((k) => ({
      x: new Date(k.traceAt),
      y: k.value,
    })),
  }))
  const mergeDatas = chartData.value.flatMap((data) => data.trendData)
  if (mergeDatas.length > 0) {
    isNoData.value = true
  } else {
    isNoData.value = false
  }
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
