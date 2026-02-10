<template>
  <q-card class="cust_subPage">
    <q-card-section class="row search_section_wrap">
      <q-item class="col-6 search_left">
        <q-item-section>
          <q-input filled v-model="fromDate" stack-label :label="$t('조회 시작일')">
            <template v-slot:prepend>
              <q-icon name="event" class="cursor-pointer">
                <q-popup-proxy cover transition-show="scale" transition-hide="scale">
                  <q-date v-model="fromDate" :mask="DATE_FORMAT">
                    <div class="row items-center justify-end">
                      <q-btn v-close-popup :label="$t('닫기')" color="primary" flat />
                    </div>
                  </q-date>
                </q-popup-proxy>
              </q-icon>
            </template>
          </q-input>
        </q-item-section>
        <q-item-section>
          <q-input filled v-model="toDate" stack-label :label="$t('조회 종료일')">
            <template v-slot:prepend>
              <q-icon name="event" class="cursor-pointer">
                <q-popup-proxy cover transition-show="scale" transition-hide="scale">
                  <q-date v-model="toDate" :mask="DATE_FORMAT">
                    <div class="row items-center justify-end">
                      <q-btn v-close-popup :label="$t('닫기')" color="primary" flat />
                    </div>
                  </q-date>
                </q-popup-proxy>
              </q-icon>
            </template>
          </q-input>
        </q-item-section>
        <q-item-section>
          <filterable-select
            v-model="selectedTool"
            :options="toolOptions"
            option-value="id"
            option-label="name"
            :label="$t('설비')"
          >
            <template v-slot:prepend>
              <q-icon name="construction" />
            </template>
          </filterable-select>
        </q-item-section>
      </q-item>
      <q-item class="col-2 search_right">
        <q-item-section>
          <q-btn class="search_btn with_icon_btn sBtn" @click="onSearch">{{ $t('조회') }}</q-btn>
        </q-item-section>
      </q-item>
    </q-card-section>
    <q-card-section class="map full-height">
      <LeafletMap ref="map" :center="latLng" :zoom="zoom" :max-zoom="18" :min-zoom="14">
        <l-control position="topright">
          <div ref="legend" class="leaflet-control-custom">
            <div class="items-center">
              <span>{{ $t('현재 설비') }} : {{ legendToolName || $t('없음') }}</span>
            </div>
          </div>
        </l-control>
      </LeafletMap>
    </q-card-section>
  </q-card>
</template>

<script setup>
import { onMounted, ref, shallowRef } from 'vue'
import L from 'leaflet'
import { useDateTimeRange } from 'src/common/module/validation'
import 'leaflet'
import 'leaflet.markercluster'
import '@lychee-org/leaflet.photo'
import '@lychee-org/leaflet.photo/Leaflet.Photo.css'
import { formatDate, formatDateTime } from 'src/common/utils'
import { date } from 'quasar'
import { DATE_FORMAT } from 'src/common/constant/format'
import ToolService from 'src/services/modeling/ToolService'
import useUI from 'src/common/module/ui'
import { t } from '/src/plugins/i18n'
import PhotoDataService from 'src/services/parameterData/PhotoDataService'
import { LControl } from '@vue-leaflet/vue-leaflet'
import LeafletMap from 'components/map/LeafletMap.vue'

const toDay = formatDate()
const fromDay = formatDate(date.subtractFromDate(Date.now(), { days: 3 }))
const fromDate = ref(fromDay)
const toDate = ref(toDay)
const zoom = 14
const latLng = ref([35.8503, 128.4222])

const toolOptions = ref([])
const selectedTool = ref(null)
const legendToolName = shallowRef()

const map = shallowRef(null)
const photoCluster = shallowRef(null)
const ui = useUI()

onMounted(() => {
  loadTools()
})

const loadTools = async () => {
  toolOptions.value = await ToolService.getTools()
  if (toolOptions.value.length > 0) {
    selectedTool.value = toolOptions.value[0]
  }
}

const clearLayer = () => {
  if (photoCluster.value !== null) {
    map.value.leafletMap.removeLayer(photoCluster.value)
  }
}
const onSearch = async () => {
  latLng.value = [selectedTool.value.location.latitude, selectedTool.value.location.longitude]
  ui.loading.show()
  const photoDatas = await loadParameterPhotoData()
  clearLayer()
  legendToolName.value = selectedTool.value.name
  if (photoDatas.length === 0) {
    ui.notify.warning(t('조회된 사진이 없습니다.'))
    ui.loading.hide()
    return
  }

  const photoClusterDatas = toPhotoClusterData(photoDatas)

  updatePhotoClusterLayer(photoClusterDatas)
  ui.loading.hide()
}

const loadParameterPhotoData = () => {
  return PhotoDataService.getRobotPhotoData(selectedTool.value.id, new Date(fromDate.value), new Date(toDate.value))
}

const toPhotoClusterData = (photoDatas) => {
  return photoDatas.map((data) => {
    return {
      lat: data.coordinates.latitude,
      lng: data.coordinates.longitude,
      url: data.imageUrl,
      thumbnail: data.imageUrl,
      caption: t('일시') + ' : ' + formatDateTime(data.caption),
    }
  })
}

const updatePhotoClusterLayer = (photos) => {
  photoCluster.value = L.photo
    .cluster({
      maxClusterRadius: 40,
      showCoverageOnHover: true,
      icon: {
        iconSize: [50, 50],
      },
    })
    .add(photos)
    .on('click', (evt) => {
      const photo = evt.layer.photo,
        template = `<a href="${photo.url}" target=newwin><img src="${photo.url}" /></a><p>${photo.caption}</p>`
      L.popup({ className: 'leaflet-photo-wrapper' })
        .setContent(template)
        .setLatLng(evt.latlng)
        .openOn(map.value.leafletMap)
    })
  photoCluster.value.addTo(map.value.leafletMap)
}

useDateTimeRange(fromDate, toDate).onInvalid(() => {
  ui.notify.warning(t('조회기간 시작일시는 종료일시보다 작아야 합니다.'))
})
</script>

<style scoped>
.map {
  width: 100%;
  /* height: calc(100vh - 204px); */
}

.leaflet-control-custom {
  background-color: white;
  padding: 5px;
  border-radius: 5px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  opacity: 0.8;
  color: red;
}
</style>
