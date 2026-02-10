<template>
  <q-dialog v-model="isVisible">
    <q-card class="settings-modal">
      <q-card-section class="modal-header">
        <div class="text-h6">설비 설정 보기</div>
        <q-btn flat round dense icon="close" v-close-popup />
      </q-card-section>

      <q-card-section class="modal-content">
        <div class="content-layout">
          <!-- 상단: ToolStatusDisplay -->
          <div class="top-panel">
            <div class="tool-status-wrapper">
              <ToolStatusDisplay @open-product-modal="$emit('openProductModal')" />
            </div>
          </div>

          <!-- 하단: 설정 테이블 -->
          <div class="bottom-panel">
            <div class="settings-table-container">
              <div v-for="(chunk, chunkIndex) in processChunks" :key="chunkIndex" class="table-chunk">
                <table class="settings-table">
                  <thead>
                    <tr>
                      <th class="diagonal-header"></th>
                      <th v-for="process in chunk" :key="process.processName" class="normal-header">
                        {{ process.processName }}
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr>
                      <td class="type-cell">타임설정</td>
                      <td v-for="process in chunk" :key="`setting-${process.processName}`" class="setting-value">
                        {{ process.settingValue }}
                      </td>
                    </tr>
                    <tr>
                      <td class="type-cell">타임현재</td>
                      <td v-for="process in chunk" :key="`current-${process.processName}`" class="current-value">
                        {{ process.currentValue }}
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      </q-card-section>
    </q-card>
  </q-dialog>
</template>

<script setup>
import { computed, defineProps, defineEmits, inject } from 'vue'
import ToolStatusDisplay from './ToolStatusDisplay.vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['update:modelValue', 'openProductModal'])

const toolStatusContext = inject('toolStatusContext')

const isVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value),
})

// processValues를 columnsPerTable 개수만큼 청크로 나누기
const processChunks = computed(() => {
  const chunks = []
  const processValues = toolStatusContext.value.processValuesForModal.value
  const columnsPerTable = toolStatusContext.value.columnsPerTable

  const processData = processValues.map((process) => ({
    processName: process?.processName?.customValue || '-',
    settingValue: process?.settingDisplayValue || '-',
    currentValue: process?.currentDisplayValue || '-',
  }))

  for (let i = 0; i < processData.length; i += columnsPerTable) {
    chunks.push(processData.slice(i, i + columnsPerTable))
  }

  return chunks
})
</script>

<style lang="scss" scoped>
.settings-modal {
  width: auto; // 고정 크기에서 자동 크기로 변경
  min-width: 600px; // 최소 너비 설정
  max-width: 1100px; // 최대 너비 유지
  max-height: 90vh; // 85vh에서 90vh로 확대 (세로 배치로 인한 높이 증가)

  .modal-header {
    background-color: #f5f5f5;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 24px;
    border-bottom: 1px solid #e0e0e0;
  }

  .modal-content {
    padding: 20px;
    max-height: 80vh; // 75vh에서 80vh로 확대
    overflow: auto;
  }

  .content-layout {
    display: flex;
    flex-direction: column; // ��로에서 세로 배치로 변경
    gap: 20px; // 24px에서 20px로 축소
    align-items: flex-start;
  }

  .top-panel {
    flex: none;
    width: 100%;
    display: flex;
    justify-content: flex-start;
  }

  .tool-status-wrapper {
    width: 350px;
    display: flex;
    justify-content: flex-start;
  }

  .bottom-panel {
    flex: 1;
    display: flex;
    flex-direction: column;
    min-width: 0;
  }

  .table-header {
    margin-bottom: 16px;

    h6 {
      margin: 0;
      color: #333;
      font-weight: 600;
    }
  }

  .settings-table-container {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .table-chunk {
    border-radius: 8px;
    overflow: hidden;
  }

  .settings-table {
    border-collapse: collapse;
    width: auto;

    // 헤더 스타일
    th {
      padding: 8px 6px; // 6px 2px에서 8px 6px로 확대
      font-size: 13px;
      font-weight: 600;
      background-color: #f5f5f5;
      text-align: center;
      border: 1px solid #dee2e6; // 모든 방향에 테두리 추가

      // 구분 헤더에 사선 추가
      &.diagonal-header {
        position: relative;
        background-color: #f5f5f5;
        width: 80px;
        min-width: 80px;
        max-width: 80px;

        &::after {
          content: '';
          position: absolute;
          top: 0;
          left: 0;
          width: 100%;
          height: 100%;
          background: url("data:image/svg+xml,%3csvg width='100' height='100' xmlns='http://www.w3.org/2000/svg'%3e%3cline x1='0' y1='0' x2='100' y2='100' stroke='%23000' stroke-width='1'/%3e%3c/svg%3e");
          background-size: 100% 100%;
          background-repeat: no-repeat;
          pointer-events: none;
          z-index: 1;
        }
      }

      &.normal-header {
        background-color: #f5f5f5;
        width: 80px;
        min-width: 80px;
        max-width: 80px;
        font-size: clamp(6px, 1vw, 13px); // 최소 6px부터 시작하여 더 작은 폰트 허용
        line-height: 1.2;
        word-break: keep-all; // 단어 줄바꿈 방지
        hyphens: none; // 하이픈 처리 제거
        height: auto;
        vertical-align: middle;
      }
    }

    // Body 셀 스타일
    td {
      padding: 8px 6px;
      font-size: 13px;
      border: 1px solid #dee2e6; // 모든 방향에 테두리 추가
      text-align: center;

      &.type-cell {
        font-weight: 600;
        background-color: #f8f9fa;
        width: 80px;
        min-width: 80px;
        max-width: 80px;
      }

      &.setting-value,
      &.current-value {
        width: 80px;
        min-width: 80px;
        max-width: 80px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      &.setting-value {
        background-color: #fff3cd;
      }

      &.current-value {
        background-color: #d1ecf1;
      }
    }
  }
}

// 반응형 처리
@media (max-width: 1024px) {
  .settings-modal {
    width: 95vw;

    .content-layout {
      flex-direction: column;
      gap: 16px;
    }

    .top-panel {
      flex: none;
      width: 100%;
    }

    .bottom-panel {
      width: 100%;
    }
  }
}

@media (max-width: 768px) {
  .settings-modal {
    .settings-table {
      th,
      td {
        padding: 6px 4px;
        font-size: 11px;
      }
    }
  }
}
</style>
