<template>
  <q-dialog v-model="isShow" persistent>
    <q-layout container style="width: 1500px; max-width: 70vw; align-content: baseline">
      <q-card>
        <q-header bordered class="bg-grey">
          <q-card-section class="bg-primary">
            <div class="text-h6">{{ $t('지도 검색') }}</div>
          </q-card-section>
          <q-card-section style="display: flex" class="q-pa-sm bg-white">
            <q-input
              v-model="address"
              :label="$t('검색')"
              @keydown="(e) => e.key === 'Enter' && moveToAddress()"
              style="min-width: 300px"
            />
            <q-btn color="primary" @click="moveToAddress" class="q-ml-sm sBtn">{{ $t('검색') }}</q-btn>
          </q-card-section>
        </q-header>
        <q-page class="shadow-1 boarder">
          <q-drawer
            v-model="searchResultDrawer"
            :width="400"
            :breakpoint="500"
            bordered
            style="position: absolute; top: 0; right: 0"
          >
            <div style="display: flex">
              <div class="text-h6 q-pa-md text-primary">{{ $t('검색 결과') }}</div>

              <q-icon
                class="absolute text-grey"
                style="top: 15px; right: 15px"
                size="md"
                name="close"
                @click="closeSearchResultDrawer"
              />
            </div>
            <div v-for="(result, index) in searchResults" :key="index">
              <div
                @click="onClickAddressMarker(result)"
                class="clickable-text q-pa-sm"
                style="cursor: pointer; border-bottom: 1px solid grey"
              >
                {{ result.display_name + ' (' + $t('위도') + ' : ' }}
                <span class="text-primary">{{ result.lat }}</span>
                {{ ', ' + $t('경도') + ' : ' }}
                <span class="text-primary">{{ result.lon }}</span>
                {{ ')' }}
              </div>
            </div>
          </q-drawer>
          <q-page-container>
            <div style="height: calc(100vh - 250px); width: 100%; position: relative">
              <LeafletMap ref="map" :zoom="zoom" :center="centerPosition" @click="onClick">
                <l-marker :lat-lng="markerLatLng" />
              </LeafletMap>
            </div>
          </q-page-container>
        </q-page>
        <q-card-actions>
          <q-space />
          <q-btn flat color="negative" :label="$t('닫기')" @click="onCancel" />
          <q-btn flat color="primary" :label="$t('저장')" @click="onOk" />
        </q-card-actions>
      </q-card>
    </q-layout>
  </q-dialog>
</template>
<script setup>
import { LMarker } from '@vue-leaflet/vue-leaflet'
import { latLng } from 'leaflet'
import { nextTick, ref, shallowRef } from 'vue'
import useUI from 'src/common/module/ui'
import { useI18n } from 'vue-i18n'
import LeafletMap from 'components/map/LeafletMap.vue'

const { t } = useI18n()
const props = defineProps({
  zoom: Number,
  center: Array,
})
const ui = useUI()
const centerPosition = ref([])
const emit = defineEmits(['close'])
const isShow = ref(true)
const markerLatLng = ref([0, 0])
const address = ref('')
const searchResults = ref([])
const searchResultDrawer = ref(false)
const map = shallowRef(null)

const moveToAddress = async () => {
  try {
    ui.loading.show()
    markerLatLng.value = [0, 0]
    const response = await fetch(
      `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(address.value)}`,
    )
    searchResults.value = await response.json()
    const location = searchResults.value[0]
    if (location) {
      searchResultDrawer.value = true
      centerPosition.value = []
      centerPosition.value = latLng(location.lat, location.lon)
      setTimeout(function () {
        nextTick()
        map.value.leafletMap.invalidateSize()
      }, 100)
    } else {
      ui.notify.warning(t('검색 지역을 입력하세요.'))
    }
    ui.loading.hide()
  } catch (error) {
    console.error(t('Error fetching geocoding data:'), error)
  }
}

const onClickAddressMarker = (result) => {
  centerPosition.value = latLng(result.lat, result.lon)
  markerLatLng.value = [result.lat, result.lon]
}

const closeSearchResultDrawer = async () => {
  await (searchResultDrawer.value = false)
  setTimeout(function () {
    nextTick()
    map.value.leafletMap.invalidateSize()
  }, 100)
}

const onClick = (e) => {
  markerLatLng.value = [e.latlng.lat, e.latlng.lng]
}

const onOk = async () => {
  if (markerLatLng.value.length === 0 || (markerLatLng.value[0] === 0 && markerLatLng.value[1] === 0)) {
    ui.notify.warning(t('좌표가 선택되지 않았습니다. 지도에서 좌표를 선택하세요.'))
  } else {
    isShow.value = false
    emit('close', markerLatLng.value)
  }
}

const onCancel = () => {
  isShow.value = false
  emit('close')
}

const initCenterPosition = () => {
  centerPosition.value = latLng(props.center)
  markerLatLng.value = props.center
}

initCenterPosition()
</script>
<style scoped>
.clickable-text:hover {
  text-decoration: underline;
  color: blue;
}
</style>
