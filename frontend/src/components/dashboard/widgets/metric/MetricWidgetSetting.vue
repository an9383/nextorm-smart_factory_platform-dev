<template>
  <!-- 데이터 수집 기준 시간 설정 -->
  <q-card flat bordered class="q-mt-md">
    <q-card-section class="bg-primary text-white">
      <div class="text-subtitle2">
        <q-icon name="refresh" color="white" class="q-mr-xs" size="20px" />
        {{ $t('데이터 수집 기준') }}
      </div>
      <div class="text-caption">{{ $t('데이터 수집 주기를 선택해 주세요.') }}</div>
    </q-card-section>
    <q-card-section class="row items-center q-my-sm">
      <q-input
        v-model.number="config.dataInterval"
        type="number"
        dense
        outlined
        style="width: 120px"
        :rules="[$rules.natural, $rules.required]"
      />
      <div class="q-ml-sm">{{ $t('초') }}</div>
    </q-card-section>
  </q-card>

  <!-- 색상 임계값 설정 -->
  <q-card flat bordered class="q-mt-md">
    <q-card-section class="bg-secondary text-white">
      <div class="text-subtitle2">
        <q-icon name="palette" color="white" class="q-mr-xs" size="20px" />
        {{ $t('색상 임계값 설정') }}
      </div>
      <div class="text-caption">
        {{ $t('메트릭 수치에 따라 색상을 구분하는 임계값을 설정합니다. (단위: %)') }}
      </div>
    </q-card-section>
    <q-card-section>
      <div class="row q-col-gutter-md">
        <div class="col-12 col-md-6">
          <div class="threshold-field">
            <div class="field-label q-mb-sm">
              <q-icon name="circle" color="red" class="q-mr-xs" size="16px" />
              {{ $t('빨강 임계값') }}
            </div>
            <q-input
              v-model.number="config.colorThresholds.redThreshold"
              type="number"
              dense
              outlined
              :rules="[$rules.required]"
            />
            <div class="text-caption text-grey-7 q-mt-xs">{{ $t('이 값 이하면 빨강색으로 표시') }}</div>
          </div>
        </div>
        <div class="col-12 col-md-6">
          <div class="threshold-field">
            <div class="field-label q-mb-sm">
              <q-icon name="circle" color="orange" class="q-mr-xs" size="16px" />
              {{ $t('주황 임계값') }}
            </div>
            <q-input
              v-model.number="config.colorThresholds.orangeThreshold"
              type="number"
              dense
              outlined
              :rules="[$rules.required]"
            />
            <div class="text-caption text-grey-7 q-mt-xs">{{ $t('이 값 이하면 주황색으로 표시') }}</div>
          </div>
        </div>
      </div>
      <div class="q-mt-md text-caption text-grey-7">
        <q-icon name="info" size="16px" class="q-mr-xs" />
        {{ $t('주황 임계값보다 큰 값은 녹색으로 표시됩니다.') }}
      </div>
    </q-card-section>
  </q-card>

  <!-- 표시 설정 -->
  <q-card flat bordered class="q-mt-md">
    <q-card-section class="bg-info text-white">
      <div class="text-subtitle2">
        <q-icon name="view_module" color="white" class="q-mr-xs" size="20px" />
        {{ $t('표시 설정') }}
      </div>
      <div class="text-caption">{{ $t('위젯의 표시 옵션을 설정합니다.') }}</div>
    </q-card-section>
    <q-card-section>
      <div class="row items-center q-mb-md">
        <label class="q-mr-sm text-body2" style="min-width: 120px">{{ $t('한 줄에 표시할 개수') }}:</label>
        <q-input
          v-model.number="config.itemsPerRow"
          type="number"
          dense
          outlined
          style="width: 120px"
          :min="1"
          :max="24"
          :rules="[$rules.natural, $rules.required]"
        />
      </div>
      <div class="row items-center q-mb-md">
        <label class="q-mr-sm text-body2" style="min-width: 120px">{{ $t('라벨 글씨 크기') }}:</label>
        <q-input
          v-model.number="config.labelFontSize"
          type="number"
          dense
          outlined
          style="width: 100px"
          :min="8"
          :max="32"
          :rules="[$rules.natural, $rules.required]"
        />
        <span class="q-ml-sm text-body2">px</span>
      </div>
      <div class="row items-center">
        <label class="q-mr-sm text-body2" style="min-width: 120px">{{ $t('메트릭 글씨 크기') }}:</label>
        <q-input
          v-model.number="config.valueFontSize"
          type="number"
          dense
          outlined
          style="width: 100px"
          :min="10"
          :max="48"
          :rules="[$rules.natural, $rules.required]"
        />
        <span class="q-ml-sm text-body2">px</span>
      </div>
    </q-card-section>
  </q-card>

  <!-- 메트릭 테이블 설정 -->
  <q-card flat bordered class="q-mt-md">
    <q-card-section class="bg-accent text-white">
      <div class="text-subtitle2">
        <q-icon name="table_chart" color="white" class="q-mr-xs" size="20px" />
        {{ $t('메트릭 설정') }}
      </div>
      <div class="text-caption">{{ $t('표시할 메트릭 정보를 설정합니다.') }}</div>
    </q-card-section>
    <q-card-section>
      <div class="q-mb-md">
        <q-btn color="primary" icon="add" @click="addMetric">
          {{ $t('메트릭 추가') }}
        </q-btn>
      </div>

      <!-- 메트릭 테이블 -->
      <div class="table-preview">
        <q-table
          :rows="config.metrics"
          :columns="columns"
          row-key="index"
          flat
          bordered
          dense
          :pagination="{ rowsPerPage: 0 }"
          hide-pagination
          virtual-scroll-slice-size="0"
        >
          <!-- 헤더 -->
          <template #header="props">
            <q-tr :props="props">
              <q-th class="bg-grey-2" style="width: 40px">
                <q-icon name="drag_indicator" color="grey-6" size="18px" />
              </q-th>
              <q-th v-for="col in props.cols" :key="col.name" :props="props" class="bg-grey-2">
                {{ col.label }}
              </q-th>
              <q-th class="bg-grey-2" style="width: 60px">
                {{ $t('삭제') }}
              </q-th>
            </q-tr>
          </template>

          <!-- 바디 -->
          <template #body="props">
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
                <q-icon name="drag_indicator" color="grey-6" size="18px" class="cursor-grab" />
              </q-td>

              <!-- 순서 -->
              <q-td>{{ props.rowIndex + 1 }}</q-td>

              <!-- 라벨 -->
              <q-td>
                <q-input v-model="props.row.label" dense outlined :placeholder="$t('라벨 입력')" />
              </q-td>

              <!-- 메트릭 파라미터 -->
              <q-td>
                <div class="cascader-wrapper">
                  <cascader
                    :model-value="parameterIdToTreeIdMap.get(props.row.metricParameterId)"
                    @update:model-value="(treeId) => onMetricParamChanged(props.rowIndex, treeId)"
                    :titles="[$t('설비'), $t('파라미터')]"
                    :items="parameterToolTree"
                    item-value="treeId"
                    item-text="name"
                    clearable
                    :valid-depth="2"
                    dense
                  />
                  <q-tooltip v-if="props.row.metricParameterId" :delay="300">
                    {{ getParameterDisplayName(props.row.metricParameterId) }}
                  </q-tooltip>
                </div>
              </q-td>

              <!-- 가동상태 파라미터 -->
              <q-td>
                <div class="cascader-wrapper">
                  <cascader
                    :model-value="parameterIdToTreeIdMap.get(props.row.operatingStatusParameterId)"
                    @update:model-value="(treeId) => onOperatingStatusParamChanged(props.rowIndex, treeId)"
                    :titles="[$t('설비'), $t('파라미터')]"
                    :items="parameterToolTree"
                    item-value="treeId"
                    item-text="name"
                    clearable
                    :valid-depth="2"
                    dense
                  />
                  <q-tooltip v-if="props.row.operatingStatusParameterId" :delay="300">
                    {{ getParameterDisplayName(props.row.operatingStatusParameterId) }}
                  </q-tooltip>
                </div>
              </q-td>

              <!-- 삭제 버튼 -->
              <q-td>
                <q-btn flat round dense color="negative" icon="delete" size="sm" @click="removeMetric(props.rowIndex)">
                  <q-tooltip>{{ $t('삭제') }}</q-tooltip>
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

const config = useWidgetSettingConfig({
  dataInterval: 30, // 기본값 30초
  colorThresholds: {
    redThreshold: 50, // 빨강 임계값 (50% 이하)
    orangeThreshold: 80, // 주황 임계값 (80% 이하)
  },
  itemsPerRow: 12, // 한 줄에 표시할 메트릭 개수 (기본값 12)
  labelFontSize: 12, // 라벨 글씨 크기 (기본값 12px)
  valueFontSize: 16, // 메트릭 값 글씨 크기 (기본값 16px)
  metrics: [], // 메트릭 배열
})

const parameterToolTree = ref([])
const parameterIdToTreeIdMap = reactive(new Map())
const dragOverIndex = ref(null)

// 테이블 컬럼 정의
const columns = [
  { name: 'order', label: t('순서'), field: 'order', align: 'center', style: 'width: 60px' },
  { name: 'label', label: t('라벨'), field: 'label', align: 'center', style: 'width: 150px' },
  { name: 'metric', label: t('메트릭'), field: 'metric', align: 'center', style: 'width: 250px' },
  {
    name: 'operatingStatus',
    label: t('가동상태'),
    field: 'operatingStatus',
    align: 'center',
    style: 'width: 250px',
  },
]

const flattenTree = computed(() => {
  const flatTree = (items) => {
    return items.flatMap((item) => [item, ...(item.children ? flatTree(item.children) : [])])
  }
  return parameterToolTree.value.flatMap((item) => [item, ...(item.children ? flatTree(item.children) : [])])
})

// 파라미터 트리 로드
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

// 메트릭 추가
const addMetric = () => {
  const newMetric = {
    label: '',
    metricParameterId: null,
    operatingStatusParameterId: null,
  }
  config.value.metrics = [...config.value.metrics, newMetric]
}

// 메트릭 제거
const removeMetric = (index) => {
  const metric = config.value.metrics[index]

  // 파라미터 ID 맵에서 제거
  if (metric.metricParameterId) {
    parameterIdToTreeIdMap.delete(metric.metricParameterId)
  }
  if (metric.operatingStatusParameterId) {
    parameterIdToTreeIdMap.delete(metric.operatingStatusParameterId)
  }

  config.value.metrics.splice(index, 1)
  config.value.metrics = [...config.value.metrics]
}

// 메트릭 파라미터 변경
const onMetricParamChanged = (index, treeId) => {
  const found = flattenTree.value.find((v) => v.treeId === treeId)
  if (found?.type.toUpperCase() !== 'PARAMETER') {
    return
  }
  parameterIdToTreeIdMap.set(found.id, treeId)
  config.value.metrics[index].metricParameterId = found.id
}

// 가동상태 파라미터 변경
const onOperatingStatusParamChanged = (index, treeId) => {
  const found = flattenTree.value.find((v) => v.treeId === treeId)
  if (found?.type.toUpperCase() !== 'PARAMETER') {
    return
  }
  parameterIdToTreeIdMap.set(found.id, treeId)
  config.value.metrics[index].operatingStatusParameterId = found.id
}

// 파라미터 표시 이름 가져오기
const getParameterDisplayName = (parameterId) => {
  if (!parameterId) return ''
  const parameter = flattenTree.value.find((v) => v.id === parameterId)
  if (!parameter) return ''
  const tool = flattenTree.value.find((v) => v.type === 'TOOL' && v.id === parameter.parent)
  return tool ? `${tool.name} / ${parameter.name}` : parameter.name
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

  const fromIndex = parseInt(event.dataTransfer.getData('text/plain'))
  const toIndex = rowIndex

  if (fromIndex === toIndex) {
    return
  }

  const movedMetric = config.value.metrics[fromIndex]
  config.value.metrics.splice(fromIndex, 1)
  config.value.metrics.splice(toIndex, 0, movedMetric)
  config.value.metrics = [...config.value.metrics]

  dragOverIndex.value = null
}

onMounted(async () => {
  await loadTreeOfToolsAndParameters()

  // 기존 메트릭의 파라미터 매핑 복원
  config.value.metrics.forEach((metric) => {
    if (metric.metricParameterId) {
      const found = flattenTree.value.find((v) => v.id === metric.metricParameterId)
      if (found?.treeId) {
        parameterIdToTreeIdMap.set(metric.metricParameterId, found.treeId)
      }
    }
    if (metric.operatingStatusParameterId) {
      const found = flattenTree.value.find((v) => v.id === metric.operatingStatusParameterId)
      if (found?.treeId) {
        parameterIdToTreeIdMap.set(metric.operatingStatusParameterId, found.treeId)
      }
    }
  })
})
</script>

<style scoped>
.threshold-field {
  margin-bottom: 0.5rem;
}

.field-label {
  font-weight: 500;
  font-size: 0.875rem;
}

.table-preview {
  overflow: visible;
}

.table-preview :deep(.q-table__container) {
  overflow: visible;
}

.table-preview :deep(.q-table__middle) {
  overflow: visible;
}

.cascader-wrapper {
  position: relative;
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
