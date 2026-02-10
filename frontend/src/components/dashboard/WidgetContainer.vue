<template>
  <!-- 위젯 타이틀 영역 -->
  <q-card-section class="q-py-sm q-px-sm full-width widget-title">
    <div class="row justify-between">
      <template v-if="isEditMode && !isUndefinedWidget">
        <q-input
          :model-value="title"
          :placeholder="`${widgetInfo.name} ${$t('위젯 이름')}`"
          dense
          standout="bg-grey-4 text-white"
          class="text-subtitle1 col widget-name-input"
          @update:model-value="onTitleChanged"
        />
        <q-btn flat round color="blue-grey-8" icon="settings" size="13px" @click="isPopSettingShow = true" />
      </template>
      <template v-else>
        <div class="col text-subtitle1 ellipsis text-weight-bold">
          {{ title }}
        </div>
        <q-btn
          v-if="config"
          :loading="isRefreshing"
          flat
          round
          color="blue-grey-8"
          icon="refresh"
          size="13px"
          @click="refresh"
        />
      </template>
    </div>
  </q-card-section>
  <!-- 위젯 컨텐츠 영역 -->
  <q-card-section class="column col overflow-auto q-pa-sm">
    <template v-if="!isUndefinedWidget">
      <component
        v-show="config"
        :is="widgetInfo.component"
        ref="widgetComponent"
        :dashboard-widget-id="props.dashboardWidgetId"
        :widget-id="props.widgetId"
        :config="config"
        :refreshable="!isEditMode"
        @refreshStart="onRefreshStarted"
        @refreshEnd="onRefreshEnded"
      />
    </template>
    <div v-if="!config" class="column justify-center full-height">
      <div class="text-center">
        <q-icon name="warning" color="warning" size="20px" />
        {{ $t('위젯 설정이 없습니다.') }}
      </div>
    </div>
    <div v-if="isUndefinedWidget" class="column justify-center full-height">
      <div class="text-center">
        <q-icon name="warning" color="warning" size="20px" />
        {{ $t('위젯 정보를 찾을 수 없습니다.') }}
      </div>
      <div class="text-center">
        {{ $t('새로고침 후에도 문제가 발생시 관리자에게 문의해주세요') }}
      </div>
    </div>
  </q-card-section>
  <!-- 위젯 설정 팝업 -->
  <pop-widget-setting
    v-if="isPopSettingShow"
    v-model="isPopSettingShow"
    :setting-component="widgetInfo.settingComponent"
    :widget-name="widgetInfo.name"
    :config="_.cloneDeep(config)"
    @update:config="onConfigChanged"
  />
</template>

<script setup>
import { ref } from 'vue'
import { findWidgetById } from 'src/common/constant/widgets'
import PopWidgetSetting from './PopWidgetSetting.vue'
import _ from 'lodash'

const props = defineProps({
  dashboardWidgetId: Number, //대시보드 위젯 ID
  widgetId: String, //위젯 ID
  title: String, //위젯 제목
  isEditMode: Boolean, //대시보드 Edit Mode 여부
  config: Object, //위젯 config
  isUndefinedWidget: Boolean, // 정의되지 않은 위젯 여부
})

//실제 위젯 컴포넌트
const widgetComponent = ref(null)

const widgetInfo = ref(findWidgetById(props.widgetId))
const emit = defineEmits(['update:title', 'update:config'])

const onTitleChanged = (title) => {
  emit('update:title', title)
}

const onConfigChanged = (config) => {
  emit('update:config', config)
}

//위젯 설정 팝업 노출 여부
const isPopSettingShow = ref(false)

/** 위젯 refresh 관련 로직 */
const isRefreshing = ref(false)
const refresh = () => {
  widgetComponent.value?.refreshAction()
}
const onRefreshStarted = () => {
  isRefreshing.value = true
}
const onRefreshEnded = () => {
  isRefreshing.value = false
}
</script>

<style lang="scss" scoped>
.widget-title {
  .q-btn {
    :deep(.q-btn__content) {
      margin-bottom: 10px;
    }

    :deep(.absolute-full) {
      bottom: 10px;
    }
  }
}

.widget-name-input:deep(.q-field__control) {
  height: 26px;
}
</style>
