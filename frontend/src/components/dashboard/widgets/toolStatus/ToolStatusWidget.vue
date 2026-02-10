<template>
  <div
    :class="`full-width full-height chart_container no-padding row flex tool_status_wrap ${isFlexible ? 'flexible_wrap' : ''}`"
  >
    <div class="col chartBorder rounded-borders">
      <LeafletMap ref="map" :zoom="13" :center="centerPosition" @ready="onMapReady">
        <l-polygon :lat-lngs="lines" color="lightcoral" weight="3" opacity="0.7" stroke="true" />
        <l-marker
          v-if="currentLatLng !== undefined"
          :lat-lng="[currentLatLng.lat, currentLatLng.lng]"
          :icon="pulseIcon"
        />
      </LeafletMap>
    </div>
    <div class="col q-pl-sm">
      <q-list separator>
        <q-item :class="getBetteryClass(robotData.batteryRemaining)" v-ripple>
          <q-item-section class="text-subtitle2 text-bold q-pr-md titleWidth section_left">
            <h3 class="tit">{{ $t('배터리') }}</h3>
            <span class="flex items-center no-wrap">{{ robotData.batteryRemaining }} <i>%</i></span>
          </q-item-section>
          <q-item-section
            v-if="robotData.batteryRemaining !== null && robotData.batteryRemaining !== ''"
            class="value_container section_right"
          >
            <ul class="battery_wrap">
              <li class="gauge"></li>
              <li class="gauge"></li>
              <li class="gauge"></li>
              <li class="gauge"></li>
            </ul>
            <!-- <div class="col flex">
              <img class="q-mr-sm" :src="batteryImg" width="50px" height="20px" />
              <span class="flex items-center">{{ robotData.batteryRemaining + ' %' }}</span>
            </div> -->
          </q-item-section>
          <div v-else class="value_container col">{{ $t('데이터 없음') }}</div>
        </q-item>

        <q-item v-ripple class="weather_wrap">
          <q-item-section class="text-subtitle2 text-bold titleWidth section_left">
            <h3 class="tit">{{ $t('기상') }}</h3>
            <div class="flex">
              <i class="skycondi">{{ robotData.skyCondition }}</i>
              <span class="flex">{{ robotData.temperature }} <i>℃</i></span>
            </div>
          </q-item-section>
          <q-item-section
            v-if="robotData.skyCondition !== null && robotData.skyCondition !== ''"
            class="value_container section_right"
          >
            <div class="col flex">
              <img class="" :src="weatherImg" width="50px" height="50px" />
              <!-- <div class="flex items-center">{{ robotData.temperature + '℃ ' + robotData.skyCondition }}</div> -->
            </div>
          </q-item-section>
          <div v-else class="value_container col">{{ $t('데이터 없음') }}</div>
        </q-item>

        <q-item v-ripple class="location_wrap">
          <q-item-section class="text-subtitle2 text-bold q-pr-md titleWidth section_left">
            <h3 class="tit">{{ $t('위치') }}</h3>
            <span class="location_text">{{ robotData.location.lat + ', ' + robotData.location.lng }}</span>
          </q-item-section>
          <q-item-section
            v-if="robotData.location.lat !== null && robotData.location.lat !== ''"
            class="value_container section_right"
          >
            <!-- {{ robotData.location.lat + ', ' + robotData.location.lng }} -->
          </q-item-section>
          <div v-else class="value_container col">{{ $t('데이터 없음') }}</div>
        </q-item>

        <q-item v-ripple>
          <q-item-section class="text-subtitle2 text-bold titleWidth section_left">
            <h3 class="tit">{{ $t('데이터 수집 상태') }}</h3>
          </q-item-section>
          <q-item-section
            v-if="robotData.goodCnt !== null && robotData.goodCnt !== ''"
            class="value_container section_right"
          >
            <div class="status-container">
              <!-- 양호 상태 -->
              <q-chip
                outline
                dense
                clickable
                color="blue"
                text-color="white"
                class="q-mr-sm"
                @click.stop="showMenuGood = true"
              >
                <span>{{ $t('양호') }}</span>
                <span>{{ robotData.goodCnt }}</span>
                <q-menu :model-value="showMenuGood" class="text-body2 custom-menu" style="background-color: #4d66d2">
                  <q-list>
                    <q-item v-for="(parameter, index) in robotData.goodParameter" :key="index">
                      <q-item-section class="text-white">{{ parameter }}</q-item-section>
                    </q-item>
                  </q-list>
                </q-menu>
              </q-chip>
              <!-- 문제 상태 -->
              <q-chip outline dense clickable :color="getBadCnt" text-color="white" @click.stop="showMenuBad = true">
                <span>{{ $t('문제') }}</span>
                <span>{{ robotData.badCnt }}</span>
                <q-menu :model-value="showMenuBad" class="text-body2 custom-menu" style="background-color: #ff5370">
                  <q-list>
                    <q-item v-for="(parameter, index) in robotData.badParameter" :key="index">
                      <q-item-section class="text-white">{{ parameter }}</q-item-section>
                    </q-item>
                  </q-list>
                </q-menu>
              </q-chip>
            </div>
          </q-item-section>
          <div v-else class="value_container col">{{ $t('데이터 없음') }}</div>
        </q-item>

        <q-item v-ripple class="velocity_wrap">
          <q-item-section class="text-subtitle2 q-pr-md titleWidth section_left">
            <h3 class="tit">{{ $t('속도 (1시간)') }}</h3>
          </q-item-section>
          <q-item-section
            v-if="robotData.velocity.avg !== null && robotData.velocity.avg !== ''"
            class="value_container section_right"
          >
            <div class="status-container">
              <q-chip color="primary" text-color="white" size="sm">
                <span>{{ $t('평균') }}</span>
                <span>{{ robotData.velocity.avg }} m/m</span>
              </q-chip>
              <q-chip color="secondary" text-color="white" size="sm">
                <span>{{ $t('최고') }}</span>
                <span>{{ robotData.velocity.max }} m/m</span>
              </q-chip>
              <q-chip color="info" text-color="white" size="sm">
                <span>{{ $t('최저') }}</span>
                <span>{{ robotData.velocity.min }} m/m</span>
              </q-chip>
            </div>
            <div></div>
          </q-item-section>
          <div v-else class="value_container col">{{ $t('데이터 없음') }}</div>
        </q-item>
        <q-item v-ripple>
          <q-item-section class="text-subtitle2 text-bold titleWidth section_left">
            <h3 class="tit">{{ $t('이동거리 (1시간)') }}</h3>
          </q-item-section>
          <q-item-section
            v-if="robotData.travelDistance !== null && robotData.travelDistance !== ''"
            class="value_container section_right"
          >
            <q-chip outline dense color="grey" text-color="white">
              {{ robotData.travelDistance + 'm' }}
            </q-chip>
          </q-item-section>
          <div v-else class="value_container col">{{ $t('데이터 없음') }}</div>
        </q-item>
      </q-list>
    </div>
    <q-resize-observer @resize="onResize" />
  </div>
</template>

<script setup>
import L, { latLng } from 'leaflet'
import { LMarker, LPolygon } from '@vue-leaflet/vue-leaflet'
import '@ansur/leaflet-pulse-icon/dist/L.Icon.Pulse'
import '@ansur/leaflet-pulse-icon/dist/L.Icon.Pulse.css'
import { computed, nextTick, ref, shallowRef } from 'vue'
import { useWidgetRefresh, widgetProps } from '/src/common/module/widgetCommon'
import { useI18n } from 'vue-i18n'
import WidgetService from 'src/services/widget/WidgetService.js'
import LocationService from 'src/services/modeling/LocationService'
import LeafletMap from 'components/map/LeafletMap.vue'

defineProps(widgetProps)
const { t } = useI18n()
const isFlexible = ref(false)
const map = shallowRef(null)
let isMapReady = false

const onMapReady = () => {
  isMapReady = true
  refreshAction()
}

const onResize = ({ width }) => {
  nextTick(() => {
    if (width <= 460) {
      //너비가 460px 이하일 때 flexible 적용`
      isFlexible.value = true
    } else {
      isFlexible.value = false
      map.value.leafletMap?.invalidateSize()
    }
  })
}

const currentLatLng = ref()
const pulseIcon = L.icon.pulse({ iconSize: [8, 8], color: 'red' })

const lines = ref([])
// const lineColor = ref()
const centerPosition = ref([35.849021, 128.41232])
const markerLatLng = ref([0, 0])
const robotData = ref({
  batteryRemaining: '',
  goodCnt: '',
  badCnt: '',
  goodParameter: [],
  badParameter: [],
  location: { lat: '', lng: '' },
  skyCondition: '',
  temperature: '',
  precipitation: '',
  velocity: {
    avg: '',
    max: '',
    min: '',
  },
  travelDistance: '',
})
const showMenuGood = ref(false)
const showMenuBad = ref(false)
const batteryImg = ref(null)
const weatherImg = ref(null)

const getBadCnt = computed(() => {
  if (robotData.value.badCnt === 0) {
    return 'grey'
  } else {
    return 'red'
  }
})

const refresh = async (config) => {
  if (isMapReady === false) return
  const { toolId } = config
  try {
    const widget = await WidgetService.getToolStatusWidgetData(toolId)
    lines.value = JSON.parse(widget.markers).markers
    if (widget.latitude !== null && widget.longitude !== null) {
      currentLatLng.value = { lat: widget.latitude, lng: widget.longitude }
      centerPosition.value = latLng(widget.latitude, widget.longitude)
      markerLatLng.value = [widget.latitude, widget.longitude]
    } else {
      const line = await LocationService.getLineTypeLocationByToolId(toolId)
      currentLatLng.value = undefined
      centerPosition.value = latLng(line.latitude, line.longitude)
      markerLatLng.value = null
    }
    robotData.value.batteryRemaining = widget.batteryRemaining
    batteryImg.value = getBatteryuImageUrl()
    robotData.value.goodCnt = widget.goodCnt
    robotData.value.badCnt = widget.badCnt
    robotData.value.goodParameter = widget.goodParameter
    robotData.value.badParameter = widget.badParameter
    robotData.value.location.lat = Math.trunc(widget.latitude * 100000) / 100000
    robotData.value.location.lng = Math.trunc(widget.longitude * 100000) / 100000
    robotData.value.temperature = widget.temperature
    robotData.value.precipitation = widget.precipitationFoam
    getWeatherImageUrl(widget.skyCondition)
    robotData.value.velocity.avg = widget.avgVelocity
    robotData.value.velocity.max = widget.maxVelocity
    robotData.value.velocity.min = widget.minVelocity
    robotData.value.travelDistance = widget.distance !== null ? Math.floor(widget.distance) : null
  } catch (error) {
    robotData.value.batteryRemaining = ''
    robotData.value.goodCnt = ''
    robotData.value.location.lat = ''
    robotData.value.skyCondition = ''
    robotData.value.velocity.avg = ''
    robotData.value.travelDistance = ''
  }
}

const getWeatherImageUrl = (skyCondition) => {
  if (skyCondition === '') return ''
  const precipitation = robotData.value.precipitation
  if (precipitation !== 'NONE') {
    if (precipitation === 'RAINY') {
      robotData.value.skyCondition = t('비')
      weatherImg.value = '/img/widgets/weather/rain.svg'
    } else if (precipitation === 'RAINYSNOW') {
      robotData.value.skyCondition = t('비/눈')
      weatherImg.value = '/img/widgets/weather/sleet.svg'
    } else if (precipitation === 'SNOW') {
      robotData.value.skyCondition = t('눈')
      weatherImg.value = '/img/widgets/weather/snow.svg'
    } else if (precipitation === 'RAINDROP') {
      robotData.value.skyCondition = t('빗방울')
      weatherImg.value = '/img/widgets/weather/drizzle.svg'
    } else if (precipitation === 'RAINDROPSNOWFLAKE') {
      robotData.value.skyCondition = t('빗방울/눈날림')
      weatherImg.value = '/img/widgets/weather/sleet.svg'
    } else if (precipitation === 'SNOWFLAKE') {
      robotData.value.skyCondition = t('눈날림')
      weatherImg.value = '/img/widgets/weather/snow.svg'
    }
  } else {
    if (skyCondition === 'SUNNY') {
      robotData.value.skyCondition = t('맑음')
      weatherImg.value = '/img/widgets/weather/clear-day.svg'
    } else if (skyCondition === 'CLOUDY') {
      robotData.value.skyCondition = t('구름많음')
      weatherImg.value = '/img/widgets/weather/cloudy.svg'
    } else if (skyCondition === 'OVERCLOUD') {
      robotData.value.skyCondition = t('흐림')
      weatherImg.value = '/img/widgets/weather/rain.svg'
    }
  }
}

const getBatteryuImageUrl = () => {
  const battery = robotData.value.batteryRemaining
  if (battery < 26) {
    return '/img/widgets/battery/25PerBattery.png'
  } else if (25 < battery && battery < 51) {
    return '/img/widgets/battery/50PerBattery.png'
  } else if (50 < battery && battery < 76) {
    return '/img/widgets/battery/75PerBattery.png'
  } else {
    return '/img/widgets/battery/fullBattery.png'
  }
}

const getBetteryClass = (batteryRemaining) => {
  return batteryRemaining > 75
    ? 'bfull'
    : batteryRemaining > 50
      ? 'b75per'
      : batteryRemaining > 25
        ? 'b50per'
        : 'b25per'
}

const { refreshAction } = useWidgetRefresh(refresh)
</script>
<style lang="scss" scoped>
.chart_container {
  width: calc(100% - 10px);
  height: calc(100% - 20px);
  overflow-y: hidden;
  overflow-x: hidden;
}

.label {
  font-weight: bold;
  background: #f5f5f5;
  padding: 8px;
  border-right: 1px solid #000;
}

.status-container {
  display: flex;
  background: white;
  border-left: none;
}

.q-chip {
  font-weight: bold;
  font-size: 0.875em;
  padding: 6px 12px;
}

.titleWidth {
  max-width: 140px;
}

.value_container {
  display: flow;
  align-content: center;
}

.chartBorder {
  border: 1px solid black;
}

.custom-menu {
  width: 100%;
  border-radius: 16px; /* 메뉴 모서리를 둥글게 */
  box-shadow: 0px 4px 8px rgba(0, 0, 0, 0.1);
  padding: 8px;
}
</style>
