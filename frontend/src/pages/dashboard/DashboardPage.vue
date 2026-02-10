<template>
  <q-page class="full-height dashboard-container full-width column no-wrap">
    <div class="q-px-md q-pt-md q-pb-sm row title-area">
      <template v-if="isEditMode">
        <div class="title-area-left">
          <q-input
            v-model="name"
            standout="bg-grey-4 text-white"
            :placeholder="$t('대시보드 이름')"
            dense
            class="input-name"
          />
          <q-btn class="save_btn with_icon_btn" @click="saveDashboard">
            {{ $t('저장') }}
          </q-btn>
          <q-fab v-if="route.params.id" push direction="right">
            <q-fab-action color="red" @click="showDeleteConfirm" icon="delete">
              <q-tooltip v-permission:dashboard.delete class="text-caption"> {{ $t('대시보드 삭제') }}</q-tooltip>
            </q-fab-action>
            <q-fab-action color="blue-grey" @click="setEditMode(false)" icon="cancel">
              <q-tooltip class="text-caption"> {{ $t('변경사항 취소') }}</q-tooltip>
            </q-fab-action>
          </q-fab>
        </div>
        <!-- <q-space /> -->
        <div class="title-area-right">
          <q-btn class="add_btn with_icon_btn" @click="showPopAddWidget" :disable="isDeleteMode === true">{{
            $t('위젯 추가')
          }}</q-btn>
          <q-btn class="delete_btn with_icon_btn red" @click="setDeleteMode(true)">{{ $t('위젯 삭제') }}</q-btn>
        </div>
      </template>
      <template v-else>
        <span class="text-h5 q-ml-sm">{{ name }}</span>
        <!-- <q-space /> -->
        <q-btn
          v-if="!props.isEditableSlide"
          v-permission:dashboard.update
          class="edit_btn with_icon_btn"
          @click="setEditMode(true)"
        >
          {{ $t('수정') }}
        </q-btn>
      </template>
    </div>
    <div class="overflow-auto col">
      <grid-layout
        v-if="widgets && widgets.length > 0"
        v-model:layout="widgets"
        :col-num="12"
        :row-height="30"
        :is-draggable="isEditable"
        :is-resizable="isEditable"
        use-css-transforms
        vertical-compact
        responsive
        is-bounded
      >
        <grid-item
          v-for="(item, index) in widgets"
          :key="item.id"
          :x="item.x"
          :y="item.y"
          :w="item.w"
          :h="item.h"
          :min-w="item.minW"
          :min-h="item.minH"
          :i="item.i"
        >
          <!-- 더미 위젯인 경우 수정 모드에서만 보이도록 처리 -->
          <q-card
            v-if="isLocationAssistantWidget(item.widgetId) || isEditMode"
            bordered
            class="widget-container column"
            :class="{ 'location-assistant-widget-card': !isLocationAssistantWidget(item.widgetId) }"
          >
            <widget-container
              :dashboard-widget-id="item.id"
              :widget-id="item.widgetId"
              :title="item.title"
              :is-edit-mode="isEditMode"
              :is-editable="props.isEditableSlide"
              @update:title="(title) => (item.title = title)"
              @update:config="(config) => (item.config = config)"
              :config="item.config"
              :is-undefined-widget="item.isUndefinedWidget"
            />
            <q-inner-loading showing dark v-if="isDeleteMode">
              <q-checkbox
                :model-value="deleteTargetIdxs.has(index)"
                checked-icon="check_circle"
                unchecked-icon="radio_button_unchecked"
                color="red"
                size="200px"
                keep-color
                @update:model-value="
                  (value) => {
                    if (value) {
                      deleteTargetIdxs.add(index)
                    } else {
                      deleteTargetIdxs.delete(index)
                    }
                  }
                "
                class="delChk-btn"
              />
            </q-inner-loading>
          </q-card>
        </grid-item>
      </grid-layout>
    </div>
    <!-- 위젯 삭제 모드 시 버튼 -->
    <q-page-sticky position="bottom" :offset="[0, 50]" v-if="isDeleteMode">
      <q-btn
        fab
        icon="close"
        color="grey"
        class="q-mr-sm"
        :label="$t('취소')"
        label-position="right"
        @click="setDeleteMode(false)"
      />
      <q-btn fab icon="delete" color="red" :label="$t('삭제')" label-position="right" @click="deleteWidgets" />
    </q-page-sticky>
    <!-- 대시보드 추가 팝업 -->
    <pop-add-widget v-model="isShowPopAddWidget" @confirm="addWidgets" />
  </q-page>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '/src/stores/app'
import { GridItem, GridLayout } from 'grid-layout-plus'
import PopAddWidget from '/src/components/dashboard/PopAddWidget.vue'
import WidgetContainer from '/src/components/dashboard/WidgetContainer.vue'
import { findWidgetById } from '/src/common/constant/widgets'
import DashboardService from 'src/services/dashboard/DashboardService'
import useUI from 'src/common/module/ui'
import { t } from '/src/plugins/i18n'

const ui = useUI()

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const name = ref('') // 대시보드 이름
const widgets = ref([]) //대시보드 위젯

const props = defineProps({
  dashboardId: {
    type: Number,
    default: () => undefined,
  },
  isEditableSlide: {
    type: Boolean,
    required: false,
    default: () => false,
  },
})

onMounted(async () => {
  initDashboard()
})

watch(route, () => {
  initDashboard()
})

const initDashboard = () => {
  clearDashboard()
  const dashboardId = route.params.id || props.dashboardId
  if (dashboardId) {
    setEditMode(false)
    loadDashboard(dashboardId)
  } else {
    setEditMode(true)
  }
}

const clearDashboard = () => {
  name.value = ''
  widgets.value = []
}

const loadDashboard = async (dashboardId) => {
  ui.loading.show()
  const dashboard = await DashboardService.getDashboardById(dashboardId)
  ui.loading.hide()
  name.value = dashboard.name
  widgets.value = dashboard.widgets.map((item, index) => {
    const widgetInfo = findWidgetById(item.widgetId)
    return {
      ...item,
      config: item.config ? JSON.parse(item.config) : null,
      minW: widgetInfo?.minW || item.w,
      minH: widgetInfo?.minH || item.h,
      i: index,
      id: item.id,
      isUndefinedWidget: !widgetInfo, // 정의된 위젯이 없는 경우 구분용
    }
  })
}

/** Edit 모드, 삭제 모드 관련 로직 */
const isEditMode = ref(false)
const setEditMode = (value) => {
  isEditMode.value = value
  setDeleteMode(false)
}
const isDeleteMode = ref(false)
const deleteTargetIdxs = ref(new Set())
const setDeleteMode = (value) => {
  deleteTargetIdxs.value = new Set()
  isDeleteMode.value = value
}
const deleteWidgets = () => {
  widgets.value = widgets.value.filter((widget, index) => !deleteTargetIdxs.value.has(index))
  setDeleteMode(false)
}
const isEditable = computed(() => {
  return isEditMode.value && !isDeleteMode.value
})
/** 위젯 추가 관련 로직 */
const isShowPopAddWidget = ref(false)
const showPopAddWidget = () => {
  isShowPopAddWidget.value = true
}
const addWidgets = (widgetIds) => {
  widgets.value.push(
    ...widgetIds.map((widgetId, index) => {
      const widgetInfo = findWidgetById(widgetId)
      return {
        x: ((widgets.value.length + index) * widgetInfo.minW) % 12,
        y: widgets.value.length + index + 12,
        w: widgetInfo.minW,
        h: widgetInfo.minH,
        minW: widgetInfo.minW,
        minH: widgetInfo.minH,
        i: Math.trunc(new Date().getTime() * Math.random()),
        title: '',
        widgetId,
      }
    }),
  )
}

/** 대시보드 추가/수정/삭제 관련 로직 */
const showDeleteConfirm = () => {
  ui.confirm(t('대시보드 삭제'), t('대시보드를 삭제하시겠습니까?'), t('삭제'), t('취소')).onOk(deleteDashbaord)
}
const deleteDashbaord = async () => {
  ui.loading.show()
  const dashboardId = route.params.id
  await DashboardService.deleteDashboard(dashboardId)
  ui.loading.hide()
  ui.notify.success(t('대시보드가 삭제 되었습니다.'))
  appStore.loadDashboards() //메뉴의 대시보드 목록 초기화
  router.replace('/dashboard')
}
const saveDashboard = async () => {
  const params = {
    name: name.value,
    widgets: widgets.value.map((v) => ({
      ...v,
      config: JSON.stringify(v.config),
    })),
  }
  ui.loading.show()

  const dashboardId = route.params.id
  if (dashboardId) {
    await DashboardService.modifyDashboard(dashboardId, {
      ...params,
    })
  } else {
    const createdDashBoard = await DashboardService.createDashboard(params)
    router.replace(`/dashboard/${createdDashBoard.id}`)
  }
  appStore.loadDashboards() //메뉴의 대시보드 목록 초기화
  ui.loading.hide()
  ui.notify.success(t('저장 되었습니다.'))
  setEditMode(false)
  initDashboard()
}
const isLocationAssistantWidget = (widgetId) => {
  return widgetId !== 'locationAssistant'
}
</script>

<style lang="scss" scoped>
.dashboard-container {
  .input-name {
    display: inline-block;
    width: 700px;
  }

  .title-area {
    height: 60px;
  }
}

.location-assistant-widget-card {
  background: transparent !important;
  border: 1px dashed #ccc !important;

  &:deep(.q-card__section) {
    background: transparent !important;
  }
}
</style>
