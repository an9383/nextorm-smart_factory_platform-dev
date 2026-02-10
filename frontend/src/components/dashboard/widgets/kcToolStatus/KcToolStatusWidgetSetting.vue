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

        <!-- 생산량 -->
        <div class="col-12 col-md-6">
          <div class="equipment-field">
            <div class="field-label q-mb-sm">
              <q-icon name="format_list_numbered" color="primary" class="q-mr-xs" size="18px" />
              {{ $t('생산량') }}
            </div>
            <cascader
              :model-value="parameterIdToTreeIdMap.get(config.productionCountParameterId)"
              @update:model-value="(treeId) => onEquipmentParamChanged('productionCountParameterId', treeId)"
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

        <!-- 사이클 타임 -->
        <div class="col-12 col-md-6">
          <div class="equipment-field">
            <div class="field-label q-mb-sm">
              <q-icon name="format_list_numbered" color="primary" class="q-mr-xs" size="18px" />
              {{ $t('사이클 타임') }}
            </div>
            <cascader
              :model-value="parameterIdToTreeIdMap.get(config.cycleTimeParameterId)"
              @update:model-value="(treeId) => onEquipmentParamChanged('cycleTimeParameterId', treeId)"
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

        <!-- 진행 공정 -->
        <div class="col-12 col-md-6">
          <div class="equipment-field">
            <div class="field-label q-mb-sm">
              <q-icon name="play_circle" color="primary" class="q-mr-xs" size="18px" />
              {{ $t('진행 공정') }}
            </div>
            <cascader
              :model-value="parameterIdToTreeIdMap.get(config.processNameParameterId)"
              @update:model-value="(treeId) => onEquipmentParamChanged('processNameParameterId', treeId)"
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

        <!-- 에러 -->
        <div class="col-12 col-md-6">
          <div class="equipment-field">
            <div class="field-label q-mb-sm">
              <q-icon name="error" color="negative" class="q-mr-xs" size="18px" />
              {{ $t('에러') }}
            </div>
            <cascader
              :model-value="parameterIdToTreeIdMap.get(config.processErrorMessageParameterId)"
              @update:model-value="(treeId) => onEquipmentParamChanged('processErrorMessageParameterId', treeId)"
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

        <!-- 제품ID -->
        <div class="col-12 col-md-6">
          <div class="equipment-field">
            <div class="field-label q-mb-sm">
              <q-icon name="error" color="negative" class="q-mr-xs" size="18px" />
              {{ $t('제품 ID') }}
            </div>
            <cascader
              :model-value="parameterIdToTreeIdMap.get(config.productIdParameterId)"
              @update:model-value="(treeId) => onEquipmentParamChanged('productIdParameterId', treeId)"
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

        <!-- 캐비티 -->
        <div class="col-12 col-md-6">
          <div class="equipment-field">
            <div class="field-label q-mb-sm">
              <q-icon name="error" color="negative" class="q-mr-xs" size="18px" />
              {{ $t('캐비티') }}
            </div>
            <cascader
              :model-value="parameterIdToTreeIdMap.get(config.cavityParameterId)"
              @update:model-value="(treeId) => onEquipmentParamChanged('cavityParameterId', treeId)"
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

        <!-- 평균생산수량 -->
        <div class="col-12 col-md-6">
          <div class="equipment-field">
            <div class="field-label q-mb-sm">
              <q-icon name="error" color="negative" class="q-mr-xs" size="18px" />
              {{ $t('평균생산수량') }}
            </div>
            <cascader
              :model-value="parameterIdToTreeIdMap.get(config.averageProductionCountParameterId)"
              @update:model-value="(treeId) => onEquipmentParamChanged('averageProductionCountParameterId', treeId)"
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

        <!-- 총 제품 생산수량 -->
        <div class="col-12 col-md-6">
          <div class="equipment-field">
            <div class="field-label q-mb-sm">
              <q-icon name="error" color="negative" class="q-mr-xs" size="18px" />
              {{ $t('제품 생산수량') }}
            </div>
            <cascader
              :model-value="parameterIdToTreeIdMap.get(config.calculatedProductionCountParameterId)"
              @update:model-value="(treeId) => onEquipmentParamChanged('calculatedProductionCountParameterId', treeId)"
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

        <!-- 계획대비 생산율 -->
        <div class="col-12 col-md-6">
          <div class="equipment-field">
            <div class="field-label q-mb-sm">
              <q-icon name="error" color="negative" class="q-mr-xs" size="18px" />
              {{ $t('계획대비 생산율') }}
            </div>
            <cascader
              :model-value="parameterIdToTreeIdMap.get(config.productionRateAgainstPlanParameterId)"
              @update:model-value="(treeId) => onEquipmentParamChanged('productionRateAgainstPlanParameterId', treeId)"
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
        {{ t('공정 파라미터 설정') }}
      </div>
      <div class="text-caption">{{ t('공정 상태에 따른 설정/현재값 파라미터를 선택해주세요') }}</div>
    </q-card-section>
    <q-card-section>
      <div class="text-subtitle2 q-mb-sm row items-center">
        <q-btn color="primary" icon="add" @click="addRow" class="q-ml-sm">
          {{ t('행 추가') }}
          <q-tooltip>{{ t('행 추가') }}</q-tooltip>
        </q-btn>

        <div class="q-ml-lg row items-center">
          <label class="q-mr-sm text-body2">{{ t('분할 표시 개수') }}:</label>
          <q-input
            v-model.number="config.columnsPerTable"
            type="number"
            dense
            outlined
            style="width: 80px"
            :min="1"
            :rules="[$rules.natural, $rules.required]"
          />
        </div>
      </div>

      <!-- 테이블 형태로 표시 -->
      <div class="table-preview">
        <q-table
          :rows="config.processValues"
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
              <q-td
                v-for="col in props.cols"
                :key="col.name"
                :props="props"
                :class="col.name === 'order' ? '' : 'cursor-pointer cell-editable'"
                @click="col.name !== 'order' ? openCellEditModal(props.rowIndex, col.name) : null"
              >
                <div class="cell-content">
                  {{ getCellDisplayValue(props.rowIndex, col.name) }}
                  <q-icon
                    v-if="col.name !== 'order'"
                    name="edit"
                    size="14px"
                    class="cell-edit-icon q-ml-xs"
                    color="primary"
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

  <q-dialog v-model="cellEditModal.show" persistent>
    <q-card style="min-width: 500px">
      <q-card-section class="row items-center q-pb-none">
        <div class="text-h6">{{ t('셀 값 설정') }}</div>
        <q-space />
        <q-btn icon="close" flat round dense v-close-popup />
      </q-card-section>
      <q-card-section class="q-pt-md">
        <div class="text-subtitle2 q-mb-md">
          {{ t('위치') }}: {{ t('행') }} {{ cellEditModal.rowIndex + 1 }},
          {{ getColumnLabel(cellEditModal.columnName) }}
        </div>

        <!-- 값 종류 선택 -->
        <q-select
          v-model="cellEditModal.data.type"
          :label="$t('값 종류')"
          outlined
          :options="VALUE_TYPE_OPTIONS"
          option-value="value"
          option-label="label"
          emit-value
          map-options
          :rules="[$rules.required]"
          @update:model-value="onCellValueTypeChanged"
          class="q-mb-md"
        />

        <!-- 값 강조색 -->
        <q-input v-model="cellEditModal.data.highlightColor" :label="$t('값 강조색')" outlined class="q-mb-md" />

        <!-- 파라미터 선택 (값 종류가 parameter일 때) -->
        <div v-if="cellEditModal.data.type === 'parameter'" class="q-mb-md">
          <cascader
            :model-value="parameterIdToTreeIdMap.get(cellEditModal.data.parameterId)"
            @update:model-value="onCellParamChanged"
            class="input-required"
            :label="$t('설비/파라미터')"
            :titles="[$t('설비'), $t('파라미터')]"
            :items="parameterToolTree"
            item-value="treeId"
            item-text="name"
            clearable
            :valid-depth="2"
            :rules="[$rules.required]"
          />
        </div>

        <!-- 직접 값 입력 (값 종류가 custom일 때) -->
        <div v-if="cellEditModal.data.type === 'custom'" class="q-mb-md">
          <q-input
            v-model="cellEditModal.data.customValue"
            :label="$t('직접 입력 값')"
            outlined
            :rules="[$rules.required]"
          />
        </div>
      </q-card-section>

      <q-card-actions align="right" class="q-pa-md">
        <q-btn flat :label="$t('취소')" v-close-popup />
        <q-btn color="primary" :label="$t('저장')" @click="saveCellValue" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useQuasar } from 'quasar'
import { useWidgetSettingConfig, widgetSettingProps } from 'src/common/module/widgetCommon'
import ParameterService from 'src/services/modeling/ParameterService'
import Cascader from 'components/cascader/Cascader.vue'
import { pt } from 'src/plugins/i18n'

defineProps(widgetSettingProps)

const { t } = useI18n()
const $q = useQuasar()

// 값 종류 옵션
const VALUE_TYPE_OPTIONS = [
  { label: t('파라미터 선택'), value: 'parameter' },
  { label: t('직접 값 입력'), value: 'custom' },
]

const config = useWidgetSettingConfig({
  dataInterval: 30, // 기본값 30초
  // 설비 정보 파라미터 ID들
  statusParameterId: null, // 설비 상태
  productionCountParameterId: null, // 설비 카운트
  cycleTimeParameterId: null, // 사이클 타임
  processNameParameterId: null, // 진행 공정
  processErrorMessageParameterId: null, // 에러
  connectionParameterId: null, // 설비 연결 상태
  productIdParameterId: null, // 제품 ID
  cavityParameterId: null, // 캐비티
  calculatedProductionCountParameterId: null, // 총 제품 생산수량
  productionRateAgainstPlanParameterId: null, // 계획대비 생산율
  averageProductionCountParameterId: null, // 평균생산수량
  processValues: [], // 테이블 행 데이터 배열 - 각 행은 3개 셀(공정명, 설정값, 현재값) 데이터의 배열
  columnsPerTable: 10, // 분할 표시 개수 기본값 10
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

// 고정된 테이블 컬럼 정의 (순서, 공정명, 설정값, 현재값)
const fixedColumns = [
  { name: 'order', label: t('순서'), field: 'order', align: 'center' },
  { name: 'processName', label: t('공정명'), field: 'processName', align: 'left' },
  { name: 'settingValue', label: t('설정값'), field: 'settingValue', align: 'center' },
  { name: 'currentValue', label: t('현재값'), field: 'currentValue', align: 'center' },
]

// 컬럼 너비 가져오기
const getColumnWidth = () => {
  // 모든 컬럼을 동일한 너비로 자동 설정
  return 'auto'
}

// 컬럼 라벨 가져오기
const getColumnLabel = (columnName) => {
  const column = fixedColumns.find((col) => col.name === columnName)
  return column ? column.label : ''
}

// 셀 표시 값 가져오기
const getCellDisplayValue = (rowIndex, columnName) => {
  // 순서 컬럼인 경우 1부터 시작하는 순서 번호 반환
  if (columnName === 'order') {
    return rowIndex + 1
  }

  const row = config.value.processValues[rowIndex]
  const emptyCellValue = t('비어있음')

  if (!row) {
    return emptyCellValue
  }

  const cellData = row[columnName]
  if (!cellData) {
    return emptyCellValue
  }

  if (cellData.type === 'custom') {
    return cellData.customValue || emptyCellValue
  }

  // custom이 아니면, parameter 타입임
  const parameter = flattenTree.value.find((v) => v.id === cellData.parameterId)
  if (!parameter) {
    return emptyCellValue
  }
  const tool = flattenTree.value.find((v) => v.type === 'TOOL' && v.id === parameter.parent)
  return `${tool.name} / ${parameter.name}`
}

// 행 추가
const addRow = () => {
  const newRow = {
    processName: {
      type: null,
      highlightColor: null,
      parameterId: null,
      customValue: null,
    },
    settingValue: {
      type: null,
      highlightColor: null,
      parameterId: null,
      customValue: null,
    },
    currentValue: {
      type: null,
      highlightColor: null,
      parameterId: null,
      customValue: null,
    },
  }

  config.value.processValues = [...config.value.processValues, newRow]
}

// 행 제거
const removeRow = (rowIndex) => {
  config.value.processValues.splice(rowIndex, 1)
  config.value.processValues = [...config.value.processValues]
}

// 셀 편집 모달 관련
const cellEditModal = ref({
  show: false,
  rowIndex: null,
  columnName: null,
  data: {
    type: null,
    highlightColor: null,
    parameterId: null,
    customValue: null,
  },
})

// 셀 편집 모달 열기
const openCellEditModal = (rowIndex, columnName) => {
  const row = config.value.processValues[rowIndex]
  const cellData = row ? row[columnName] : null

  cellEditModal.value = {
    show: true,
    rowIndex,
    columnName,
    data: {
      type: cellData?.type || 'parameter',
      highlightColor: cellData?.highlightColor || null,
      parameterId: cellData?.parameterId || null,
      customValue: cellData?.customValue || null,
    },
  }
}

// 셀 값 저장
const saveCellValue = () => {
  const { rowIndex, columnName, data } = cellEditModal.value

  // 파라미터 타입인데 파라미터가 선택되지 않은 경우 경고
  if (data.type === 'parameter' && !data.parameterId) {
    $q.notify({
      type: 'warning',
      message: t('파라미터를 선택해 주세요.'),
      position: 'top',
    })
    return
  }

  const row = config.value.processValues[rowIndex]

  if (!row) {
    return
  }

  row[columnName] = {
    type: data.type,
    highlightColor: data.highlightColor,
    parameterId: data.parameterId,
    customValue: data.customValue,
  }

  // 파라미터 ID 맵 업데이트
  if (data.type === 'parameter' && data.parameterId) {
    parameterIdToTreeIdMap.set(data.parameterId, data.parameterId)
  }

  cellEditModal.value.show = false
}

// 셀 값 종류 변경 시 호출
const onCellValueTypeChanged = () => {
  const { data } = cellEditModal.value
  if (data.type === 'parameter') {
    data.customValue = null
  } else if (data.type === 'custom') {
    if (data.parameterId) {
      parameterIdToTreeIdMap.delete(data.parameterId)
    }
    data.parameterId = null
  }
}

// 셀 파라미터 변경 시 호출
const onCellParamChanged = (treeId) => {
  const found = flattenTree.value.find((v) => v.treeId === treeId)
  if (found?.type.toUpperCase() !== 'PARAMETER') {
    return
  }
  parameterIdToTreeIdMap.set(found.id, treeId)
  cellEditModal.value.data.parameterId = found.id
}

// 설비 정보 파라미터 변경 시 호출
const onEquipmentParamChanged = (paramName, treeId) => {
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

  const movedRow = config.value.processValues[fromIndex]
  config.value.processValues.splice(fromIndex, 1)
  config.value.processValues.splice(toIndex, 0, movedRow)

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
    'productionCountParameterId',
    'cycleTimeParameterId',
    'processNameParameterId',
    'processErrorMessageParameterId',
    'connectionParameterId',
    'productIdParameterId',
    'cavityParameterId',
    'averageProductionCountParameterId',
    'calculatedProductionCountParameterId',
    'productionRateAgainstPlanParameterId',
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
  config.value.processValues.forEach((row) => {
    Object.values(row).forEach((cellData) => {
      if (cellData?.type === 'parameter' && cellData?.parameterId) {
        const found = flattenTree.value.find((v) => v.id === cellData.parameterId)
        if (found?.treeId) {
          parameterIdToTreeIdMap.set(cellData.parameterId, found.treeId)
        }
      }
    })
  })
})
</script>

<style scoped>
.table-preview {
  overflow-x: auto;
}

.cell-editable {
  transition: background-color 0.2s;
}

.cell-editable:hover {
  background-color: rgba(0, 0, 0, 0.04);
}

.cell-content {
  display: flex;
  align-items: center;
}

.cell-edit-icon {
  transition: transform 0.2s;
}

.cell-edit-icon:hover {
  transform: scale(1.1);
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
