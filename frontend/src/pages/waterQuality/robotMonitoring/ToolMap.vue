<template>
  <LeafletMap ref="map" v-model:zoom="zoom" :center="center" @ready="onReady">
    <l-marker v-for="(location, key) in locations" :key="key" :lat-lng="location.latLng" :icon="createIcon(location)">
      <l-popup>
        <p class="location-title">{{ $t('위치') + ' : ' + location.name }}</p>
        <ul class="tool-list">
          <li v-for="tool in location.tools" :key="tool.id" @click="handleToolClick(tool)">
            <a :class="selectedTool?.id === tool.id ? 'selected' : ''">{{ tool.name }}</a>
          </li>
        </ul>
      </l-popup>
    </l-marker>

    <!-- 중심점 표시 (디버그용) -->
    <l-marker v-if="center !== null" :lat-lng="center">
      <l-icon class-name="custom-leaflet-div-icon">
        <div class="circle" :style="centerIconStyle"></div>
      </l-icon>
    </l-marker>
  </LeafletMap>
</template>

<script setup>
import L from 'leaflet'
import '@ansur/leaflet-pulse-icon/dist/L.Icon.Pulse'
import '@ansur/leaflet-pulse-icon/dist/L.Icon.Pulse.css'
import { LIcon, LMarker, LPopup } from '@vue-leaflet/vue-leaflet'
import { computed, ref, shallowRef } from 'vue'
import LeafletMap from 'components/map/LeafletMap.vue'

const props = defineProps({
  tools: {
    type: Array,
    required: true,
  },
})

const createLocationKey = (tool) => `${tool.location.latitude}__${tool.location.longitude}`
const locations = props.tools.reduce((acc, tool) => {
  const key = createLocationKey(tool)
  const findLocation = acc[key]
  if (!findLocation) {
    acc[key] = {
      name: tool.location.name,
      latitude: tool.location.latitude,
      longitude: tool.location.longitude,
      latLng: [tool.location.latitude, tool.location.longitude],
      tools: [tool],
    }
    return acc
  }
  findLocation.tools.push(tool)
  return acc
}, {})

const coordinates = Object.keys(locations).map((key) => {
  const location = locations[key]
  return location.latLng
})

const map = shallowRef(null)
const zoom = ref(13)
const center = ref(null)

const selectedTool = ref(props.tools[0])

const emit = defineEmits({
  selectTool: (tool) => tool,
})

const createIcon = (location) => {
  const includeSelectedTool = location.tools.some(
    (tool) => tool.id === selectedTool.value.id && location.latitude && location.longitude,
  )
  if (includeSelectedTool) {
    const color = 'rgb(23, 173, 0)'
    return new L.icon.pulse({ iconSize: [10, 10], color: color, fillColor: color })
  }
  return new L.icon.pulse({ iconSize: [10, 10], color: 'red' })
}

const onReady = () => {
  if (coordinates.length === 1) {
    zoom.value = 12
    return
  }

  map.value.leafletMap.fitBounds(coordinates, {
    padding: [30, 30],
  })
}

const centerIconStyle = computed(() => {
  return {
    width: `${zoom.value * 1}px`,
    height: `${zoom.value * 1}px`,
  }
})

const handleToolClick = (tool) => {
  // 같은 툴 선택하면 아무것도 하지 않게함
  if (tool.id === selectedTool.value.id) {
    return
  }
  selectedTool.value = tool
  emit('selectTool', tool)
}

// 중심좌표 계산
const getCenterOfCoordinates = (coords) => {
  if (coords.length === 1) {
    return coords[0]
  }

  let x = 0.0
  let y = 0.0
  let z = 0.0

  for (let coord of coords) {
    let latitude = (coord[0] * Math.PI) / 180
    let longitude = (coord[1] * Math.PI) / 180

    x += Math.cos(latitude) * Math.cos(longitude)
    y += Math.cos(latitude) * Math.sin(longitude)
    z += Math.sin(latitude)
  }

  let total = coords.length

  x = x / total
  y = y / total
  z = z / total

  let centralLongitude = Math.atan2(y, x)
  let centralSquareRoot = Math.sqrt(x * x + y * y)
  let centralLatitude = Math.atan2(z, centralSquareRoot)

  return [(centralLatitude * 180) / Math.PI, (centralLongitude * 180) / Math.PI]
}

const calculateCenter = () => {
  center.value = getCenterOfCoordinates(coordinates)
}

calculateCenter()
emit('selectTool', selectedTool.value)
</script>

<style scoped lang="scss">
.select-tool-info {
  color: red;
  font-weight: bold;
  background-color: lightgrey;
  padding: 5px;
}

.location-title {
  margin: 0px;
  padding: 10px 20px;
  font-weight: bold;
}

.tool-list {
  margin-top: 10px;
  padding-left: 0;
  list-style: none;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;

  li {
    display: flex;
    cursor: pointer;
    a {
      width: 100%;
      padding: 6px 10px;
      background-color: var(--mainBgColor);
      color: var(--mainColor);
      border-radius: 4px;
      border: 1px solid var(--mainColor);
      &.selected {
        background-color: var(--mainColor);
        color: #fff;
      }
    }
  }
}
</style>
