<template>
  <div class="full-width full-height">
    <LeafletMap ref="map" :center="center" :min-zoom="12" v-model:zoom="zoom" @ready="onMapReady">
      <LControl position="topright">
        <div class="legend">
          <div class="gradient"></div>
          <div class="labels">
            <div v-for="label in legendLabel" :key="label" class="label">
              {{ label }}
            </div>
          </div>
        </div>
      </LControl>
      <LCircleMarker
        class-name="leaflet-marker"
        v-for="(data, index) in heatmapData"
        :key="index"
        :lat-lng="[data.lat, data.lng]"
        :fill-opacity="opacity"
        :fill-color="getColor(data.value)"
        :radius="10"
        :opacity="0"
      >
        <LPopup :options="{ autoPan: false }">
          <div>
            <b>{{ $t('point') }}</b> :
            <a @click="openChart(data)" style="cursor: pointer"
              ><u>{{ data.value }}</u></a
            ><br />
            <b>{{ $t('latitude') }}</b> : {{ data.lat }}<br />
            <b>{{ $t('longitude') }}</b> : {{ data.lng }}
          </div>
        </LPopup>
      </LCircleMarker>
    </LeafletMap>
  </div>

  <WaterQualityHeatMapWidgetChart
    v-model="isShowRadarChart"
    :health-data="healthData"
    :latitude="selectedLat"
    :longitude="selectedLng"
    @close="onClose"
  />
  <q-resize-observer @resize="onResize" />
</template>

<script setup>
import { defineProps, ref, shallowRef, watch } from 'vue'
import { LCircleMarker, LControl, LPopup } from '@vue-leaflet/vue-leaflet'
import WaterQualityHeatMapWidgetChart from 'components/dashboard/widgets/waterQualityHeatMap/WaterQualityHeatMapWidgetChart.vue'
import { useWidgetRefresh, widgetProps } from 'src/common/module/widgetCommon'
import HealthDataService from 'src/services/parameterData/HealthDataService'
import { date } from 'quasar'
import LeafletMap from 'components/map/LeafletMap.vue'
import ToolService from 'src/services/modeling/ToolService'

const { config } = defineProps(widgetProps)
const zoom = shallowRef(14)
const center = shallowRef([35.849, 128.4123])
const selectedLat = shallowRef(null)
const selectedLng = shallowRef(null)
const healthData = shallowRef(null)
const isShowRadarChart = ref(false)
const heatmapData = shallowRef([])
const map = shallowRef(null)
const tool = shallowRef(null)
const legendLabel = ['100', '80', '50']

const opacity = shallowRef(0.1)

let isMapReady = false

const onMapReady = async () => {
  isMapReady = true
  const { toolId } = config
  tool.value = await ToolService.getToolById(toolId)
  center.value = [tool.value.location.latitude, tool.value.location.longitude]
  refreshAction()
}

const onResize = () => {
  map.value.leafletMap?.invalidateSize()
}

watch(
  () => zoom.value,
  () => {
    opacity.value = Math.max(((zoom.value - 10) / 10) * 3 - 1.1, 0.1)
  },
)

const refresh = async () => {
  if (!isMapReady) return

  const { period, parameterIds } = config
  let to, from

  if (period === 'PERIOD') {
    to = new Date(config.to)
    from = new Date(config.from)
  } else {
    to = new Date()
    from = date.subtractFromDate(to, { minutes: period })
  }

  const parametersHealthGrade = await HealthDataService.getHeatMapHealthData(
    parameterIds,
    from.toISOString(),
    to.toISOString(),
  )

  heatmapData.value = parametersHealthGrade.map((data) => {
    return {
      lat: data.latitude,
      lng: data.longitude,
      value: data.healthScore,
      healthData: data.healthData,
    }
  })
}

const getColor = (value) => {
  value = value / 100
  if (value <= 0.5) return '#FF0000'
  if (value < 0.6) return '#FF8000'
  if (value < 0.7) return '#C8B12D'
  if (value < 0.8) return '#81C633'
  return '#3ad33a'
}

const openChart = (data) => {
  isShowRadarChart.value = true
  selectedLat.value = data.lat
  selectedLng.value = data.lng
  healthData.value = data.healthData
}

const onClose = () => {
  isShowRadarChart.value = false
}

const { refreshAction } = useWidgetRefresh(refresh)
</script>

<style scoped>
.legend {
  position: relative;
  padding: 10px;
  background-color: white;
  border: 1px solid #ccc;
  border-radius: 5px;
  opacity: 0.8;
  width: 60px;
}

.gradient {
  width: 20px;
  height: 100px;
  background: linear-gradient(to bottom, #3ad33a, #ff8000, #ff0000);
}

.labels {
  position: absolute;
  left: 30px;
  top: 10px;
  height: 100px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.label {
  text-align: center;
  font-size: 12px;
  margin-left: 5px;
}
</style>
