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
              <BoilerToolStatusDisplay
                :is-connected="isConnected"
                :is-running="isRunning"
                :tool-name="toolName"
                :detect-alarm="detectAlarm"
                :current-temperature="currentTemperature"
                :current-pressure="currentPressure"
                @open-product-modal="$emit('openProductModal')"
              />
            </div>
          </div>

          <!-- 하단: 설정 테이블 -->
          <div class="row">
            <!-- 상태 램프 정보 테이블 -->
            <div class="col">
              <table class="settings-table">
                <thead>
                  <tr>
                    <th>상태</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="param in statusLampParameters" :key="param.parameterId">
                    <td :class="parameterValues[param.parameterId]?.value === 1 && 'active'">
                      {{ parameterValues[param.parameterId]?.name }}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
            <!-- 상태값 정보 테이블 -->
            <div class="col">
              <table class="settings-table">
                <thead>
                  <tr>
                    <th colspan="2">상태값</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="param in statusValueParameters" :key="param.parameterId">
                    <td>
                      {{ parameterValues[param.parameterId]?.name }}
                    </td>
                    <td>
                      {{ getStatusValue(param.parameterId) }}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
            <!-- 알람정보 테이블 -->
            <div class="col">
              <table class="settings-table">
                <thead>
                  <tr>
                    <th :colspan="alarmColumnCount">알람 (발생시 빨간색 배경)</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="parameters in verticalArrayAlarmParameters" :key="parameters.parameterId">
                    <td
                      v-for="param in parameters"
                      :key="param"
                      :class="parameterValues[param.parameterId]?.value === 1 && 'alert'"
                    >
                      {{ parameterValues[param.parameterId]?.name }}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </q-card-section>
    </q-card>
  </q-dialog>
</template>

<script setup>
import { computed, defineProps, defineEmits } from 'vue'
import BoilerToolStatusDisplay from './BoilerToolStatusDisplay.vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false,
  },
  isConnected: {
    type: Boolean,
    default: false,
  },
  isRunning: {
    type: Boolean,
    default: false,
  },
  toolName: {
    type: String,
    default: '-',
  },
  detectAlarm: {
    type: Boolean,
    default: false,
  },
  currentTemperature: {
    type: String,
    default: '-',
  },
  currentPressure: {
    type: String,
    default: '-',
  },
  statusParameters: {
    type: Array,
    default: () => [],
  },
  parameterValues: {
    type: Object,
    default: () => {},
  },
})

const emit = defineEmits(['update:modelValue', 'openProductModal'])

const isVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value),
})

const statusLampParameters = computed(() => props.statusParameters.filter((param) => param.type === 'status'))
const statusValueParameters = computed(() => props.statusParameters.filter((param) => param.type === 'value'))

const ALARM_MAX_ROW = 10
const statusAlarmParameters = computed(() => props.statusParameters.filter((param) => param.type === 'alarm'))
const alarmColumnCount = computed(() =>
  statusAlarmParameters.value.length > ALARM_MAX_ROW
    ? Math.ceil(statusAlarmParameters.value.length / ALARM_MAX_ROW)
    : 1,
)
const verticalArrayAlarmParameters = computed(() => convertToVerticalArray(statusAlarmParameters.value, ALARM_MAX_ROW))

const convertToVerticalArray = (originalArray, divCount) => {
  const totalCount = originalArray.length
  const maxRowCount = Math.ceil(totalCount / divCount)
  const result = []

  for (let col = 0; col < divCount; col++) {
    const columnArray = []

    for (let row = 0; row < maxRowCount; row++) {
      const index = col + row * divCount

      if (index < totalCount) {
        columnArray.push(originalArray[index])
      }
    }

    if (columnArray.length > 0) {
      result.push(columnArray)
    }
  }

  return result
}

const getStatusValue = (parameterId) => {
  const parameterData = props.parameterValues[parameterId]
  if (!parameterData) {
    return '-'
  }

  const value = parameterData.value
  if (value == null || value === '' || isNaN(Number(value))) {
    return '-'
  }
  // 소수점 2자리까지만 사용
  const roundValue = Math.round(value * 100) / 100
  return `${roundValue}${parameterData.unit || ''}`
}
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

  .table-header {
    margin-bottom: 16px;

    h6 {
      margin: 0;
      color: #333;
      font-weight: 600;
    }
  }

  .table-chunk {
    border-radius: 8px;
    overflow: hidden;
  }

  .col {
    flex: auto;
    margin: 10px;
  }

  td.active {
    background-color: #1eec4c; // 녹색 배경
  }

  td.alert {
    background-color: #ff4c4c; // 빨간색 배경
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
      padding: 8px 20px;
      font-size: 13px;
      border: 1px solid #dee2e6; // 모든 방향에 테두리 추가
      text-align: center;
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
