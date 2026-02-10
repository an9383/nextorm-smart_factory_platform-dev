<template>
  <q-dialog v-model="isShow" persistent>
    <q-card class="dialog-container">
      <q-card-section class="bg-secondary text-white">
        <div class="text-h6">{{ $t('설비에서 복사') }}</div>
      </q-card-section>
      <q-card-section class="contents">
        <q-card>
          <q-card-section class="q-pb-none">
            <div class="text-h6">{{ $t('기준 설비') }}</div>
          </q-card-section>
          <q-card-section>
            <div class="row q-bt">
              <div class="col-4 tool-tree">
                <div class="row q-pa-md top-search-wrap">
                  <q-input
                    v-model="sourceToolTree.search"
                    standout="bg-blue-grey-5"
                    :placeholder="$t('설비 검색')"
                    color="white"
                    dense
                    clearable
                    clear-icon="close"
                    class="col"
                    input-class="text-white"
                  />
                  <q-btn
                    flat
                    round
                    color="white"
                    @click="toggleExpandTree('source')"
                    :icon="sourceToolTree.isExpandAll ? 'mdi-collapse-all' : 'mdi-expand-all'"
                    class="col-auto q-ml-sm"
                  />
                </div>
                <q-tree
                  ref="sourceTree"
                  :nodes="sourceToolTree.items"
                  node-key="treeId"
                  label-key="name"
                  :filter="sourceToolTree.search"
                  class="selectable"
                  selected-color="primary"
                  no-selection-unset
                  v-model:selected="sourceToolTree.selected"
                  v-model:expanded="sourceToolTree.expanded"
                  @update:expanded="onTreeAction('sourceTree', 'expanded', $event)"
                  @update:selected="onTreeAction('sourceTree', 'selected', $event)"
                >
                  <template #default-header="prop">
                    <div class="row items-center full-width">
                      <q-icon
                        :name="getTreeIconByType(prop.node.type).icon"
                        :color="getTreeIconByType(prop.node.type).color"
                      />
                      <div>{{ prop.node.name }}</div>
                    </div>
                  </template>
                </q-tree>
              </div>
              <div class="col-8">
                <q-table
                  v-model:selected="sourceParameterGrid.selected"
                  :rows="sourceParameterGrid.items"
                  :columns="sourceParameterGrid.columns"
                  class="parameter-table"
                  table-header-class="text-weight-bold"
                  row-key="id"
                  selection="multiple"
                  @selection="checkSourceTable"
                  grid-header
                  dense
                  flat
                  v-model:pagination="pagination"
                  :rows-per-page-options="[0]"
                  :filter="searchText"
                  :filter-method="parameterFilterMethod"
                >
                  <template v-slot:top-left>
                    <q-input
                      v-model="searchText"
                      :placeholder="$t('파라미터명')"
                      dense
                      style="width: 300px; min-width: 300px"
                      outlined
                      clearable
                      class="search-input"
                    >
                      <template v-slot:append>
                        <q-icon name="search" />
                      </template>
                    </q-input>
                  </template>
                </q-table>
              </div>
            </div>
          </q-card-section>
        </q-card>
        <q-card class="q-mt-sm">
          <q-card-section class="q-pb-none">
            <div class="text-h6">{{ $t('목표 설비') }}</div>
          </q-card-section>
          <q-card-section>
            <div class="row q-bt">
              <div class="col-4 tool-tree">
                <div class="row q-pa-md top-search-wrap">
                  <q-input
                    v-model="targetToolTree.search"
                    standout="bg-blue-grey-5"
                    :placeholder="$t('설비 검색')"
                    color="white"
                    dense
                    clearable
                    clear-icon="close"
                    class="col"
                    input-class="text-white"
                  />
                  <q-btn
                    flat
                    round
                    color="white"
                    @click="toggleExpandTree('target')"
                    :icon="targetToolTree.isExpandAll ? 'mdi-collapse-all' : 'mdi-expand-all'"
                    class="col-auto q-ml-sm"
                  />
                </div>
                <q-tree
                  ref="targetTree"
                  :nodes="targetToolTree.items"
                  node-key="treeId"
                  label-key="name"
                  :filter="targetToolTree.search"
                  class="selectable cardHeight"
                  tick-strategy="leaf-filtered"
                  selected-color="primary"
                  no-transition
                  v-model:ticked="targetToolTree.ticked"
                  v-model:expanded="targetToolTree.expanded"
                  @update:expanded="onTreeAction('targetTree', 'expanded', $event)"
                  @update:ticked="onTreeAction('targetTree', 'selected', $event)"
                >
                  <template #default-header="prop">
                    <div class="row items-center full-width">
                      <q-icon
                        :name="getTreeIconByType(prop.node.type).icon"
                        :color="getTreeIconByType(prop.node.type).color"
                      />
                      <div>{{ prop.node.name }}</div>
                    </div>
                  </template>
                </q-tree>
              </div>
              <div class="col-8">
                <q-table
                  class="parameter-table"
                  v-model:selected="targetParameterGrid.selected"
                  :rows="targetParameterGrid.items"
                  :columns="targetParameterGrid.columns"
                  row-key="id"
                  flat
                  dense
                  hide-bottom
                  v-model:pagination="pagination"
                  :rows-per-page-options="[0]"
                />
              </div>
            </div>
          </q-card-section>
        </q-card>
      </q-card-section>
      <q-card-actions align="right">
        <q-btn flat color="negative" :label="$t('닫기')" @click="onCancel" />
        <q-btn flat color="primary" :label="$t('저장')" @click="onOk" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>
<script setup>
import { nextTick, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { pt } from 'src/plugins/i18n'
import useUI from 'src/common/module/ui'
import LocationService from 'src/services/modeling/LocationService.js'
import ParameterService from 'src/services/modeling/ParameterService.js'

const props = defineProps({
  copyToolId: Number,
})
const emit = defineEmits(['close'])
const ui = useUI()
const { t } = useI18n()
const isShow = ref(true)
const sourceTree = ref()
const targetTree = ref()

const sourceToolTree = reactive({
  isExpandAll: false,
  search: '',
  items: [],
  selected: [],
  expanded: [],
})
const sourceParameterGrid = ref({
  columns: [
    { name: 'name', align: 'left', label: t('파라미터명'), field: 'name', sortable: false },
    {
      name: 'displayName',
      align: 'left',
      label: t('화면 표시'),
      field: 'name',
      sortable: false,
      format: (val, row) => pt(row.name),
    },
    { name: 'dataType', align: 'left', label: t('데이터 타입'), field: 'dataType', sortable: false },
  ],
  items: [],
  selected: [],
  loading: false,
})
const targetParameterGrid = ref({
  columns: [
    { name: 'name', align: 'left', label: t('파라미터명'), field: 'name', sortable: false },
    {
      name: 'displayName',
      align: 'left',
      label: t('화면 표시'),
      field: 'name',
      sortable: false,
      format: (val, row) => pt(row.name),
    },
    { name: 'dataType', align: 'left', label: t('데이터 타입'), field: 'dataType', sortable: false },
  ],
  items: [],
  selected: [],
  loading: false,
})
const targetToolTree = reactive({
  isExpandAll: false,
  search: '',
  items: [],
  expanded: [],
  ticked: [],
})
const locations = ref([])

const searchText = ref('')

const parameterFilterMethod = (rows, terms) => {
  const q = (terms || '').toString().trim().toLowerCase()
  if (!q) return rows

  return rows.filter((row) => {
    const name = (row.name || '').toString().toLowerCase()
    // pt 변환 실패/빈값 대비
    const displayName = ((pt(row.name || '') || row.name || '') + '').toLowerCase()
    return name.includes(q) || displayName.includes(q)
  })
}

const loadOptions = async () => {
  ui.loading.show()
  locations.value = await LocationService.getLocationsTreeTypeUntil('LINE')
  ui.loading.hide()
}

const checkSourceTable = (evt) => {
  if (evt.rows.length > 1) {
    if (evt.added) {
      evt.rows.forEach((v) => targetParameterGrid.value.items.push(v))
    } else {
      targetParameterGrid.value.items = []
    }
  } else {
    if (evt.added) {
      targetParameterGrid.value.items.push(evt.rows[0])
    } else {
      targetParameterGrid.value.items = targetParameterGrid.value.items.filter((v) => v.id !== evt.rows[0].id)
    }
  }
}

const loadToolTree = async (lineId) => {
  ui.loading.show()
  const toolsTree = await LocationService.getLocationsAndToolsTree()
  ui.loading.hide()

  sourceToolTree.items = [...toolsTree]
  targetToolTree.items = [...toolsTree]

  lineId = props.copyToolId
  if (lineId) {
    sourceToolTree.selected = [findTreeItemByTreeId('source', `TOOL_${lineId}`)][0].treeId
    onTreeAction('sourceTree', 'selected', `TOOL_${lineId}`)
    if (!sourceToolTree.isExpandAll) {
      await nextTick()
      toggleExpandTree('source')
    }
  }
}

const findTreeItemByTreeId = (treeName, treeId) => {
  const expandTreeData = (items) => {
    const data = []
    items.forEach((item) => {
      data.push(item)
      if (item.children) {
        data.push(...expandTreeData(item.children))
      }
    })
    return data
  }
  if (treeName === 'source') {
    return expandTreeData(sourceToolTree.items).find((v) => v.treeId === treeId)
  } else {
    return expandTreeData(targetToolTree.items).find((v) => v.treeId === treeId)
  }
}

const toggleExpandTree = (treeType) => {
  if (treeType === 'source') {
    if (sourceToolTree.isExpandAll) {
      sourceTree.value.collapseAll()
      sourceToolTree.isExpandAll = !sourceToolTree.isExpandAll
    } else {
      sourceTree.value.expandAll()
      sourceToolTree.isExpandAll = !sourceToolTree.isExpandAll
    }
  } else {
    if (targetToolTree.isExpandAll) {
      targetTree.value.collapseAll()
      targetToolTree.isExpandAll = !targetToolTree.isExpandAll
    } else {
      targetTree.value.expandAll()
      targetToolTree.isExpandAll = !targetToolTree.isExpandAll
    }
  }
}

const onTreeAction = async (treeName, actionType, eventData) => {
  if (actionType === 'expanded') {
    if (treeName === 'sourceTree') {
      sourceToolTree.expanded = eventData
    } else {
      targetToolTree.expanded = eventData
    }
  } else {
    if (treeName === 'sourceTree') {
      sourceParameterGrid.value.selected = []
      if (sourceToolTree.selected === null) {
        sourceParameterGrid.value.items = []
      }
      if (targetToolTree.ticked.includes(sourceToolTree.selected)) {
        targetToolTree.ticked = targetToolTree.ticked.filter((v) => v !== sourceToolTree.selected)
      }
      await nextTick()
      const location = sourceTree.value.getNodeByKey(eventData)
      if (location) {
        if (location.type === 'TOOL') {
          const toolId = location.id
          sourceParameterGrid.value.items = await ParameterService.getParameters({ toolId, isVirtual: false })
          targetParameterGrid.value.items = []
        } else {
          ui.notify.warning(t('설비를 선택하세요.'))
          sourceParameterGrid.value.items = []
        }
      }
    } else {
      if (
        targetToolTree.ticked &&
        targetToolTree.ticked.some((v) => v === sourceToolTree.selected && targetToolTree.ticked.length !== 0)
      ) {
        targetToolTree.ticked = targetToolTree.ticked.filter((v) => v !== sourceToolTree.selected)
        ui.notify.warning(t('Source 설비와 같은 설비를 선택할 수 없습니다.'))
      }
    }
  }
}

const getTreeIconByType = (type) => {
  if (type === 'SITE') {
    return {
      icon: 'mdi-office-building',
      color: 'primary',
    }
  } else if (type === 'FAB') {
    return {
      icon: 'mdi-factory',
      color: 'secondary',
    }
  } else if (type === 'LINE') {
    return {
      icon: 'mdi-account-network',
      color: 'green',
    }
  } else {
    return {
      icon: 'mdi-tools',
      color: 'grey',
    }
  }
}

const validateData = () => {
  if (sourceToolTree.selected === null || sourceToolTree.selected.length === 0) {
    ui.notify.warning(t('Source 설비를 선택하세요.'))
    return false
  }
  if (targetToolTree.ticked === null || targetToolTree.ticked.length === 0) {
    ui.notify.warning(t('Target 설비를 선택하세요.'))
    return false
  }
  if (sourceParameterGrid.value.selected === null || sourceParameterGrid.value.selected.length === 0) {
    ui.notify.warning(t('복사할 파라미터를 선택하세요.'))
    return false
  }
  return true
}

const onOk = async () => {
  if (validateData()) {
    const selectedTargetToolIds = []
    targetToolTree.ticked.forEach((v) => selectedTargetToolIds.push(Number(v.replace('TOOL_', ''))))
    const param = { targetToolIds: selectedTargetToolIds, parameterCreateRequestDtos: targetParameterGrid.value.items }
    try {
      await ParameterService.copyParametersByToolIds(param)
      ui.notify.success(t('저장 되었습니다.'))
      isShow.value = false
      emit('close', 'ok')
    } catch (error) {
      if (error.response?.data.code === 'ERROR_DUPLICATION_PARAMETER_NAME') {
        ui.notify.error(t('파라미터명') + t('이(가) 중복 됩니다'))
      }
    }
  }
}

const onCancel = () => {
  isShow.value = false
  emit('close')
}

loadOptions()

loadToolTree()
</script>
<style lang="scss" scoped>
.dialog-container {
  width: 1200px;
  max-width: 1200px;

  .contents {
    height: 80vh;
    overflow-y: auto;
  }
}

.tool-tree {
  display: flex;
  height: 300px;
  border-right: 1px solid lightgray;
  flex-direction: column;
  .q-tree {
    overflow: auto;
  }
}

.parameter-table {
  height: 300px;

  :deep(th) {
    font-weight: 1000;
  }
}

.search-input {
  :deep(.q-field__control) {
    min-height: 40px;
  }

  :deep(.q-field__native) {
    padding-right: 12px;
    padding-left: 12px;
    font-size: 14px;
    line-height: 1.4;
  }

  :deep(.q-field__control-container) {
    padding-top: 0;
    padding-bottom: 0;
  }
}
</style>
