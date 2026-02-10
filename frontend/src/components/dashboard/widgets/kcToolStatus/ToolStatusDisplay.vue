<template>
  <div class="stiffener-card">
    <!-- 헤더 섹션 -->
    <div class="card-header">
      <!-- 계획대비 생산율 -->
      <div class="equipment-image-section">
        <div class="parameter-item">
          <div class="parameter-label">계획대비 생산율</div>
          <div class="parameter-value">{{ productionRate ? `${productionRate}` : '0' }}</div>
        </div>
      </div>

      <!-- 정보 영역 (상하 분할) -->
      <div class="info-section">
        <!-- 상단 행: 상태 정보와 제품명 라벨 -->
        <div class="info-top-row">
          <div class="status-controls">
            <div class="status-badge" :class="isConnected ? '' : 'status-off'">
              {{ isConnected ? '통신 ON' : '통신 OFF' }}
            </div>
            <div class="status-badge" :class="isRunning ? '' : 'status-off'">
              {{ isRunning ? '가동중' : '정지' }}
            </div>
            <q-btn v-if="showSettingsDisplayModalButton" class="search_btn sBtn" @click="$emit('openSettingsModal')">
              {{ '설정보기' }}
            </q-btn>
          </div>
          <div class="product-label">제품명</div>
        </div>

        <!-- 하단 행: 설비명과 실제 제품명 -->
        <div class="info-bottom-row">
          <div class="equipment-name">{{ toolName }}</div>
          <div class="product-section">
            <q-btn
              class="product-name-button"
              :class="isProductModalButtonDisabled ? 'disabled' : ''"
              :disable="isProductModalButtonDisabled"
              flat
              no-caps
              @click="$emit('openProductModal')"
            >
              {{ productName }}
              <q-tooltip
                v-if="isProductModalButtonDisabled"
                style="font-size: 12pt; font-weight: 500"
                anchor="top middle"
                self="bottom middle"
              >
                설정한 제품정보를 반영중입니다
              </q-tooltip>
            </q-btn>
          </div>
        </div>
      </div>
    </div>

    <!-- 파라미터 그리드 섹션 -->
    <div class="parameters-grid">
      <!-- 첫 번째 줄: 4:6 비율 -->
      <div class="parameter-item">
        <div class="parameter-label">카운터</div>
        <div class="parameter-value">{{ calculateProductionCount || '-' }}</div>
      </div>

      <div class="parameter-item">
        <div class="parameter-label">회전수</div>
        <div class="parameter-value">{{ productionCount }}</div>
      </div>

      <div class="parameter-item">
        <div class="parameter-label">Cavity</div>
        <div class="parameter-value">{{ cavity || '-' }}</div>
      </div>

      <div class="parameter-item">
        <div class="parameter-label">사이클 타임</div>
        <div class="parameter-value">{{ cycleTime || '-' }}</div>
      </div>

      <!-- 두 번째 줄: 5:5 비율을 위한 컨테이너 -->
      <div class="second-row">
        <div class="parameter-item">
          <div class="parameter-label">현재 공정</div>
          <div class="parameter-value">{{ currentProcess }}</div>
        </div>

        <div class="parameter-item">
          <div class="parameter-label">공정 에러</div>
          <div class="parameter-value">{{ processError || '-' }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, defineEmits, ref, onMounted, watch, inject } from 'vue'
import EqmsResourceService from 'src/services/EqmsResourceService'

defineProps({
  showSettingsDisplayModalButton: {
    type: Boolean,
    default: false,
  },
})

const toolStatusContext = inject('toolStatusContext')

// context에서 필요한 데이터만 computed로 추출
const isConnected = computed(() => toolStatusContext.value.isToolConnected.value)
const isRunning = computed(() => toolStatusContext.value.isToolRunning.value)
const toolName = computed(() => toolStatusContext.value.statusParameterToolName)
const productionCount = computed(() => toolStatusContext.value.productionCount.value)
const cycleTime = computed(() => toolStatusContext.value.cycleTime.value)
const productId = computed(() => toolStatusContext.value.productId.value)
const productIdMetaValue = computed(() => toolStatusContext.value.productIdMetaValue)
const cavity = computed(() => toolStatusContext.value.cavity.value)
const calculateProductionCount = computed(() => toolStatusContext.value.calculateProductionCount.value)
const productionRate = computed(() => toolStatusContext.value.productionRate.value)
const currentProcess = computed(() => toolStatusContext.value.currentProcess.value)
const processError = computed(() => toolStatusContext.value.processErrorMessage.value)

const moldProducts = ref([])
const product = ref(null)

defineEmits(['openProductModal', 'openSettingsModal'])

const productName = computed(() => {
  return product.value ? product.value.name : '-'
})

// 메타 값과 현재 제품 ID가 다르면 버튼 비활성화
const isProductModalButtonDisabled = computed(() => {
  // string 타입으로 만들어서 비교
  const metaStr = productIdMetaValue.value + ''
  const valueStr = productId.value + ''
  return metaStr !== valueStr
})

const getMoldProducts = async () => {
  moldProducts.value = await EqmsResourceService.getMoldProducts()
  if (productId.value) {
    product.value = moldProducts.value.find((p) => p.id === productId.value) || null
  }
}

onMounted(async () => getMoldProducts())

watch(productId, async (newVal) => {
  if (newVal) {
    product.value = moldProducts.value.find((p) => p.id === newVal) || null
  } else {
    product.value = null
  }
})
</script>

<style lang="scss" scoped>
.stiffener-card {
  width: 100%;
  height: 100%;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.card-header {
  background-color: #f7f7f9;
  padding: 4px 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.equipment-image-section {
  flex: 0 0 auto;
  border: 4px solid white;
  border-radius: 8px;
}

.main-equipment-icon {
  width: 83px;
  height: 55px;
  object-fit: contain;
}

.info-section {
  display: flex;
  flex-direction: column;
  justify-content: center;
  flex: 1;
  margin-left: 8px;
  gap: 2px;
}

.info-top-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 4px;
}

.status-controls {
  display: flex;
  align-items: center;
  gap: 2px;
}

.status-badge {
  background-color: #4caf50;
  color: white;
  padding: 1px 6px;
  border-radius: 10px;
  font-size: 12px;
  text-align: center;
  line-height: 1.2;
}

.status-off {
  background-color: #f44336;
}

.product-label {
  font-size: 11px;
  color: #888;
  text-align: right;
}

.info-bottom-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.equipment-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.product-section {
  flex: 1;
  text-align: right;
}

.parameters-grid {
  display: grid;
  grid-template-columns: 2.5fr 2.5fr 2.5fr 2.5fr;
  grid-template-rows: 1fr 1fr;
  gap: 4px;
  padding: 4px;
}

.parameter-item {
  background-color: #f1f1f3;
  border-radius: 8px;
  padding: 4px;
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  min-height: 50px;
  justify-content: center;
}

.second-row {
  grid-column: 1 / -1;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 4px;
  background: none;
  padding: 0;
}

.parameter-label {
  font-size: 13px;
  color: #333;
  margin-bottom: 4px;
}

.parameter-value {
  font-size: clamp(8px, 4vw, 14px);
  font-weight: 500;
  color: #111;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
  width: 100%;
}

.parameter-item.large {
  grid-column: span 2;
}

.large-value {
  font-size: 24px;
  font-weight: 700;
  color: #111;
}

.parameter-timestamp {
  font-size: 10px;
  color: #aaa;
  margin-top: 4px;
}

.product-name-button {
  font-size: clamp(10px, 2vw, 14px);
  font-weight: 500;
  color: #333;
  padding: 4px 8px;
  border: 1px dashed #ccc;
  border-radius: 4px;
  background-color: #f9f9f9;
  transition: all 0.2s ease;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 150px;
  min-width: 0;

  &:hover {
    background-color: #e3f2fd;
    border-color: #2196f3;
    color: #1976d2;
  }
}

.product-name-button.disabled {
  background-color: #fc8c83;
}

.search_btn {
  height: 20px !important;
  min-height: 20px !important;
  font-size: 11px !important;
  padding: 0 6px !important;
  border-radius: 10px !important;
  background-color: #e3f2fd !important;
  color: #1976d2 !important;
  border: 1px solid #90caf9 !important;

  &:hover {
    background-color: #bbdefb !important;
    border-color: #64b5f6 !important;
  }

  .q-btn__content {
    line-height: 1 !important;
    min-height: auto !important;
  }
}
</style>
