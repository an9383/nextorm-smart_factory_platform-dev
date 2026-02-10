<template>
  <q-page class="q-pa-sm">
    <q-card class="cust_subPage">
      <q-card-section class="row search_section_wrap">
        <q-item class="col-8 search_left">
          <q-item-section>
            <cascader
              v-model="selectedLocation"
              :label="$t('위치')"
              :titles="[$t('사업장'), $t('공장'), $t('라인')]"
              :items="locations"
              item-value="id"
              item-text="name"
              :valid-depth="3"
              class="input-required"
              @update:model-value="onChangeLocation"
            />
          </q-item-section>
          <q-item-section>
            <filterable-select
              v-model="selectedTools"
              :label="$t('설비')"
              :options="toolList"
              option-value="id"
              option-label="name"
              multiple
              @update:model-value="onChangeTool"
            />
          </q-item-section>
          <q-item-section>
            <q-input color="teal" filled v-model="latitude" :label="$t('위도')"></q-input>
          </q-item-section>
          <q-item-section>
            <q-input color="teal" filled v-model="longitude" :label="$t('경도')"></q-input>
          </q-item-section>
        </q-item>
        <q-item class="col-4 search_right">
          <q-item-section>
            <q-toggle
              v-permission:role.create.update
              v-model="isEdit"
              color="secondary"
              :label="$t('수정')"
              left-label
              :disable="selectedTools.length !== 1"
              @click="toggleEditMode"
            />
          </q-item-section>
          <q-item-section>
            <div v-if="isEdit">
              <q-btn
                v-permission:role.update
                class="delete_btn with_icon_btn sBtn"
                @click="removeLastPoint"
                id="deleteButton"
                >{{ $t('마커 삭제') }}
              </q-btn>
              <q-btn v-permission:role.update class="save_btn with_icon_btn sBtn" @click="onSave" id="saveButton"
                >{{ $t('저장') }}
              </q-btn>
            </div>
          </q-item-section>
        </q-item>
      </q-card-section>
      <q-card-section class="column col">
        <LeafletMap
          ref="map"
          :key="isSpliceMode"
          v-model:zoom="zoom"
          :center="[latitude, longitude]"
          @click="addMarker"
          @contextmenu="showMarkerContextMenu($event)"
          :style="{ cursor: isSpliceMode ? 'crosshair' : '' }"
        >
          <l-polyline v-for="line in lines" :key="line" :lat-lngs="line.latLngs" :color="line.color" />
          <l-marker
            v-for="(marker, index) in markers"
            :key="marker"
            :options="{ icon: marker.icon }"
            :lat-lng="marker.latLng"
            :draggable="isEdit"
            @dragend="onDragEnd(index, $event)"
            @contextmenu="showMarkerContextMenu($event)"
          ></l-marker>

          <q-menu touch-position context-menu>
            <q-list v-if="markerContextMenu.isShow && isEdit" class="context-list">
              <q-item
                v-permission:role.delete
                clickable
                v-close-popup
                @click="onClickMarkerContextMenu({ label: $t('마커 삭제'), id: 'DELETE' })"
              >
                <q-item-section>
                  <q-icon color="blue-grey-8" name="mdi-delete-forever" />
                </q-item-section>
                <q-item-section>{{ $t('마커 삭제') }}</q-item-section>
              </q-item>

              <q-item
                v-permission:role.create
                @mouseenter="mouseOver"
                @mouseleave="mouseLeave"
                clickable
                v-close-popup
                @click="onClickMarkerContextMenu({ label: $t('마커 추가'), id: 'ADD' })"
              >
                <q-item-section>
                  <q-icon color="primary" name="add" />
                </q-item-section>
                <q-item-section>{{ $t('마커 추가') }}</q-item-section>
              </q-item>
            </q-list>
          </q-menu>
          <l-control position="topright">
            <div ref="legend" class="leaflet-control leaflet-control-custom">
              <div v-for="(item, index) in legendItems" :key="index" class="flex items-center">
                <div :style="{ backgroundColor: item.color }" class="legend-item"></div>
                <span>{{ item.label }}</span>
              </div>
            </div>
          </l-control>
        </LeafletMap>
      </q-card-section>
    </q-card>
  </q-page>
</template>

<script setup>
import { LControl, LMarker, LPolyline } from '@vue-leaflet/vue-leaflet'
import L from 'leaflet'
import { useI18n } from 'vue-i18n'
import { ref, shallowRef } from 'vue'
import ReservoirLayoutService from 'src/services/waterQuality/ReservoirLayoutService.js'
import Cascader from 'components/cascader/Cascader.vue'
import useUI from 'src/common/module/ui'
import LocationService from 'src/services/modeling/LocationService'
import ToolService from 'src/services/modeling/ToolService'
import _ from 'lodash'
import { filterTreeItem, findTreeItem } from 'src/common/treeUtil'
import LeafletMap from 'components/map/LeafletMap.vue'

const { t } = useI18n()
const zoom = ref(13)
const ui = useUI()
const map = shallowRef(null)
const latitude = ref(35.84902197250592)
const longitude = ref(128.4123229980469)

const selectedLocation = ref(null)
const toolList = ref([])
const locations = ref([])
const selectedTools = ref([])
const reservoirLayout = ref([])

const isEdit = ref(false)
const lastMarkersValue = ref([])
const lines = ref([])
const markers = ref([])

const isSpliceMode = ref(false)

const legendItems = ref([])

let lastToolValue,
  lastLocationValue = null

const markerContextMenu = ref({
  x: 0,
  y: 0,
  isShow: false,
  latLng: [],
})

const showMarkerContextMenu = (e) => {
  if (e.originalEvent.target.tagName === 'IMG') {
    markerContextMenu.value = {
      isShow: true,
      x: e.originalEvent.x,
      y: e.originalEvent.y,
      latLng: e.latlng,
    }
  } else {
    markerContextMenu.value = {
      isShow: false,
      x: 0,
      y: 0,
      latLng: [],
    }
  }
}

const mouseOver = () => {
  const index = lines.value[0].latLngs.findIndex((obj) => _.isEqual(obj, markerContextMenu.value.latLng))
  const newLines = {
    color: 'rgba(255,2,92,0.96)',
    latLngs: [markerContextMenu.value.latLng, lines.value[0].latLngs[index + 1]],
  }
  lines.value = [...lines.value, newLines]
}

const mouseLeave = () => {
  lines.value.length === 2 ? (lines.value = [lines.value[0]]) : (lines.value = [lines.value[0], lines.value[1]])
}

const onClickMarkerContextMenu = (contextItem) => {
  if (contextItem.id === 'ADD') {
    if (isSpliceMode.value) {
      lines.value = [lines.value[0], lines.value[2]]
    }
    isSpliceMode.value = true
  } else {
    ui.confirm(t('경고'), t('선택한 마커가 삭제됩니다. 삭제하시겠습니까?'), t('삭제'), t('취소')).onOk(() =>
      deleteMarker(),
    )
  }
}

const deleteMarker = () => {
  const modifyMarkers = markers.value.filter(
    (marker) => !_.isEqual(marker.latLng, Object.values(markerContextMenu.value.latLng)),
  )
  const modifyLines = lines.value[0].latLngs.filter((line) => !_.isEqual(line, markerContextMenu.value.latLng))

  markers.value = [...modifyMarkers]
  lines.value[0].latLngs = [...modifyLines]
  if (isSpliceMode.value) {
    lines.value = [lines.value[0]]
    isSpliceMode.value = false
    markerContextMenu.value.isShow = false
  }
}

const loadLocations = async () => {
  ui.loading.show()
  locations.value = await LocationService.getLocationsTree()
  ui.loading.hide()

  const filterTreeItemByLine = filterTreeItem(locations.value, 'LINE', { id: 'type' })
  if (filterTreeItemByLine.length === 1) {
    selectedLocation.value = filterTreeItemByLine[0].id
    await onChangeLocation(filterTreeItemByLine[0].id)
  }
}

const addMarker = (e) => {
  if (!isEdit.value) return
  if (!isSpliceMode.value && markerContextMenu.value.isShow) return
  if (markerContextMenu.value.isShow) {
    const index =
      markers.value.findIndex((marker) => _.isEqual(marker.latLng, Object.values(markerContextMenu.value.latLng))) + 1
    if (index !== -1) {
      const newItem = {
        latLng: [e.latlng.lat, e.latlng.lng],
        icon: createColoredIcon(markers.value[0].icon.options.color),
      }
      markers.value.splice(index, 0, newItem)
      lines.value[0].latLngs.splice(index, 0, { lat: e.latlng.lat, lng: e.latlng.lng })
      markers.value = [...markers.value]
      lines.value[0].latLngs = [...lines.value[0].latLngs]
    }
    markerContextMenu.value.isShow = false
    isSpliceMode.value = false

    lines.value = [lines.value[0]]
  } else {
    const color = reservoirLayout.value.length !== 0 ? reservoirLayout.value[0].color : getColor(selectedTools.value[0])
    const latLng = {
      latLng: [e.latlng.lat, e.latlng.lng],
      icon: createColoredIcon(color),
    }
    markers.value = [...markers.value, latLng]
    if (lines.value.length !== 0) {
      lines.value[0].latLngs = [...lines.value[0].latLngs, { lat: e.latlng.lat, lng: e.latlng.lng }]
    } else {
      lines.value.push({ color: color, latLngs: [{ lat: e.latlng.lat, lng: e.latlng.lng }] })
    }
  }
}

const onDragEnd = (index, e) => {
  markers.value[index].latLng = [e.target._latlng.lat, e.target._latlng.lng]
  markers.value = [...markers.value]
  lines.value[0].latLngs[index] = { lat: e.target._latlng.lat, lng: e.target._latlng.lng }
  lines.value[0].latLngs = [...lines.value[0].latLngs]
}

// Edit 모드 토글
const toggleEditMode = () => {
  if (selectedTools.value.length !== 1) {
    return false
  }
  if (isEdit.value === false && !_.isEqual(lastMarkersValue.value, markers.value)) {
    ui.confirm(t('경고'), t('수정한 내용이 저장되지 않았습니다. 조건을 변경하시겠습니까?'), t('예'), t('취소'))
      .onOk(() => {
        onChangeTool()
      })
      .onCancel(() => {
        isEdit.value = true
      })
  }
  if (isSpliceMode.value && _.isEqual(lastMarkersValue.value, markers.value)) {
    isSpliceMode.value = false
    lines.value = [lines.value[0]]
    markerContextMenu.value.isShow = false
  }
}

// 마지막 지점 삭제
const removeLastPoint = () => {
  if (isEdit.value && markers.value.length > 0) {
    markers.value.pop()
    lines.value[0].latLngs.pop()
    markers.value = [...markers.value]
    lines.value[0].latLngs = [...lines.value[0].latLngs]
  }
  if (isSpliceMode.value) {
    markerContextMenu.value.isShow = false
    isSpliceMode.value = false
    lines.value = [lines.value[0]]
  }
}

const onSave = async () => {
  if (!isEdit.value) return
  if (reservoirLayout.value.length === 0) {
    const { lat, lng } = getCenterGPS(markers.value)
    const data = { lat, lng, markers: [...markers.value.map((r) => r.latLng)] }
    const strData = JSON.stringify(data)
    await ReservoirLayoutService.createReservoirLayout({
      tool: selectedTools.value[0],
      data: strData,
    })
    ui.notify.success(t('저장되었습니다.'))
    isEdit.value = false
  } else {
    if (markers.value.length === 0) {
      await ReservoirLayoutService.deleteReservoirLayout(reservoirLayout.value[0].id)
      ui.notify.success(t('저장되었습니다.'))
      return
    }
    const { lat, lng } = getCenterGPS(markers.value)
    const data = { lat, lng, markers: [...markers.value.map((r) => r.latLng)] }
    const strData = JSON.stringify(data)
    await ReservoirLayoutService.modifyReservoirLayout(reservoirLayout.value[0].id, {
      tool: selectedTools.value[0],
      data: strData,
    })
    ui.notify.success(t('저장되었습니다.'))
    isEdit.value = false
  }
  lastMarkersValue.value = [...markers.value]
  onChangeTool()
}

const getCenterGPS = (latLngValue) => {
  const minLat = Math.min(...latLngValue.map((values) => values.latLng[0]))
  const maxLat = Math.max(...latLngValue.map((values) => values.latLng[0]))
  const minLng = Math.min(...latLngValue.map((values) => values.latLng[1]))
  const maxLng = Math.max(...latLngValue.map((values) => values.latLng[1]))

  return {
    lat: (maxLat - minLat) / 2 + minLat,
    lng: (maxLng - minLng) / 2 + minLng,
  }
}

const onChangeLocation = async (locationId) => {
  const onConfirm = async () => {
    clearMap()
    toolList.value = []

    const findTreeItemByLine = findTreeItem(locations.value, locationId)
    if (findTreeItemByLine.type === 'LINE') {
      changeMapCam(findTreeItemByLine.object.latitude, findTreeItemByLine.object.longitude)
      const tools = await ToolService.getToolsByLocationId(locationId)
      toolList.value = tools.map((item) => ({ ...item }))
      lastLocationValue = selectedLocation.value
      if (toolList.value.length === 1) {
        selectedTools.value = toolList.value
        onChangeTool()
      }
    }
  }
  if (!_.isEqual(lastMarkersValue.value, markers.value) && selectedTools.value.length === 1) {
    ui.confirm(t('경고'), t('수정한 내용이 저장되지 않았습니다. 조건을 변경하시겠습니까?'), t('예'), t('취소'))
      .onOk(() => {
        lastMarkersValue.value = []
        onConfirm()
      })
      .onCancel(() => {
        selectedLocation.value = lastLocationValue
      })
  } else {
    lastMarkersValue.value = []
    await onConfirm()
  }
}

const changeMapCam = (lat, lon) => {
  latitude.value = lat
  longitude.value = lon
}

const clearMap = () => {
  markers.value = []
  lines.value = []
  selectedTools.value = []
  lastMarkersValue.value = []
  reservoirLayout.value = []
  legendItems.value = []
  isEdit.value = false
  markerContextMenu.value.isShow = false
  isSpliceMode.value = false
}

const getColor = (toolId) => {
  const predefinedColors = [
    '#5470c6',
    '#ea7ccc',
    '#91cc75',
    '#fac858',
    '#ee6666',
    '#73c0de',
    '#3ba272',
    '#fc8452',
    '#9a60b4',
  ]
  const availableColors = predefinedColors.filter(
    (color) => !reservoirLayout.value.map((reservoirLayout) => reservoirLayout.color).includes(color),
  )

  return (
    reservoirLayout.value.find((reservoirLayout) => reservoirLayout.toolId === toolId)?.color ||
    availableColors[0] ||
    getRandomColor()
  )
}

const loadReservoirLayout = async () => {
  const layouts = await ReservoirLayoutService.getReservoirLayout({
    params: { toolIds: selectedTools.value.map((tool) => tool.id) },
  })
  if (layouts.length === 0) {
    return
  }

  reservoirLayout.value = layouts.map((reservoirLayout) => ({
    ...reservoirLayout,
    data: JSON.parse(reservoirLayout.data),
    color: getColor(reservoirLayout.toolId),
  }))

  legendItems.value = reservoirLayout.value.map(({ color, toolId }) => ({
    color,
    label: toolList.value.find((tool) => tool.id === toolId).name,
  }))

  let newMarkers = []
  let newLines = []
  reservoirLayout.value.forEach((reservoirLayout) => {
    let line = { color: reservoirLayout.color, latLngs: [] }
    reservoirLayout.data.markers.forEach((marker) => {
      newMarkers.push({
        latLng: marker,
        icon: createColoredIcon(reservoirLayout.color),
      })
      line.latLngs.push({ lat: marker[0], lng: marker[1] })
    })
    newLines.push(line)
  })

  markers.value = [...newMarkers]
  lines.value = [...newLines]
  changeMapCam(
    reservoirLayout.value[reservoirLayout.value.length - 1].data.lat,
    reservoirLayout.value[reservoirLayout.value.length - 1].data.lng,
  )
  if (layouts.length === 1) {
    lastMarkersValue.value = [...newMarkers]
  } else {
    lastMarkersValue.value = []
  }
  lastToolValue = selectedTools.value
  isSpliceMode.value = false
}

const onChangeTool = () => {
  const onConfirm = () => {
    if (selectedTools.value.length === 0) {
      clearMap()
      return false
    } else if (selectedTools.value.length > 1) {
      isEdit.value = false
      markerContextMenu.value.isShow = false
      isSpliceMode.value = false
    }
    loadReservoirLayout()
  }
  const isMarkersChanged =
    !_.isEqual(lastMarkersValue.value, markers.value) && selectedTools.value.length >= 0 && isEdit.value === true
  if (isMarkersChanged) {
    ui.confirm(t('경고'), t('수정한 내용이 저장되지 않았습니다. 조건을 변경하시겠습니까?'), t('예'), t('취소'))
      .onOk(() => {
        isEdit.value = false
        onConfirm()
      })
      .onCancel(() => {
        selectedTools.value = lastToolValue
      })
  } else {
    onConfirm()
  }
}

const getRandomColor = () => {
  const randomChannel = () => {
    const channel = Math.floor(Math.random() * 256).toString(16)
    return channel.length === 1 ? '0' + channel : channel
  }
  const red = randomChannel()
  const green = randomChannel()
  const blue = randomChannel()

  return `#${red}${green}${blue}`
}

const createColoredIcon = (color) => {
  return L.icon({
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    shadowSize: [41, 41],
    className: 'marker-icon',
    color: color,
    // 추가: 마커 색상 설정
    iconUrl: `data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 48 48"><path fill="${encodeURIComponent(color)}"  d="M24 4C15.18 4 8 11.18 8 20c0 12 16 24 16 24s16-12 16-24c0-8.82-7.18-16-16-16zm0 22c-2.21 0-4-1.79-4-4s1.79-4 4-4 4 1.79 4 4-1.79 4-4 4z"/></svg>`,
  })
}

loadLocations()
</script>

<style lang="scss" scoped>
.command_layout {
  width: 100%;
  height: 50px;
}

.command_container {
  text-align: end;
  padding-top: 10px;

  button {
    margin-right: 5px;
  }
}

.leaflet-control-custom {
  background-color: white;
  padding: 10px;
  border-radius: 5px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.legend-item {
  display: inline-block;
  width: 20px;
  height: 20px;
  margin-right: 5px;
}

.items-center {
  padding-bottom: 3px;
}

.context-list {
  width: 160px;
}
</style>
