<template>
  <q-page class="q-pa-sm">
    <q-card class="cust_subPage">
      <q-card-section class="row search_section_wrap">
        <q-item class="col-11 search_left grid-0404-full">
          <q-item-section>
            <q-input filled v-model="fromDate" stack-label :label="$t('조회 시작일')">
              <template v-slot:prepend>
                <q-icon name="event" class="cursor-pointer">
                  <q-popup-proxy cover transition-show="scale" transition-hide="scale">
                    <q-date
                      v-model="fromDate"
                      mask="YYYY-MM"
                      default-view="Years"
                      emit-immediately
                      minimal
                      ref="yearMonth"
                      @update:model-value="onUpdateCalendar"
                    >
                      <div class="row items-center justify-end">
                        <q-btn v-close-popup :label="$t('Close')" color="primary" flat />
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
                    <q-date
                      v-model="toDate"
                      mask="YYYY-MM"
                      default-view="Years"
                      emit-immediately
                      minimal
                      ref="yearMonth"
                      @update:model-value="onUpdateCalendar"
                    >
                      <div class="row items-center justify-end">
                        <q-btn v-close-popup :label="$t('Close')" color="primary" flat />
                      </div>
                    </q-date>
                  </q-popup-proxy>
                </q-icon>
              </template>
            </q-input>
          </q-item-section>
          <q-item-section>
            <ToolSelectBox v-model="selectedTool" />
          </q-item-section>
          <q-item-section>
            <ParameterSelectBox
              :tool-id="selectedToolId"
              v-model="selectParameters"
              :type="[PARAMETER_TYPE.HEALTH]"
              :dataTypes="[PARAMETER_DATA_TYPE.INTEGER]"
              multiple
            />
          </q-item-section>
        </q-item>
        <q-item class="col-1 search_right">
          <q-item-section>
            <q-btn @click="onSearch" class="search_btn with_icon_btn sBtn">{{ $t('조회') }}</q-btn>
          </q-item-section>
        </q-item>
      </q-card-section>

      <q-card-section class="column col">
        <div class="full-width full-height q-py-md">
          <LeafletMap ref="map" :zoom="zoom" :center="center">
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

            <LLayerGroup v-if="keyframes.length > 0">
              <transition-group name="fade" tag="div">
                <LCircleMarker
                  v-for="(point, index) in keyframes[selectedFrameIndex].points"
                  :key="point.yearMonth + '_' + index"
                  :lat-lng="[point.coordinates[1], point.coordinates[0]]"
                  :fill-color="getColor(point.value)"
                  :radius="5"
                  :fill-opacity="1"
                  :opacity="0"
                  class-name="circle-marker test"
                >
                  <LPopup :options="{ autoPan: false }">
                    <div>
                      <b>{{ t('point') }}:</b>
                      <a @click="openRadarChart(point)" style="cursor: pointer">{{ point.value }}</a>
                      <br />
                      <b>{{ t('latitude') }}:</b> {{ point.coordinates[1] }}
                      <br />
                      <b>{{ t('longitude') }}:</b> {{ point.coordinates[0] }}
                    </div>
                  </LPopup>
                </LCircleMarker>
              </transition-group>
            </LLayerGroup>
          </LeafletMap>
        </div>
        <div class="q-mt-sm">
          <WaterQualityHeatMapWidgetChart
            v-model="isShowRadarChart"
            :health-data="healthData"
            :latitude="selectedLat"
            :longitude="selectedLng"
            @close="onClose"
          />
        </div>
      </q-card-section>

      <q-card-actions v-if="keyframes.length > 0" class="q-pa-md" style="height: 80px">
        <div class="col-12">
          <q-item class="q-py-none">
            <q-item-section avatar class="q-mr-md">
              <q-btn
                v-if="keyframes.length > 0"
                @click="togglePlayback"
                class="sBtn secondary"
                :icon="playingInterval ? 'stop' : 'play_arrow'"
                :label="playingInterval ? $t('정지') : $t('재생')"
              />
            </q-item-section>
            <q-item-section class="col q-mx-xl">
              <q-slider
                color="secondary"
                markers
                :marker-labels="markerLabel"
                :min="0"
                :max="markerLabel.length - 1"
                v-model="selectedFrameIndex"
                @update:model-value="updateMapToFrame"
              />
            </q-item-section>
          </q-item>
        </div>
      </q-card-actions>
    </q-card>
  </q-page>
</template>

<script setup>
import { computed, ref, shallowRef } from 'vue'
import { LCircleMarker, LControl, LLayerGroup, LPopup } from '@vue-leaflet/vue-leaflet'
import { useI18n } from 'vue-i18n'
import HealthDataService from 'src/services/parameterData/HealthDataService'
import WaterQualityHeatMapWidgetChart from 'components/dashboard/widgets/waterQualityHeatMap/WaterQualityHeatMapWidgetChart.vue'
import useUI from 'src/common/module/ui'
import LeafletMap from 'components/map/LeafletMap.vue'
import ToolSelectBox from 'components/form/ToolSelectBox.vue'
import ParameterSelectBox from 'components/form/ParameterSelectBox.vue'
import { PARAMETER_DATA_TYPE, PARAMETER_TYPE } from 'src/common/constant/parameter'

const { t } = useI18n()
const ui = useUI()
const zoom = shallowRef(14)
const center = shallowRef([35.84644, 128.43996])
const selectParameters = ref([])
const selectedTool = ref()
const toDate = ref(new Date().toISOString().slice(0, 7))
const fromDate = ref(new Date(new Date().setFullYear(new Date().getFullYear() - 1)).toISOString().slice(0, 7))
const keyframes = ref([])
const selectedFrameIndex = ref(0)
const playingInterval = ref()
const selectedLat = ref()
const selectedLng = ref()
const healthData = ref()
const isShowRadarChart = ref(false)
const markerLabel = ref([])
const legendLabel = ['100', '80', '50']
const currentView = ref('Years')
const yearMonth = ref(null)

const onUpdateCalendar = () => {
  currentView.value = currentView.value === 'Years' ? 'Months' : 'Years'
  yearMonth.value.setView(currentView.value)
}

const selectedToolId = computed(() => {
  return selectedTool.value ? selectedTool.value.id : null
})

const onSearch = async () => {
  if (!selectParameters.value || selectParameters.value.length === 0) {
    ui.notify.warning(t('파라미터가 선택되지 않았습니다.'))
    return
  } else if (fromDate.value > toDate.value) {
    ui.notify.warning(t('조회기간 시작년월은 종료년월보다 작아야 합니다.'))
    return
  }
  const [year, month] = toDate.value.split('-')
  const lastDay = new Date(year, month, 0).setHours(23, 59, 59, 999)
  ui.loading.show()
  const monthlyHealthPoint = await HealthDataService.getMonthlyHeatMapHealthData(
    selectParameters.value.map((value) => value.id),
    new Date(fromDate.value),
    new Date(lastDay),
  )
  ui.loading.hide()

  const groupedByYearMonth = monthlyHealthPoint.reduce((acc, { yearMonth, longitude, latitude, score, healthData }) => {
    acc[yearMonth] = acc[yearMonth] || []
    acc[yearMonth].push({
      coordinates: [longitude, latitude],
      value: score,
      healthData: healthData,
    })
    return acc
  }, {})

  const keyframesValue = Object.entries(groupedByYearMonth)
    .map(([yearMonth, points]) => ({ yearMonth, points }))
    .sort((a, b) => a.yearMonth.localeCompare(b.yearMonth))

  keyframes.value = keyframesValue
  markerLabel.value = keyframesValue.map((value, index) => ({ value: index, label: value.yearMonth }))

  updateMapToFrame(0)
  playingInterval.value = null
}

const updateMapToFrame = (index) => {
  selectedFrameIndex.value = index
}

const openRadarChart = async (point) => {
  selectedLat.value = point.coordinates[1]
  selectedLng.value = point.coordinates[0]
  healthData.value = point.healthData
  isShowRadarChart.value = true
}

const onClose = () => {
  isShowRadarChart.value = false
}

const togglePlayback = () => {
  if (playingInterval.value) {
    clearInterval(playingInterval.value)
    playingInterval.value = null
  } else {
    playingInterval.value = setInterval(() => {
      const newIndex = (selectedFrameIndex.value + 1) % keyframes.value.length
      selectedFrameIndex.value = newIndex
      updateMapToFrame(newIndex)
    }, 2000) // 재생 간격 설정 (2초마다)
  }
}

const getColor = (value) => {
  value = value / 100
  if (value <= 0.5) return '#FF0000'
  if (value < 0.6) return '#FF8000'
  if (value < 0.7) return '#C8B12D'
  if (value < 0.8) return '#81C633'
  return '#3ad33a'
}
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

:deep(.circle-marker) {
  transition: all 0.5s ease-in;
}

.q-slider :deep(.q-slider__marker-labels--h-ltr) {
  width: 55px;
}
</style>
