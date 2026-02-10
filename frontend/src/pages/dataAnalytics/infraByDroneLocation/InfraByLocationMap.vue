<template>
  <LeafletMap ref="map" class="q-py-md col" :zoom="zoom" :min-zoom="12" v-model:center="center" @ready="onMapReady" />
</template>

<script setup>
import { ref, shallowRef, watch } from 'vue'
import L from 'leaflet'
import 'leaflet.markercluster'
import 'leaflet.markercluster/dist/MarkerCluster.css'
import 'leaflet.markercluster/dist/MarkerCluster.Default.css'
import '@kalisio/leaflet.donutcluster'
import '@kalisio/leaflet.donutcluster/src/Leaflet.DonutCluster.css'
import 'leaflet-extra-markers'
import 'leaflet-extra-markers/dist/css/leaflet.extra-markers.min.css'
import { useI18n } from 'vue-i18n'
import MapService from 'src/services/waterQuality/MapService'
import useUI from 'src/common/module/ui'
import LeafletMap from 'components/map/LeafletMap.vue'

const props = defineProps({
  latitude: {
    type: Number,
    required: true,
  },
  longitude: {
    type: Number,
    required: true,
  },
})

const { t } = useI18n()
const ui = useUI()

const map = shallowRef(null)
const zoom = ref(14)
const infraDatas = ref([])
const center = ref([props.latitude, props.longitude])
let markers

const onMapReady = () => {
  const bounds = map.value.leafletMap.getBounds()
  const center = bounds.getCenter()
  getInfra(center)
}

const getInfra = async (center) => {
  infraDatas.value = []
  if (zoom.value <= 11) return
  ui.loading.show()
  const data = await MapService.getInfraByLocation(center.lat, center.lng, calculateRadius())
  ui.loading.hide()
  infraDatas.value = data
  updateMarkerCluster()
}

const calculateRadius = () => {
  const bounds = map.value.leafletMap.getBounds()
  const center = bounds.getCenter()
  const northEast = bounds.getNorthEast()

  const radius = center.distanceTo(northEast)

  return Math.floor(radius)
}

watch(
  () => center.value,
  () => {
    getInfra(center.value)
  },
)
watch([() => props.latitude, () => props.longitude], () => {
  center.value = { lat: props.latitude, lng: props.longitude }
})

const updateMarkerCluster = () => {
  removeLayer()
  markers = createDonutMarker()
  infraDatas.value.forEach((infraData) => {
    const loweredType = infraData.type.toLowerCase()
    const iconAndColor = getIconAndColor(loweredType)

    const title = infraData.name || t('이름 없음')
    const typeName = t(loweredType)
    const marker = L.marker([infraData.lat, infraData.lon], {
      title: typeName,
      icon: createMarkerIcon(iconAndColor),
    })
    marker.bindPopup(title + '<br>' + typeName)
    markers.addLayer(marker)
  })
  map.value.leafletMap.addLayer(markers)
}

const removeLayer = () => {
  if (markers) {
    map.value.leafletMap.removeLayer(markers)
  }
}

const createDonutMarker = () => {
  return L.DonutCluster(
    {
      chunkedLoading: false,
    },
    {
      key: 'title',
      arcColorDict: {
        [t('restaurant')]: 'blue',
        [t('industrial')]: 'red',
        [t('cafe')]: 'yellow',
      },
    },
  )
}

const createMarkerIcon = (iconAndColor) => {
  return L.ExtraMarkers.icon({
    icon: iconAndColor.icon,
    markerColor: iconAndColor.color,
    shape: 'circle',
    prefix: 'fa',
  })
}

const getIconAndColor = (type) => {
  if (type === 'restaurant') {
    return { icon: 'fa-utensils', color: 'blue' }
  } else if (type === 'cafe') {
    return { icon: 'fa-coffee', color: 'yellow' }
  } else {
    return { icon: 'fa-industry', color: 'red' }
  }
}
</script>

<style scoped></style>
