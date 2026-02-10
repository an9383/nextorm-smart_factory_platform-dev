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

  <!-- 테이블 컬럼 설정 -->
  <q-card flat bordered class="q-mt-md">
    <q-card-section class="bg-secondary text-white">
      <div class="text-subtitle2">
        <q-icon name="view_column" color="white" class="q-mr-xs" size="20px" />
        {{ t('테이블 컬럼 설정') }}
      </div>
      <div class="text-caption">{{ t('테이블에 표시할 컬럼을 설정해 주세요.') }}</div>
    </q-card-section>
    <q-card-section>
      <div class="text-subtitle2 q-mb-sm row items-center">
        {{ t('컬럼 목록') }}
        <q-btn flat round dense color="primary" icon="add" @click="addColumn" class="q-ml-sm" />
        <q-space />
        <q-btn
          flat
          dense
          color="grey-7"
          :icon="allColumnsExpanded ? 'unfold_less' : 'unfold_more'"
          @click="toggleAllColumns"
          class="q-ml-sm"
        >
          <q-tooltip>{{ allColumnsExpanded ? t('모두 접기') : t('모두 펼치기') }}</q-tooltip>
        </q-btn>
      </div>

      <div v-for="(column, index) in config.columns" :key="`column-${index}`" class="column-set q-mb-md">
        <!-- 컬럼 헤더 -->
        <div class="column-header row items-center q-mb-md cursor-pointer" @click="toggleColumn(index)">
          <div class="col-auto">
            <q-icon :name="columnExpanded[index] ? 'expand_more' : 'chevron_right'" size="24px" class="text-grey-7" />
          </div>
          <div class="col-auto q-ml-sm">
            <div class="text-h6 text-primary">#{{ index + 1 }}</div>
          </div>
          <div class="col">
            <div class="text-subtitle2 text-grey-8">{{ column.name || t('컬럼') }}</div>
          </div>
          <div class="col-auto">
            <q-btn
              flat
              round
              dense
              color="negative"
              icon="close"
              @click.stop="removeColumn(index)"
              :disable="config.columns.length === 1"
            >
              <q-tooltip>{{ t('컬럼 삭제') }}</q-tooltip>
            </q-btn>
          </div>
        </div>

        <!-- 컬럼 내용 영역 -->
        <div v-show="columnExpanded[index]">
          <!-- 기본 설정 -->
          <div class="column-section q-mb-md">
            <div class="section-title q-mb-lg">
              <q-icon name="settings" size="18px" class="q-mr-xs" />
              {{ t('기본 설정') }}
            </div>
            <div class="row q-col-gutter-sm">
              <div class="col-8">
                <q-input v-model="column.name" :label="$t('컬럼명')" dense outlined :rules="[$rules.required]" />
              </div>
              <div class="col-4">
                <q-select
                  v-model="column.align"
                  :label="$t('정렬')"
                  dense
                  outlined
                  :options="alignOptions"
                  option-value="value"
                  option-label="label"
                  emit-value
                  map-options
                />
              </div>
            </div>
          </div>

          <!-- 값 변환 설정 -->
          <div class="column-section q-mb-md">
            <div class="section-title q-mb-lg">
              <q-icon name="transform" size="18px" class="q-mr-xs" />
              {{ t('값 변환 함수') }}
            </div>
            <q-select
              v-model="column.valueTransformer"
              :label="$t('변환 함수 선택')"
              dense
              outlined
              :options="availableTransformers"
              option-value="value"
              option-label="label"
              emit-value
              map-options
              clearable
            >
              <template v-slot:option="scope">
                <q-item v-bind="scope.itemProps">
                  <q-item-section>
                    <q-item-label>{{ scope.opt.label }}</q-item-label>
                    <q-item-label caption>{{ scope.opt.description }}</q-item-label>
                  </q-item-section>
                </q-item>
              </template>
            </q-select>
          </div>

          <!-- 스타일 규칙 섹션 -->
          <div class="column-section">
            <div class="section-title q-mb-lg">
              <q-icon name="palette" size="18px" class="q-mr-xs" />
              {{ t('값에 따른 스타일 규칙') }}
              <q-btn
                flat
                dense
                size="sm"
                color="primary"
                icon="add"
                :label="$t('규칙 추가')"
                @click="openStyleRuleModal(index)"
                class="q-ml-sm"
              />
            </div>

            <div v-if="column.styleRules && column.styleRules.length > 0" class="style-rules-list">
              <div
                v-for="(rule, ruleIndex) in column.styleRules"
                :key="`rule-${ruleIndex}`"
                class="style-rule-item q-pa-sm q-mb-xs"
              >
                <div class="row items-center no-wrap">
                  <div class="col-auto rule-condition">
                    <span class="text-weight-medium">{{ t('값') }} {{ getOperatorLabel(rule.operator) }}</span>
                    <span v-if="typeof rule.value === 'number'" class="text-primary q-ml-xs">{{ rule.value }}</span>
                    <span v-else class="text-secondary q-ml-xs">"{{ rule.value }}"</span>
                  </div>
                  <div class="col-auto q-mx-sm">
                    <q-icon name="arrow_forward" size="16px" color="grey-6" />
                  </div>
                  <div class="col rule-style">
                    <span
                      v-if="rule.backgroundColor"
                      class="style-preview q-pa-xs"
                      :style="{
                        backgroundColor: rule.backgroundColor,
                        color: rule.textColor || '#000',
                        fontWeight: rule.fontWeight || 'normal',
                      }"
                    >
                      {{ t('미리보기') }}
                    </span>
                    <span v-if="rule.textColor && !rule.backgroundColor" class="q-ml-xs">
                      {{ t('글자색') }}: <span :style="{ color: rule.textColor }">●</span>
                    </span>
                  </div>
                  <div class="col-auto">
                    <q-btn
                      flat
                      round
                      dense
                      size="sm"
                      color="primary"
                      icon="edit"
                      @click="openStyleRuleModal(index, ruleIndex)"
                    >
                      <q-tooltip>{{ t('수정') }}</q-tooltip>
                    </q-btn>
                    <q-btn
                      flat
                      round
                      dense
                      size="sm"
                      color="negative"
                      icon="delete"
                      @click="removeStyleRule(index, ruleIndex)"
                    >
                      <q-tooltip>{{ t('삭제') }}</q-tooltip>
                    </q-btn>
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="text-caption text-grey-6 q-ml-sm">{{ t('설정된 규칙이 없습니다.') }}</div>
          </div>
        </div>
        <!-- 컬럼 내용 영역 끝 -->
      </div>
    </q-card-section>
  </q-card>

  <!-- 테이블 값 설정 -->
  <q-card flat bordered class="q-mt-md">
    <q-card-section class="bg-info text-white">
      <div class="text-subtitle2">
        <q-icon name="table_chart" color="white" class="q-mr-xs" size="20px" />
        {{ t('테이블 값 설정') }}
      </div>
      <div class="text-caption">{{ t('테이블에 표시할 값을 설정해 주세요.') }}</div>
    </q-card-section>
    <q-card-section>
      <div class="text-subtitle2 q-mb-sm row items-center">
        {{ t('테이블 미리보기') }}
        <q-btn flat round dense color="primary" icon="add" @click="addRow" class="q-ml-sm">
          <q-tooltip>{{ t('행 추가') }}</q-tooltip>
        </q-btn>
      </div>

      <!-- 테이블 형태로 표시 -->
      <div class="table-preview">
        <q-table
          :rows="config.tableRows"
          :columns="tableColumns"
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
            <q-tr :props="props">
              <q-td
                v-for="col in props.cols"
                :key="col.name"
                :props="props"
                class="cursor-pointer cell-editable"
                @click="openCellEditModal(props.rowIndex, col.name)"
              >
                <div class="cell-content">
                  {{ getCellDisplayValue(props.rowIndex, col.name) }}
                  <q-icon name="edit" size="14px" class="cell-edit-icon q-ml-xs" color="primary" />
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

  <!-- 스타일 규칙 편집 모달 -->
  <q-dialog v-model="styleRuleModal.show" persistent>
    <q-card style="min-width: 500px">
      <q-card-section class="row items-center q-pb-none">
        <div class="text-h6">
          {{ styleRuleModal.editingIndex !== null ? t('스타일 규칙 수정') : t('스타일 규칙 추가') }}
        </div>
        <q-space />
        <q-btn icon="close" flat round dense v-close-popup />
      </q-card-section>

      <q-card-section>
        <div class="text-subtitle2 q-mb-md">
          {{ t('컬럼') }}: {{ config.columns[styleRuleModal.columnIndex]?.name }}
        </div>

        <!-- 조건 설정 -->
        <div class="row q-col-gutter-sm q-mb-md">
          <div class="col-12">
            <div class="text-caption text-grey-7 q-mb-xs">{{ t('조건') }}</div>
          </div>
          <div class="col-4">
            <q-select
              v-model="styleRuleModal.data.operator"
              :label="$t('연산자')"
              outlined
              dense
              :options="conditionOperators"
              option-value="value"
              option-label="label"
              emit-value
              map-options
              :rules="[$rules.required]"
              @update:model-value="onStyleRuleOperatorChanged"
            />
          </div>
          <div class="col-8">
            <q-input
              v-if="isNumericOnlyOperator"
              v-model.number="styleRuleModal.data.value"
              :label="$t('비교 값 (숫자)')"
              type="number"
              outlined
              dense
              :rules="[$rules.required]"
            />
            <q-input
              v-else
              v-model="styleRuleModal.data.value"
              :label="$t('비교 값 (숫자 또는 문자)')"
              type="text"
              outlined
              dense
              :rules="[$rules.required]"
            />
          </div>
        </div>

        <!-- 스타일 설정 -->
        <div class="q-mb-md">
          <div class="text-caption text-grey-7 q-mb-xs">{{ t('스타일') }}</div>

          <!-- 배경색 -->
          <div class="row items-center q-mb-sm">
            <div class="col-3 text-caption">{{ t('배경색') }}</div>
            <div class="col-9">
              <q-input v-model="styleRuleModal.data.backgroundColor" outlined dense>
                <template v-slot:append>
                  <q-icon name="colorize" class="cursor-pointer">
                    <q-popup-proxy cover transition-show="scale" transition-hide="scale">
                      <q-color v-model="styleRuleModal.data.backgroundColor" />
                    </q-popup-proxy>
                  </q-icon>
                </template>
              </q-input>
            </div>
          </div>

          <!-- 글자색 -->
          <div class="row items-center q-mb-sm">
            <div class="col-3 text-caption">{{ t('글자색') }}</div>
            <div class="col-9">
              <q-input v-model="styleRuleModal.data.textColor" outlined dense>
                <template v-slot:append>
                  <q-icon name="colorize" class="cursor-pointer">
                    <q-popup-proxy cover transition-show="scale" transition-hide="scale">
                      <q-color v-model="styleRuleModal.data.textColor" />
                    </q-popup-proxy>
                  </q-icon>
                </template>
              </q-input>
            </div>
          </div>

          <!-- 글자 굵기 -->
          <div class="row items-center">
            <div class="col-3 text-caption">{{ t('글자 굵기') }}</div>
            <div class="col-9">
              <q-select
                v-model="styleRuleModal.data.fontWeight"
                outlined
                dense
                :options="[
                  { label: t('보통'), value: 'normal' },
                  { label: t('굵게'), value: 'bold' },
                ]"
                option-value="value"
                option-label="label"
                emit-value
                map-options
              />
            </div>
          </div>
        </div>

        <!-- 미리보기 -->
        <div class="q-pa-md bg-grey-2 text-center">
          <div class="text-caption text-grey-7 q-mb-xs">{{ t('미리보기') }}</div>
          <div
            class="q-pa-sm"
            :style="{
              backgroundColor: styleRuleModal.data.backgroundColor || 'transparent',
              color: styleRuleModal.data.textColor || '#000',
              fontWeight: styleRuleModal.data.fontWeight || 'normal',
            }"
          >
            {{ t('샘플 텍스트') }}
          </div>
        </div>
      </q-card-section>

      <q-card-actions align="right">
        <q-btn flat :label="$t('취소')" v-close-popup />
        <q-btn color="primary" :label="$t('저장')" @click="saveStyleRule" />
      </q-card-actions>
    </q-card>
  </q-dialog>

  <!-- 셀 편집 모달 -->
  <q-dialog v-model="cellEditModal.show" persistent>
    <q-card style="min-width: 500px">
      <q-card-section class="row items-center q-pb-none">
        <div class="text-h6">{{ t('셀 값 설정') }}</div>
        <q-space />
        <q-btn icon="close" flat round dense v-close-popup />
      </q-card-section>

      <q-card-section>
        <div class="text-subtitle2 q-mb-md">
          {{ t('위치') }}: {{ t('행') }} {{ cellEditModal.rowIndex + 1 }},
          {{ getColumnLabel(cellEditModal.columnName) }}
        </div>

        <!-- 값 종류 선택 -->
        <q-select
          v-model="cellEditModal.data.type"
          :label="$t('값 종류')"
          outlined
          :options="valueTypeOptions"
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

          <!-- 데이터 시간 표시 옵션 -->
          <q-checkbox v-model="cellEditModal.data.showTimestamp" :label="$t('데이터 시간 표시')" class="q-mt-md" />
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

      <q-card-actions align="right">
        <q-btn flat :label="$t('취소')" v-close-popup />
        <q-btn color="primary" :label="$t('저장')" @click="saveCellValue" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup>
import { ref, onMounted, computed, reactive } from 'vue'
import { useI18n } from 'vue-i18n'
import { useQuasar } from 'quasar'
import { widgetSettingProps, useWidgetSettingConfig } from 'src/common/module/widgetCommon'
import ParameterService from 'src/services/modeling/ParameterService'
import Cascader from 'components/cascader/Cascader.vue'
import { pt } from 'src/plugins/i18n'
import { useValueTransformers } from './useValueTransformers'

defineProps(widgetSettingProps)

const ENV_NAME = import.meta.env?.VITE_ENV_NAME

const { t } = useI18n()
const { availableTransformers } = useValueTransformers(ENV_NAME)
const $q = useQuasar()

const config = useWidgetSettingConfig({
  dataInterval: 30, // 기본값 30초
  columns: [{ name: '컬럼1', align: 'left', styleRules: [], valueTransformer: null }], // 테이블 컬럼 배열 (기본값 1개)
  tableRows: [], // 테이블 행 데이터 배열 - 각 행은 셀 데이터의 배열
})

const parameterToolTree = ref([])
const parameterIdToTreeIdMap = reactive(new Map()) // key: parameterId, value: treeId

// 컬럼 접기/펴기 상태
const columnExpanded = ref([])
const allColumnsExpanded = computed(() => {
  return columnExpanded.value.length > 0 && columnExpanded.value.every((expanded) => expanded)
})

// 정렬 옵션
const alignOptions = [
  { label: t('왼쪽'), value: 'left' },
  { label: t('가운데'), value: 'center' },
  { label: t('오른쪽'), value: 'right' },
]

// 값 종류 옵션
const valueTypeOptions = [
  { label: t('파라미터 선택'), value: 'parameter' },
  { label: t('직접 값 입력'), value: 'custom' },
]

// 조건 연산자 옵션
const conditionOperators = [
  { label: '> (숫자 전용)', value: 'gt', numericOnly: true },
  { label: '>= (숫자 전용)', value: 'gte', numericOnly: true },
  { label: '< (숫자 전용)', value: 'lt', numericOnly: true },
  { label: '<= (숫자 전용)', value: 'lte', numericOnly: true },
  { label: '= (숫자/문자)', value: 'eq', numericOnly: false },
  { label: '≠ (숫자/문자)', value: 'neq', numericOnly: false },
]

// 연산자가 숫자 전용인지 확인
const isNumericOnlyOperator = computed(() => {
  const operator = conditionOperators.find((op) => op.value === styleRuleModal.value.data.operator)
  return operator?.numericOnly !== false
})

// 스타일 규칙 편집 모달
const styleRuleModal = ref({
  show: false,
  columnIndex: null,
  editingIndex: null, // null이면 새로 추가, 숫자면 해당 인덱스 수정
  data: {
    operator: 'gt',
    value: null,
    backgroundColor: '',
    textColor: '',
    fontWeight: 'normal',
  },
})

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

// 테이블 컬럼 정의 (동적으로 생성)
const tableColumns = computed(() => {
  return config.value.columns.map((column, index) => ({
    name: `col_${index}`,
    label: column.name,
    field: `col_${index}`,
    align: column.align,
    sortable: false,
  }))
})

// 컬럼 너비 가져오기
const getColumnWidth = () => {
  // 모든 컬럼을 동일한 너비로 자동 설정
  return 'auto'
}

// 컬럼 라벨 가져오기
const getColumnLabel = (columnName) => {
  const columnIndex = parseInt(columnName.split('_')[1])
  const column = config.value.columns[columnIndex]
  return column ? column.name : ''
}

// 셀 표시 값 가져오기
const getCellDisplayValue = (rowIndex, columnName) => {
  const row = config.value.tableRows[rowIndex]
  if (!row) return t('비어있음')

  const columnIndex = parseInt(columnName.split('_')[1])
  const cellData = row[columnIndex]

  if (!cellData) return t('비어있음')

  if (cellData.type === 'custom') {
    return cellData.customValue || t('비어있음')
  } else if (cellData.type === 'parameter') {
    if (cellData.parameterId) {
      const found = flattenTree.value.find((v) => v.id === cellData.parameterId)
      return found ? found.name : t('파라미터')
    }
    return t('파라미터')
  }
  return t('비어있음')
}

// 행 추가
const addRow = () => {
  const newRow = []

  // 각 컬럼에 대해 빈 셀 데이터 생성
  config.value.columns.forEach(() => {
    newRow.push({
      type: null,
      highlightColor: null,
      parameterId: null,
      customValue: null,
    })
  })

  config.value.tableRows = [...config.value.tableRows, newRow]
}

// 행 제거
const removeRow = (rowIndex) => {
  config.value.tableRows.splice(rowIndex, 1)
  config.value.tableRows = [...config.value.tableRows]
}

// 컬럼 추가
const addColumn = () => {
  config.value.columns = [
    ...config.value.columns,
    { name: `컬럼${config.value.columns.length + 1}`, align: 'left', styleRules: [], valueTransformer: null },
  ]

  // 기존 행들에 새 컬럼 추가
  config.value.tableRows = config.value.tableRows.map((row) => [
    ...row,
    {
      type: null,
      highlightColor: null,
      parameterId: null,
      customValue: null,
    },
  ])

  // 새 컬럼은 펼친 상태로 추가
  columnExpanded.value.push(true)
}

// 컬럼 제거
const removeColumn = (index) => {
  if (config.value.columns.length === 1) return // 최소 1개는 유지

  // 기존 테이블 행에서 해당 컬럼 제거
  config.value.tableRows = config.value.tableRows.map((row) => {
    const newRow = [...row]
    newRow.splice(index, 1)
    return newRow
  })

  config.value.columns.splice(index, 1)
  config.value.columns = [...config.value.columns]

  // 컬럼 접기/펴기 상태도 제거
  columnExpanded.value.splice(index, 1)
}

// 컬럼 접기/펴기
const toggleColumn = (index) => {
  columnExpanded.value[index] = !columnExpanded.value[index]
}

const toggleAllColumns = () => {
  const newState = !allColumnsExpanded.value
  columnExpanded.value = columnExpanded.value.map(() => newState)
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
    showTimestamp: false,
  },
})

// 셀 편집 모달 열기
const openCellEditModal = (rowIndex, columnName) => {
  const columnIndex = parseInt(columnName.split('_')[1])
  const row = config.value.tableRows[rowIndex]
  const cellData = row ? row[columnIndex] : null

  cellEditModal.value = {
    show: true,
    rowIndex,
    columnName,
    data: {
      type: cellData?.type || 'parameter',
      highlightColor: cellData?.highlightColor || null,
      parameterId: cellData?.parameterId || null,
      customValue: cellData?.customValue || null,
      showTimestamp: cellData?.showTimestamp || false,
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

  const columnIndex = parseInt(columnName.split('_')[1])
  const row = config.value.tableRows[rowIndex]

  if (!row) {
    return
  }

  row[columnIndex] = {
    type: data.type,
    highlightColor: data.highlightColor,
    parameterId: data.parameterId,
    customValue: data.customValue,
    showTimestamp: data.showTimestamp,
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

// 스타일 규칙 추가 모달 열기
const openStyleRuleModal = (columnIndex, editingIndex = null) => {
  const column = config.value.columns[columnIndex]

  if (editingIndex !== null && column.styleRules[editingIndex]) {
    // 수정 모드
    const rule = column.styleRules[editingIndex]
    styleRuleModal.value = {
      show: true,
      columnIndex,
      editingIndex,
      data: { ...rule },
    }
  } else {
    // 추가 모드
    styleRuleModal.value = {
      show: true,
      columnIndex,
      editingIndex: null,
      data: {
        operator: 'gt',
        value: null,
        backgroundColor: '',
        textColor: '',
        fontWeight: 'normal',
      },
    }
  }
}

// 스타일 규칙 저장
const saveStyleRule = () => {
  const { columnIndex, editingIndex, data } = styleRuleModal.value

  // 값 유효성 검증
  if (data.value === null || data.value === '') {
    $q.notify({
      type: 'warning',
      message: t('비교할 값을 입력해 주세요.'),
      position: 'top',
    })
    return
  }

  const column = config.value.columns[columnIndex]

  if (!column.styleRules) {
    column.styleRules = []
  }

  if (editingIndex !== null) {
    // 수정
    column.styleRules[editingIndex] = { ...data }
  } else {
    // 추가
    column.styleRules.push({ ...data })
  }

  styleRuleModal.value.show = false
}

// 스타일 규칙 삭제
const removeStyleRule = (columnIndex, ruleIndex) => {
  const column = config.value.columns[columnIndex]
  if (column.styleRules) {
    column.styleRules.splice(ruleIndex, 1)
  }
}

// 연산자 라벨 가져오기
const getOperatorLabel = (operator) => {
  const found = conditionOperators.find((op) => op.value === operator)
  return found ? found.label : operator
}

// 스타일 규칙 연산자 변경 시 호출
const onStyleRuleOperatorChanged = (newOperator) => {
  const previousOperator = conditionOperators.find((op) => op.value === styleRuleModal.value.data.operator)
  const currentOperator = conditionOperators.find((op) => op.value === newOperator)

  // 숫자 전용 <-> 문자/숫자 전환 시 값 초기화
  if (previousOperator?.numericOnly !== currentOperator?.numericOnly) {
    styleRuleModal.value.data.value = null
  }
}

onMounted(async () => {
  await loadTreeOfToolsAndParameters()

  // 컬럼 접기/펴기 초기 상태 설정 (모두 펼친 상태)
  columnExpanded.value = config.value.columns.map(() => true)

  // 기존 컬럼에 styleRules, valueTransformer가 없으면 초기화
  config.value.columns.forEach((column) => {
    if (!column.styleRules) {
      column.styleRules = []
    }
    if (column.valueTransformer === undefined) {
      column.valueTransformer = null
    }
  })

  // 기존 테이블 행들의 파라미터 매핑 복원
  config.value.tableRows.forEach((row) => {
    row.forEach((cellData) => {
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
.column-set {
  position: relative;
  border: 1px dashed rgba(0, 0, 0, 0.12);
  border-radius: 4px;
  padding: 16px;
  margin-bottom: 8px;
  background-color: rgba(0, 0, 0, 0.02);
}

.value-set {
  position: relative;
  border: 1px dashed rgba(0, 0, 0, 0.12);
  border-radius: 4px;
  padding: 16px;
  margin-bottom: 8px;
  background-color: rgba(0, 0, 0, 0.02);
}

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

.style-rules-list {
  border-left: 3px solid #e0e0e0;
  padding-left: 12px;
}

.style-rule-item {
  background-color: #fafafa;
  border-radius: 6px;
  border: 1px solid #e0e0e0;
  transition: all 0.2s;
}

.style-rule-item:hover {
  background-color: #f5f5f5;
  border-color: #bdbdbd;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.style-preview {
  display: inline-block;
  border-radius: 4px;
  border: 1px solid rgba(0, 0, 0, 0.2);
  min-width: 60px;
  text-align: center;
  font-size: 12px;
}

/* 컬럼 헤더 스타일 */
.column-header {
  border-bottom: 2px solid #e0e0e0;
  padding-bottom: 8px;
}

/* 섹션 스타일 */
.column-section {
  padding: 12px;
  background-color: #fafafa;
  border-radius: 6px;
  border: 1px solid #e0e0e0;
}

.section-title {
  font-weight: 500;
  font-size: 14px;
  color: #424242;
  display: flex;
  align-items: center;
}

/* 규칙 조건 및 스타일 표시 */
.rule-condition {
  min-width: 150px;
  font-size: 13px;
}

.rule-style {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
}
</style>
