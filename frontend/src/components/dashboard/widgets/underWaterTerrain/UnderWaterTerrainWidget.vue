<template>
  <div class="full-width full-height chart_container no-padding row flex">
    <div class="col q-pl-sm">
      <Gio3DMap v-if="gioData" :data="gioData"></Gio3DMap>
    </div>
  </div>
</template>

<script setup>
// import _ from 'lodash'
import { defineProps, ref } from 'vue'
import { useWidgetRefresh, widgetProps } from '/src/common/module/widgetCommon'
import { date } from 'quasar'
import Gio3DMap from 'src/components/chart/Gio3DMap/Gio3DMap.vue'
import ParameterDataService from 'src/services/parameterData/ParameterDataService'
import ReservoirLayoutService from 'src/services/waterQuality/ReservoirLayoutService'

defineProps(widgetProps)

//데이터 조회(refresh)
const gioData = ref({})

const refresh = async (config) => {
  await loadUnderWaterTerrain(config)
}
useWidgetRefresh(refresh)

async function loadUnderWaterTerrain(config) {
  const { toolId, period } = config

  let fromDate = null
  let toDate = null
  if (period === 'PERIOD') {
    //기간 선택
    toDate = new Date(config.to)
    fromDate = new Date(config.from)
  } else {
    toDate = new Date().toISOString()
    fromDate = date.subtractFromDate(toDate, { minutes: period }).toISOString()
  }

  const areaData = await getAreaData(toolId)
  const data = await ParameterDataService.getUnderWaterTerrainData(toolId, fromDate, toDate)

  gioData.value = { areaData, data }
}

const getAreaData = async (toolId) => {
  const layouts = await ReservoirLayoutService.getReservoirLayout({ params: { toolIds: [toolId] } })
  let data = JSON.parse(layouts[0]?.data)
  return data.markers
}
</script>
