<template>
  <q-page class="q-pa-none" style="height: 100%">
    <q-carousel
      v-model:fullscreen="fullscreen"
      infinite
      navigation
      navigation-icon="radio_button_unchecked"
      transition-prev="slide-right"
      transition-next="slide-left"
      animated
      swipeable
      control-color="primary"
      :autoplay="interval * 1000"
      v-model="slideIndex"
      :padding="false"
      class="dashboard-carousel"
    >
      <q-carousel-slide class="carousel-slide" v-for="(item, index) in slideDashboards" :key="item.id" :name="index">
        <dashboard :dashboard-id="item" :is-editable-slide="true" />
      </q-carousel-slide>
      <template v-slot:control>
        <q-carousel-control position="top-right">
          <q-btn
            round
            dense
            color="white"
            text-color="primary"
            :icon="fullscreen ? 'fullscreen_exit' : 'fullscreen'"
            @click="fullscreen = !fullscreen"
          />
        </q-carousel-control>
      </template>
    </q-carousel>

    <q-btn fab icon="settings" color="pink" class="absolute-bottom-left q-mb-xs" @click="settingsPopup.isShow = true" />

    <q-dialog v-model="settingsPopup.isShow" persistent>
      <q-card class="pop-widget-setting">
        <q-card-section class="row bg-secondary text-white">
          <div class="text-h5">{{ $t('슬라이드쇼 설정') }}</div>
        </q-card-section>
        <q-card-section class="setting-list overflow-auto">
          <q-card flat bordered>
            <q-card-section class="bg-primary text-white">
              <div class="text-subtitle2 field-required">
                <q-icon name="mdi-clock-outline" color="white" class="q-mr-xs" size="20px" />
                {{ $t('슬라이드 주기') }}
              </div>
              <div class="text-caption">{{ $t('설정한 주기마다 슬라이드가 오른쪽으로 이동합니다.') }}</div>
            </q-card-section>
            <q-card-section class="row items-center q-my-sm">
              <q-radio v-model="settingsPopup.interval" :val="1800" :label="$t('30분')" />
              <q-radio v-model="settingsPopup.interval" :val="600" :label="$t('10분')" />
              <q-radio v-model="settingsPopup.interval" :val="300" :label="$t('5분')" />
              <q-radio v-model="settingsPopup.interval" :val="180" :label="$t('3분')" />
              <q-radio v-model="settingsPopup.interval" :val="60" :label="$t('1분')" />
              <q-radio v-model="settingsPopup.interval" :val="'SECS'" />
              <filterable-select
                v-if="settingsPopup.interval === 'SECS'"
                filled
                v-model="settingsPopup.intervalSecs"
                :options="Array.from({ length: 59 }, (_, index) => index + 1)"
                :placeholder="$t('초')"
                dense
                class="widthUnset"
              />
              <div class="column justify-center q-ml-xs">{{ $t('초') }}</div>
            </q-card-section>
          </q-card>
        </q-card-section>
        <q-card-section class="setting-list overflow-auto q-pt-none">
          <q-card flat bordered>
            <q-card-section class="bg-primary text-white">
              <div class="text-subtitle2 field-required">
                <q-icon name="mdi-monitor-dashboard" color="white" class="q-mr-xs" size="20px" />
                {{ $t('대시보드 목록') }}
              </div>
              <div class="text-caption">{{ localSubTitle }}</div>
            </q-card-section>
            <q-card-section class="q-ma-sm">
              <q-list bordered class="dashboard-list">
                <q-item v-for="dashboard in dashboards" :key="dashboard.id" clickable>
                  <q-item-section avatar>
                    <q-checkbox v-model="settingsPopup.selectedDashboards" :val="dashboard.id" />
                  </q-item-section>
                  <q-item-section>
                    <q-item-label>{{ dashboard.name }}</q-item-label>
                  </q-item-section>
                </q-item>
              </q-list>
            </q-card-section>
          </q-card>
        </q-card-section>

        <q-separator />

        <q-card-actions align="right">
          <q-btn flat :label="$t('닫기')" color="negative" @click="closeSlides()" />
          <q-btn flat :label="$t('확인')" color="primary" @click="applySettings" />
        </q-card-actions>
      </q-card>
    </q-dialog>
  </q-page>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import Dashboard from './DashboardPage.vue'
import DashboardService from 'src/services/dashboard/DashboardService'
import useUI from 'src/common/module/ui'
import { t } from 'src/plugins/i18n'
import LocalStorageService from 'src/services/LocalStorageService'

const router = useRouter()
const fullscreen = ref(false)
const ui = useUI()
const dashboards = ref([])
const slideDashboards = ref([])
const slideIndex = ref(0)
const interval = ref(60)
const isShowSlides = ref(false)
const settingsPopup = ref({
  isShow: true,
  interval: 60,
  intervalSecs: null,
  selectedDashboards: [],
})

const loadDashboards = async () => {
  dashboards.value = await DashboardService.getDashboards()
}

const initSettings = () => {
  let settings = LocalStorageService.getDashboardSlides()
  if (settings) {
    try {
      settings = JSON.parse(settings)
      if (settings.slideDashboards.length > 0) {
        settingsPopup.value.selectedDashboards = settings.slideDashboards
          .map((v) => {
            const dashboard = dashboards.value.find((k) => k.id === v)
            return dashboard ? dashboard.id : null
          })
          .filter((v) => v !== null)
      }
      if (settings.interval < 60) {
        settingsPopup.value.interval = 'SECS'
        settingsPopup.value.intervalSecs = settings.interval
      } else {
        settingsPopup.value.interval = settings.interval || 60
      }
    } catch (e) {
      console.log('대시보드 설정을 불러오는 중 오류 발생', e)
    }
  }
}

const applySettings = () => {
  if (settingsPopup.value.selectedDashboards.length > 0) {
    slideDashboards.value = settingsPopup.value.selectedDashboards
    if (settingsPopup.value.interval === 'SECS') {
      interval.value = settingsPopup.value.intervalSecs
    } else {
      interval.value = settingsPopup.value.interval
    }
    LocalStorageService.saveDashboardSlides(
      JSON.stringify({ slideDashboards: slideDashboards.value, interval: interval.value }),
    )
    slideIndex.value = 0
    isShowSlides.value = true
    settingsPopup.value.isShow = false
  } else {
    ui.notify(t('대시보드를 하나 이상 선택 해 주세요.'))
  }
}

const closeSlides = () => {
  settingsPopup.value.isShow = false
  if (isShowSlides.value === false) {
    router.back()
  }
}

const localSubTitle = computed(
  () => ` ` + t('대시보드를 선택해 주세요.') + ` (` + t('옵션') + `: ${dashboards.value.length}` + t(`개`) + `)`,
)

watch(
  () => settingsPopup.value.interval,
  (newValue) => {
    if (newValue === 'SECS') {
      if (settingsPopup.value.intervalSecs) {
        settingsPopup.value.intervalSecs
      } else {
        settingsPopup.value.intervalSecs = 59
      }
    } else {
      settingsPopup.value.intervalSecs = null
    }
  },
)

onMounted(async () => {
  await loadDashboards()
  initSettings()
})
</script>

<style scoped lang="scss">
.carousel-slide {
  overflow-y: hidden;
  padding: 0px;
}
.dashboard-carousel {
  max-width: 100%;
  height: 100%;
  background-color: inherit;
}
.dashboard-carousel.fullscreen {
  width: 100vw;
  height: 100vh;
}
.dialog-card {
  width: 600px;
  max-width: 600px;
}
.pop-widget-setting {
  width: 900px;

  .setting-list {
    max-height: 600px;
  }
}
.dashboard-list {
  max-height: 200px;
  overflow-y: auto;
}
</style>
