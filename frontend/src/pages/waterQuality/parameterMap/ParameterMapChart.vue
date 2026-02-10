<template>
  <map-gps-chart :datas="chartData" :markers="markers" :date-range="dateRange"></map-gps-chart>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { date } from 'quasar'
import ReservoirLayoutService from 'src/services/waterQuality/ReservoirLayoutService.js'
import MapGpsChart from 'src/components/chart/MapGpsChart.vue'
import ParameterDataService from 'src/services/parameterData/ParameterDataService'
import useUI from 'src/common/module/ui'

const props = defineProps({
  toolId: {
    type: Number || String,
    required: true,
  },
  parameterId: {
    type: Number || String,
    required: true,
  },
  fromDateTime: {
    type: Date,
    required: false,
    default: date.subtractFromDate(Date.now(), { days: 1 }),
  },
  toDateTime: {
    type: Date,
    required: false,
    default: new Date(),
  },
})

watch(
  () => [props.toolId, props.parameterId, props.fromDateTime, props.toDateTime],
  () => {
    loadChartData()
  },
)

const ui = useUI()
// const dateRange = ref({ from: fromDay, to: toDay })

const chartData = ref([])
const markers = ref([])
const dateRange = ref()

onMounted(() => {
  loadChartData()
})

const loadChartData = async () => {
  const { toolId, parameterId, fromDateTime, toDateTime } = props
  if (!toolId || !parameterId || !fromDateTime || !toDateTime) {
    return
  }
  ui.loading.show()

  try {
    let layouts = await ReservoirLayoutService.getReservoirLayout({ params: { toolIds: toolId } })
    let data = JSON.parse(layouts[0].data)

    markers.value = data.markers

    const fromDate = new Date(fromDateTime).toISOString()
    const toDate = new Date(toDateTime).toISOString()
    const parameterDatas = await ParameterDataService.getEcoParameters(parameterId, fromDate, toDate)
    chartData.value = parameterDatas.datas
      .filter((d) => d[1] != null && d[2] != null && d[3] != null)
      .map((d) => [d[3], d[2], d[1], d[0]])

    dateRange.value = { fromDate: new Date(fromDate), toDate: new Date(toDate) }
  } finally {
    ui.loading.hide()
  }
}
</script>

<style lang="scss"></style>
