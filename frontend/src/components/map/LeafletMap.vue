<template>
  <l-map v-model:zoom="zoom" v-model:center="center" @ready="onMapReady">
    <l-tile-layer
      :url="mapData.tileUrl"
      :attribution="mapData.tileAttribution"
      :max-zoom="maxZoom"
      :min-zoom="minZoom"
    />
    <l-control-scale :imperial="false" :metric="true" />

    <l-control position="topright">
      <q-fab color="white" text-color="secondary" :icon="fabIcon" class="map-select" direction="left" padding="sm">
        <q-fab-action
          color="white"
          text-color="secondary"
          icon="satellite_alt"
          @click="onSelectMap(tile.satellite)"
        ></q-fab-action>
        <q-fab-action
          color="white"
          text-color="secondary"
          icon="layers"
          @click="onSelectMap(tile.roadmap)"
        ></q-fab-action>
      </q-fab>
    </l-control>
    <slot />
  </l-map>
</template>
<script setup>
import { LControl, LControlScale, LMap, LTileLayer } from '@vue-leaflet/vue-leaflet'
import { computed, defineModel, shallowRef } from 'vue'
import 'leaflet'
import 'leaflet/dist/leaflet.css'
import { useAuthStore } from 'stores/auth'
import { storeToRefs } from 'pinia'

const { maxZoom, minZoom } = defineProps({
  maxZoom: {
    type: Number,
    required: false,
    default: () => 18,
  },
  minZoom: {
    type: Number,
    required: false,
    default: () => 9,
  },
})
const map = shallowRef(null)
const center = defineModel('center', {
  type: Array,
  required: false,
  default: () => [35.84902197250592, 128.4123229980469],
})
const zoom = defineModel('zoom', {
  type: Number,
  required: false,
  default: () => 14,
})

const authStore = useAuthStore()
const { userSettings } = storeToRefs(authStore) //Store state를 컴포넌트 state로 변경
const tile = {
  satellite: 'SATELLITE',
  roadmap: 'ROADMAP',
}

const defaultMapType = tile.satellite

const onMapReady = (mapObject) => {
  map.value = mapObject
}

const mapType = computed(() => {
  if (userSettings.value === null) return defaultMapType
  return userSettings.value.mapType || defaultMapType
})

defineExpose({ leafletMap: map })

const onSelectMap = async (type) => {
  const setting = { mapType: type }
  await authStore.updateUserSetting(setting)
}

const fabIcon = computed(() => {
  return mapType.value === tile.satellite ? 'satellite_alt' : 'layers'
})

const mapData = computed(() => {
  let tileUrl, tileAttribution
  if (mapType.value.toUpperCase() === tile.satellite) {
    tileUrl = 'https://api.maptiler.com/maps/hybrid/{z}/{x}/{y}.jpg?key=xESu6RoAkQvRFD3Ep1oO'
    tileAttribution =
      '<a href="https://www.maptiler.com/copyright/" target="_blank">&copy; MapTiler</a> <a href="https://www.openstreetmap.org/copyright" target="_blank">&copy; OpenStreetMap contributors</a>'
  } else {
    tileUrl = 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png'
    tileAttribution = '&copy; OpenStreetMap contributors'
  }
  return { tileUrl, tileAttribution }
})
</script>

<style scoped>
.map-select {
  opacity: 0.8;
}
</style>
