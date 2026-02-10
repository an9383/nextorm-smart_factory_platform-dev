<template>
  <div class="row" style="height: 100%">
    <q-card class="col-3" flat bordered>
      <div class="row q-pa-md top-search-wrap" style="padding: 12px 16px">
        <q-input
          standout="bg-blue-grey-5"
          v-model="toolTree.search"
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
          @click="toggleExpandTree"
          :icon="toolTree.isExpandAll ? 'mdi-collapse-all' : 'mdi-expand-all'"
          class="col-auto q-ml-sm"
        />
      </div>
      <q-card-section class="q-pt-sm overflow-auto">
        <q-tree
          v-if="toolTree.items.length > 0"
          ref="tree"
          :nodes="toolTree.items"
          node-key="treeId"
          label-key="name"
          :filter="toolTree.search"
          class="selectable"
          selected-color="primary"
          no-selection-unset
          v-model:selected="toolTree.selected"
          v-model:expanded="toolTree.expanded"
          @update:expanded="onTreeExpanded"
          @update:selected="onTreeSelected"
        >
          <template #default-header="{ node }">
            <div class="flex items-center">
              <q-icon :name="getTreeIconByType(node.type).icon" :color="getTreeIconByType(node.type).color" size="sm" />
              <span class="q-ml-sm text-subtitle2">{{ node.name }}</span>
              <q-menu v-if="treeContextMenu.menuItems.length > 0" touch-position context-menu class="tree-context-menu">
                <q-list>
                  <q-item
                    v-for="(item, index) in treeContextMenu.menuItems"
                    :key="index"
                    clickable
                    v-close-popup
                    @click="onClickTreeContextMenu(item)"
                  >
                    <q-item-section>
                      <q-icon
                        :color="
                          item.id === 'DELETE' ? 'blue-grey-8' : getTreeIconByType(item.id.replace('CREATE_', '')).color
                        "
                        :name="
                          item.id === 'DELETE'
                            ? 'mdi-delete-forever'
                            : getTreeIconByType(item.id.replace('CREATE_', '')).icon
                        "
                        size="25px"
                      />
                    </q-item-section>
                    <q-item-section>{{ item.label }}</q-item-section>
                  </q-item>
                </q-list>
              </q-menu>
            </div>
          </template>
        </q-tree>
        <div v-else class="tree-empty flex flex-center">
          <div class="column">
            <div class="text-subtitle2 q-mb-sm">
              {{ $t('등록된 위치정보가 없습니다.') }}
            </div>
          </div>
        </div>
      </q-card-section>
    </q-card>
    <q-card class="col-3" flat bordered>
      <q-card-section style="height: 635px">
        <div class="title_wrap">
          <h3>{{ $t('파라미터 선택') }}</h3>
        </div>
        <q-table
          class="parameter-table"
          :rows="parameterRows"
          :columns="columns"
          row-key="id"
          selection="multiple"
          v-model:selected="selectedParameters"
          :hide-bottom="true"
          :pagination="{
            rowsPerPage: 0,
          }"
        >
          <template v-slot:body-cell-name="props">
            <q-td :props="props">
              <div class="ellipsis" style="max-width: 150px">
                <q-tooltip>{{ t(props.value) }}</q-tooltip>
                {{ t(props.value) }}
              </div>
            </q-td>
          </template>
          <template v-slot:body-cell-isY="props">
            <q-td :props="props">
              <q-radio v-model="selectedYParameterId" @update:modelValue="handleYParameterSelect" :val="props.row.id" />
            </q-td>
          </template>
        </q-table>
      </q-card-section>
    </q-card>
    <q-card class="col-6" style="height: 690px" flat bordered>
      <parameter-trend-analysis
        ref="parameterTrendAnalysisRef"
        v-model:y-parameter="selectedYParameter"
        v-model:x-parameters="selectedParameters"
      />
    </q-card>
  </div>
</template>

<script setup>
import { onBeforeMount, reactive, ref, watch } from 'vue'
import useUI from 'src/common/module/ui'
import LocationService from 'src/services/modeling/LocationService.js'
import ToolService from 'src/services/modeling/ToolService.js'
import ParameterService from 'src/services/modeling/ParameterService'
import { findParameterByToolName } from 'pages/ai/inspectionUtil'
import { useI18n } from 'vue-i18n'
import { pt } from 'src/plugins/i18n'
import ParameterTrendAnalysis from 'pages/ai/modeling/parameterSelectStep/ParameterTrendAnalysis.vue'

const ui = useUI()
const { t } = useI18n()

const parameterTrendAnalysisRef = ref(null)
const tree = ref(null)

const treeContextMenu = ref({
  show: false,
  x: 0,
  y: 0,
  menuItems: [],
  selectedItem: undefined,
})

const toolGrid = ref({
  columns: [
    {
      name: 'factory',
      align: 'left',
      label: t('공장'),
      field: (row) => row.location.parent.name,
      sortable: false,
    },
    {
      name: 'line',
      align: 'left',
      label: t('라인'),
      field: (row) => row.location.name,
      sortable: false,
    },
    {
      name: 'name',
      align: 'left',
      label: t('설비명'),
      field: 'name',
      sortable: false,
    },
    {
      name: 'toolType',
      align: 'left',
      label: t('설비 분류'),
      field: 'toolType',
      sortable: false,
    },
    {
      name: 'type',
      align: 'left',
      label: t('타입'),
      field: 'type',
      sortable: false,
    },
  ],
  items: [],
  selected: [],
  loading: false,
})
const toolTree = reactive({
  isExpandAll: false,
  search: '',
  items: [],
  selected: null,
  expanded: [],
})

const locations = ref([])

const editItem = ref(undefined)

const loadOptions = async () => {
  ui.loading.show()
  locations.value = await LocationService.getLocationsTreeTypeUntil('LINE')
  ui.loading.hide()
}

const deleteTool = async (items) => {
  ui.loading.show()
  try {
    if (items.length === 1) {
      await ToolService.deleteToolById(items[0].id)
    } else {
      await ToolService.bulkDelete(items.map((tool) => tool.id))
    }
    ui.notify.success(t('삭제 되었습니다.'))
  } catch (error) {
    error.response.data.extraData.datas.forEach((message) => {
      if (error.response.data.code === 'ERROR_CONSTRAINT_VIOLATION') {
        ui.notify.error(message.path) //임시
      }
    })
  }
  ui.loading.hide()
  editItem.value = undefined
  toolTree.selected = null
  toolGrid.value.selected = []
  loadToolTree(items[0].parent || items[0].location.id)
  await loadToolListUnderLocation(items[0].parent || items[0].location.id)
}

const confirmDelete = (items) => {
  ui.confirm(
    t('위치 삭제'),
    t('하위의 모든 위치가 삭제됩니다. 해당 위치를 삭제하시겠습니까?'),
    t('삭제'),
    t('취소'),
  ).onOk(() => deleteTool(items))
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

const toggleExpandTree = () => {
  if (toolTree.isExpandAll) {
    tree.value.collapseAll()
    toolTree.isExpandAll = !toolTree.isExpandAll
  } else {
    tree.value.expandAll()
    toolTree.isExpandAll = !toolTree.isExpandAll
  }
}

const onTreeExpanded = (expandedIds) => {
  toolTree.expanded = expandedIds
}

const onTreeSelected = async (id) => {
  const location = tree.value.getNodeByKey(id)
  console.log(location)
  if (location) {
    toolGrid.value.items = []
    toolGrid.value.selected = []
    if (location.type === 'TOOL') {
      await loadToolInfo(location)
      await getParameters(location.id, location.name)
    } else {
      editItem.value = undefined
      await loadToolListUnderLocation(location.id)
    }
  }
}

const loadToolInfo = async (location) => {
  ui.loading.show()
  const tool = await ToolService.getToolById(location.id)
  ui.loading.hide()
  editItem.value = { ...tool, location: { id: location.parent } }
}

const loadToolTree = async (lineId) => {
  ui.loading.show()
  toolTree.items = await LocationService.getLocationsAndToolsTree()
  ui.loading.hide()

  if (lineId) {
    toolTree.selected = `LINE_${lineId}`
  }
}

const loadToolListUnderLocation = async (locationId) => {
  ui.loading.show()
  toolGrid.value.items = await LocationService.getLocationUnderTools(locationId)
  ui.loading.hide()
}

const onClickTreeContextMenu = (menuItem) => {
  if (menuItem.id === 'DELETE') {
    confirmDelete([menuItem.tool])
  } else {
    editItem.value = {
      location: { id: menuItem.location.id },
      name: '',
      toolType: '',
      type: '',
    }
  }
}

watch(
  () => editItem.value,
  () => {
    if (form.value) {
      form.value.resetValidation()
    }
    console.log(selectedParameters.value)
  },
)

const form = ref(null)

const columns = [
  {
    name: 'name',
    align: 'left',
    label: t('파라미터명'),
    field: 'name',
    format: (val) => pt(val),
  },
  { name: 'isY', align: 'left', label: t('Y값 여부'), field: 'isY' },
]

const parameterRows = ref([])

const selectedParameters = ref([])
const selectedYParameterId = ref(null)
const selectedYParameter = ref(null)

const getParameters = async (toolId, toolName) => {
  let parameters = await ParameterService.getParameters({ toolId })
  if (toolName.startsWith('살균기')) {
    parameters = [...parameters, findParameterByToolName(toolName)]
  }
  parameterRows.value = parameters.filter((parameter) => parameter.dataType !== 'STRING')
  selectedParameters.value = []
  selectedYParameterId.value = null
}

const handleYParameterSelect = (parameterId) => {
  //selectedParameters.value = selectedParameters.value.filter((parameter) => parameter.id !== parameterId)
  selectedYParameter.value = parameterRows.value.find((parameter) => parameter.id === parameterId)
}
watch(
  selectedParameters,
  (newSelectedParameters) => {
    // Y 파라미터가 있고, X 파라미터 목록에 Y 파라미터가 포함되어 있는지 확인
    if (selectedYParameterId.value && newSelectedParameters.some((param) => param.id === selectedYParameterId.value)) {
      // Y 파라미터 선택 해제
      selectedYParameterId.value = null
      selectedYParameter.value = null
    }
  },
  { deep: true },
)

onBeforeMount(() => {
  loadOptions()
  loadToolTree()
})

defineExpose({
  getData: () => {
    if (!parameterTrendAnalysisRef.value) {
      return null
    }
    return parameterTrendAnalysisRef.value.getData()
  },
})
</script>

<style lang="scss" scoped>
:deep(.q-tree),
.tree-empty {
  min-height: 550px;
  max-height: 550px;
}

.tree-context-menu .q-list {
  width: 180px !important;
}

.parameter-table {
  overflow-y: scroll;
  height: 600px;

  :deep(.q-table__middle) {
    max-height: 635px;
  }
}
</style>
