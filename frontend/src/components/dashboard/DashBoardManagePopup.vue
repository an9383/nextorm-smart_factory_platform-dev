<template>
  <q-dialog :model-value="visible" @hide="emit('close')" persistent>
    <q-card style="min-width: 600px; max-width: 800px">
      <q-card-section class="row items-center">
        <div class="text-h6">{{ $t('대시보드 관리') }}</div>
        <q-space />
        <q-btn icon="close" flat round dense @click="emit('close')" />
      </q-card-section>

      <q-separator />

      <q-card-section class="q-pa-md">
        <div v-if="isLoading" class="text-center q-pa-lg">
          <q-spinner color="primary" size="3em" />
          <div class="q-mt-md">{{ $t('로딩 중...') }}</div>
        </div>

        <div v-else>
          <div class="text-body2 q-mb-md text-grey-7">
            {{ $t('드래그하여 순서를 변경하고, 토글 버튼으로 보이기/숨기기를 설정할 수 있습니다.') }}
          </div>

          <draggable v-model="dashBoardList" item-key="id" handle=".drag-handle" class="dashboard-list">
            <template #item="{ element: dashboard, index }">
              <div class="dashboard-item q-pa-md q-mb-sm bg-grey-1 rounded-borders">
                <div class="row items-center no-wrap">
                  <!-- 드래그 핸들 -->
                  <q-icon name="drag_indicator" class="drag-handle text-grey-6 cursor-pointer q-mr-md" size="md" />

                  <!-- 순서 번호 -->
                  <div class="text-caption text-grey-6 q-mr-md" style="min-width: 30px">
                    {{ index + 1 }}
                  </div>

                  <!-- 대시보드 이름 -->
                  <div class="col text-body1">
                    {{ dashboard.name }}
                    <div class="text-caption text-grey-6">{{ $t('생성일') }}: {{ formatDate(dashboard.createAt) }}</div>
                  </div>

                  <!-- 토글 버튼 -->
                  <q-toggle
                    :model-value="!dashboard.isHide"
                    @update:model-value="toggleVisibility(dashboard)"
                    color="primary"
                  />
                </div>
              </div>
            </template>
          </draggable>

          <div v-if="dashBoardList.length === 0" class="text-center q-pa-lg text-grey-6">
            {{ $t('등록된 대시보드가 없습니다.') }}
          </div>
        </div>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="q-pa-md">
        <q-btn flat :label="$t('취소')" color="negative" @click="emit('close')" />
        <q-btn
          color="primary"
          :label="$t('저장')"
          :loading="isSaving"
          @click="saveDashboardSort"
          :disable="isLoading"
        />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useQuasar } from 'quasar'
import draggable from 'vuedraggable'
import DashboardService from 'src/services/dashboard/DashboardService.js'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['close', 'updated'])

const $q = useQuasar()
const dashBoardList = ref([])
const isLoading = ref(false)
const isSaving = ref(false)

// 팝업이 열릴 때마다 데이터 로드
watch(
  () => props.visible,
  (newVisible) => {
    if (newVisible) {
      loadDashboards()
    }
  },
)

const loadDashboards = async () => {
  try {
    isLoading.value = true
    dashBoardList.value = await DashboardService.getDashboards()
  } catch (error) {
    console.error('대시보드 목록 로드 실패:', error)
    $q.notify({
      type: 'negative',
      message: '대시보드 목록을 불러오는데 실패했습니다.',
      position: 'top',
    })
  } finally {
    isLoading.value = false
  }
}

// 변경사항 저장 (순서 및 표시/숨김)
const saveDashboardSort = async () => {
  try {
    isSaving.value = true

    // 통합 API로 순서와 표시/숨김을 한번에 저장
    const dashboards = dashBoardList.value.map((dashboard, index) => ({
      id: dashboard.id,
      sort: index + 1,
      isHide: dashboard.isHide,
    }))

    await DashboardService.updateDashboards(dashboards)

    // 변경 플래그 초기화
    dashBoardList.value.forEach((dashboard) => {
      dashboard.isVisibilityChanged = false
    })

    $q.notify({
      type: 'positive',
      message: '대시보드 설정이 저장되었습니다.',
      position: 'top',
    })

    emit('updated')
    emit('close')
  } catch (error) {
    console.error('설정 저장 실패:', error)
    $q.notify({
      type: 'negative',
      message: '설정 저장에 실패했습니다.',
      position: 'top',
    })
  } finally {
    isSaving.value = false
  }
}

// 숨김/보이기 토글 (로컬 상태만 변경)
const toggleVisibility = (dashboard) => {
  dashboard.isHide = !dashboard.isHide
  dashboard.isVisibilityChanged = true // 변경 플래그 추가
}

// 날짜 포맷팅
const formatDate = (dateString) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  })
}
</script>

<style scoped>
.dashboard-list {
  max-height: 400px;
  overflow-y: auto;
}

.dashboard-item {
  border: 1px solid #e0e0e0;
  transition: all 0.2s;
}

.dashboard-item:hover {
  border-color: #1976d2;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.drag-handle {
  cursor: grab;
}

.drag-handle:active {
  cursor: grabbing;
}

.sortable-ghost {
  opacity: 0.5;
  background: #f5f5f5;
}

.sortable-chosen {
  opacity: 0.8;
}
</style>
