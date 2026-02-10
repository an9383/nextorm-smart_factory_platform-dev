<template>
  <!-- 데이터 수집 기준 시간 설정 -->
  <q-card flat bordered class="q-mt-md">
    <q-card-section class="bg-primary text-white">
      <div class="text-subtitle2">
        <q-icon name="refresh" color="white" class="q-mr-xs" size="20px" />
        {{ t('데이터 수집 기준') }}
      </div>
      <div class="text-caption">{{ t('데이터 수집 주기를 선택해 주세요.') }}</div>
    </q-card-section>
    <q-card-section class="row items-center q-my-sm">
      <q-input
        v-model.number="config.dataInterval"
        type="number"
        dense
        outlined
        style="width: 120px"
        :rules="[$rules.natural, $rules.required]"
      >
      </q-input>
      <div class="q-ml-sm">{{ t('초') }}</div>
    </q-card-section>
  </q-card>

  <!-- 설비 정보 설정 -->
  <q-card flat bordered class="q-mt-md">
    <q-card-section class="bg-secondary text-white">
      <div class="text-subtitle2">
        <q-icon name="settings" color="white" class="q-mr-xs" size="20px" />
        {{ t('설비 정보 설정') }}
      </div>
      <div class="text-caption">{{ t('설비 관련 파라미터를 선택해 주세요.') }}</div>
    </q-card-section>
    <q-card-section class="q-pa-lg">
      <div class="row q-col-gutter-lg">
        <!-- 설비 네트워크 연결 상태 -->
        <div class="col-12 col-md-6">
          <div class="equipment-field">
            <div class="field-label q-mb-sm">
              <q-icon name="power_settings_new" color="primary" class="q-mr-xs" size="18px" />
              {{ $t('설비 연결상태') }}
            </div>
            <cascader
              :model-value="parameterIdToTreeIdMap.get(config.connectionParameterId)"
              @update:model-value="(treeId) => onEquipmentParamChanged('connectionParameterId', treeId)"
              :titles="[$t('설비'), $t('파라미터')]"
              :items="parameterToolTree"
              item-value="treeId"
              item-text="name"
              clearable
              :valid-depth="2"
              class="full-width"
            />
          </div>
        </div>

        <!-- 설비 상태 -->
        <div class="col-12 col-md-6">
          <div class="equipment-field">
            <div class="field-label q-mb-sm">
              <q-icon name="power_settings_new" color="primary" class="q-mr-xs" size="18px" />
              {{ $t('설비 상태') }}
            </div>
            <cascader
              :model-value="parameterIdToTreeIdMap.get(config.statusParameterId)"
              @update:model-value="(treeId) => onEquipmentParamChanged('statusParameterId', treeId)"
              :titles="[$t('설비'), $t('파라미터')]"
              :items="parameterToolTree"
              item-value="treeId"
              item-text="name"
              clearable
              :valid-depth="2"
              class="full-width"
            />
          </div>
        </div>

        <!-- 현재 온도 -->
        <div class="col-12 col-md-6">
          <div class="equipment-field">
            <div class="field-label q-mb-sm">
              <q-icon name="error" color="negative" class="q-mr-xs" size="18px" />
              {{ $t('현재 온도') }}
            </div>
            <cascader
              :model-value="parameterIdToTreeIdMap.get(config.currentTemperatureParameterId)"
              @update:model-value="(treeId) => onEquipmentParamChanged('currentTemperatureParameterId', treeId)"
              :titles="[$t('설비'), $t('파라미터')]"
              :items="parameterToolTree"
              item-value="treeId"
              item-text="name"
              clearable
              :valid-depth="2"
              class="full-width"
            />
          </div>
        </div>
        <div class="col-12 col-md-6">
          <div class="equipment-field">
            <div class="field-label q-mb-sm">
              <q-icon name="error" color="negative" class="q-mr-xs" size="18px" />
              {{ $t('현재 압력') }}
            </div>
            <cascader
              :model-value="parameterIdToTreeIdMap.get(config.currentPressureParameterId)"
              @update:model-value="(treeId) => onEquipmentParamChanged('currentPressureParameterId', treeId)"
              :titles="[$t('설비'), $t('파라미터')]"
              :items="parameterToolTree"
              item-value="treeId"
              item-text="name"
              clearable
              :valid-depth="2"
              class="full-width"
            />
          </div>
        </div>
      </div>
    </q-card-section>
  </q-card>

  <!-- 테이블 값 설정 -->
  <q-card flat bordered class="q-mt-md">
    <q-card-section class="bg-info text-white">
      <div class="text-subtitle2">
        <q-icon name="table_chart" color="white" class="q-mr-xs" size="20px" />
        {{ t('설비 상태 파라미터 설정') }}
      </div>
      <div class="text-caption">{{ t('설비 상태에 관련된 파라미터를 선택해주세요') }}</div>
    </q-card-section>
    <q-card-section>
      <div class="text-subtitle2 q-mb-sm row items-center">
        <q-btn color="primary" icon="add" @click="addRow" class="q-ml-sm">
          {{ t('행 추가') }}
          <q-tooltip>{{ t('행 추가') }}</q-tooltip>
        </q-btn>
      </div>

      <!-- 테이블 형태로 표시 -->
      <div class="table-preview">
        <q-table
          :rows="config.statusParameters"
          :columns="fixedColumns"
          row-key="index"
          flat
          bordered
          dense
          :pagination="{ rowsPerPage: 0 }"
          hide-pagination
        >
          <!-- 헤더 슬롯 -->
          <template v-slot:header="props">
            <q-tr :props="props">
              <!-- 드래그 핸들 컬럼 -->
              <q-th class="bg-grey-2" style="width: 40px">
                <q-icon name="drag_indicator" color="grey-6" size="18px" />
              </q-th>
              <q-th
                v-for="col in props.cols"
                :key="col.name"
                :props="props"
                :style="{ width: getColumnWidth(col.name) }"
                class="bg-grey-2"
              >
                {{ col.label }}
              </q-th>
              <q-th class="bg-grey-2" style="width: 60px">
                {{ t('삭제') }}
              </q-th>
            </q-tr>
          </template>

          <!-- 바디 슬롯 -->
          <template v-slot:body="props">
            <q-tr
              :props="props"
              :draggable="true"
              @dragstart="onDragStart($event, props.rowIndex)"
              @dragover="onDragOver($event)"
              @drop="onDrop($event, props.rowIndex)"
              :class="{ 'drag-over': dragOverIndex === props.rowIndex }"
            >
              <!-- 드래그 핸들 -->
              <q-td class="drag-handle">
                <q-icon
                  name="drag_indicator"
                  color="grey-6"
                  size="18px"
                  class="cursor-grab"
                  @mousedown="onDragHandleMouseDown"
                />
              </q-td>
              <!-- 순서 컬럼 -->
              <q-td :key="'order'">
                <div class="cell-content">
                  {{ props.rowIndex + 1 }}
                </div>
              </q-td>

              <!-- 파라미터 컬럼 -->
              <q-td>
                <div class="cell-content">
                  <cascader
                    :model-value="parameterIdToTreeIdMap.get(config.statusParameters[props.rowIndex].parameterId)"
                    @update:model-value="
                      (treeId) => {
                        const parameterId = findParameterIdByTreeId(treeId)
                        if (parameterId === null) {
                          config.statusParameters[props.rowIndex].parameterId = null
                          return
                        }
                        parameterIdToTreeIdMap.set(parameterId, treeId)
                        config.statusParameters[props.rowIndex].parameterId = parameterId
                      }
                    "
                    :titles="[$t('설비'), $t('파라미터')]"
                    :items="parameterToolTree"
                    item-value="treeId"
                    item-text="name"
                    clearable
                    :valid-depth="2"
                    class="full-width"
                  />
                </div>
              </q-td>

              <!-- 타입 컬럼 -->
              <q-td>
                <div class="cell-content">
                  <q-select
                    v-model="config.statusParameters[props.rowIndex].type"
                    :options="STATUS_PARAMETER_TYPE"
                    map-options
                    emit-value
                    dense
                    outlined
                  />
                </div>
              </q-td>
              <q-td>
                <q-btn flat round dense color="negative" icon="delete" size="sm" @click="removeRow(props.rowIndex)">
                  <q-tooltip>{{ t('행 삭제') }}</q-tooltip>
                </q-btn>
              </q-td>
            </q-tr>
          </template>
        </q-table>
      </div>
    </q-card-section>
  </q-card>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useWidgetSettingConfig, widgetSettingProps } from 'src/common/module/widgetCommon'
import ParameterService from 'src/services/modeling/ParameterService'
import Cascader from 'components/cascader/Cascader.vue'
import { pt } from 'src/plugins/i18n'

defineProps(widgetSettingProps)

const { t } = useI18n()

// 설비 상태 파라미터 타입
const STATUS_PARAMETER_TYPE = [
  { label: t('상태'), value: 'status' },
  { label: t('값'), value: 'value' },
  { label: t('알람'), value: 'alarm' },
]

const config = useWidgetSettingConfig({
  dataInterval: 30, // 기본값 30초
  // 설비 정보 파라미터 ID들
  statusParameterId: null, // 설비 상태
  connectionParameterId: null, // 설비 연결 상태
  currentTemperature: null, // 현재 온도
  statusParameters: [], // 상태를 표현하는 파라미터
})

const parameterToolTree = ref([])
const parameterIdToTreeIdMap = reactive(new Map()) // key: parameterId, value: treeId

// 드래그 앤 드롭 관련
const dragOverIndex = ref(null)

const flattenTree = computed(() => {
  const flatTree = (items) => {
    return items.flatMap((item) => [item, ...(item.children ? flatTree(item.children) : [])])
  }
  return parameterToolTree.value.flatMap((item) => [item, ...(item.children ? flatTree(item.children) : [])])
})

const loadTreeOfToolsAndParameters = async () => {
  const toolParameterTree = await ParameterService.getParameterToolTree()
  parameterToolTree.value = toolParameterTree.map((tool) => {
    return {
      ...tool,
      children: tool.children.map((parameter) => ({
        ...parameter,
        name: pt(parameter.name),
      })),
    }
  })
}

// 고정된 테이블 컬럼 정의 (순서, 파라미터, 타입(상태 or 값))
const fixedColumns = [
  { name: 'order', label: t('순서'), field: 'order', align: 'center' },
  { name: 'parameter', label: t('파라미터'), field: 'parameter', align: 'left' },
  { name: 'type', label: t('타입 (상태 or 값)'), field: 'type', align: 'center' },
]

// 컬럼 너비 가져오기
const getColumnWidth = () => {
  // 모든 컬럼을 동일한 너비로 자동 설정
  return 'auto'
}

// 행 추가
const addRow = () => {
  const newRow = {
    parameterId: null,
    type: STATUS_PARAMETER_TYPE[0].value,
  }
  config.value.statusParameters = [...config.value.statusParameters, newRow]
}

// 행 제거
const removeRow = (rowIndex) => {
  config.value.statusParameters.splice(rowIndex, 1)
  config.value.statusParameters = [...config.value.statusParameters]
}

/**
 * treeId로 cascader에서 선택된 파라미터의 ID를 찾는다. 없으면 null 반환
 * @param treeId
 */
const findParameterIdByTreeId = (treeId) => {
  if (treeId === null) {
    // 클리어 이벤트가 들어온 경우
    return null
  }

  const found = flattenTree.value.find((v) => v.treeId === treeId)
  if (found?.type.toUpperCase() !== 'PARAMETER') {
    return null
  }
  return found.id
}

// 설비 정보 파라미터 변경 시 호출
const onEquipmentParamChanged = (paramName, treeId) => {
  if (treeId === null) {
    // 클리어 이벤트가 들어온 경우
    config.value[paramName] = null
    return
  }
  const found = flattenTree.value.find((v) => v.treeId === treeId)
  if (found?.type.toUpperCase() !== 'PARAMETER') {
    return
  }
  parameterIdToTreeIdMap.set(found.id, treeId)
  config.value[paramName] = found.id
}

// 드래그 시작
const onDragStart = (event, rowIndex) => {
  event.dataTransfer.effectAllowed = 'move'
  event.dataTransfer.setData('text/plain', rowIndex)
}

// 드래그 오버
const onDragOver = (event) => {
  event.preventDefault()
  event.dataTransfer.dropEffect = 'move'
}

// 드롭
const onDrop = (event, rowIndex) => {
  event.preventDefault()

  const fromIndex = event.dataTransfer.getData('text/plain')
  const toIndex = rowIndex

  if (fromIndex === toIndex) {
    return
  }

  const movedRow = config.value.statusParameters[fromIndex]
  config.value.statusParameters.splice(fromIndex, 1)
  config.value.statusParameters.splice(toIndex, 0, movedRow)

  dragOverIndex.value = null
}

// 드래그 핸들 마우스 다운
const onDragHandleMouseDown = (event) => {
  event.stopPropagation()
}

onMounted(async () => {
  await loadTreeOfToolsAndParameters()

  // 기존 설비 정보 파라미터 매핑 복원 (제품명 제외)
  const equipmentParams = [
    'statusParameterId',
    'processErrorMessageParameterId',
    'connectionParameterId',
    'currentTemperatureParameterId',
    'currentPressureParameterId',
  ]
  equipmentParams.forEach((paramName) => {
    const parameterId = config.value[paramName]
    if (parameterId) {
      const found = flattenTree.value.find((v) => v.id === parameterId)
      if (found?.treeId) {
        parameterIdToTreeIdMap.set(parameterId, found.treeId)
      }
    }
  })

  // 기존 테이블 행들의 파라미터 매핑 복원
  config.value.statusParameters.forEach((row) => {
    const { parameterId } = row
    const found = flattenTree.value.find((v) => v.id === parameterId)
    if (found?.treeId) {
      parameterIdToTreeIdMap.set(parameterId, found.treeId)
    }
  })
})
</script>

<style scoped>
.table-preview {
  overflow-x: auto;
}

.cell-content {
  display: flex;
  align-items: center;
}

.equipment-field {
  margin-bottom: 1.5rem;
}

.field-label {
  font-weight: 500;
  font-size: 0.875rem;
}

/* 드래그 앤 드롭 관련 스타일 */
.drag-handle {
  text-align: center;
  vertical-align: middle;
}

.drag-handle .cursor-grab {
  cursor: grab;
}

.drag-handle .cursor-grab:active {
  cursor: grabbing;
}

.drag-over {
  background-color: rgba(25, 118, 210, 0.1) !important;
  border: 2px dashed #1976d2;
}

.q-table tbody tr {
  transition: background-color 0.2s;
}

.q-table tbody tr:hover {
  background-color: rgba(0, 0, 0, 0.03);
}
</style>
