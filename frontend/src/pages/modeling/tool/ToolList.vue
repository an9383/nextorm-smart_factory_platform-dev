<template>
  <q-page class="q-pa-sm">
    <div class="row full-height">
      <q-card class="col-4">
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
              <div class="flex items-center" @contextmenu.prevent="showTreeContextMenu($event, node)">
                <q-icon
                  :name="getTreeIconByType(node.type).icon"
                  :color="getTreeIconByType(node.type).color"
                  size="sm"
                />
                <span class="q-ml-sm text-subtitle2">{{ node.name }}</span>
                <q-menu
                  v-if="treeContextMenu.menuItems.length > 0"
                  touch-position
                  context-menu
                  class="tree-context-menu"
                >
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
                            item.id === 'DELETE'
                              ? 'blue-grey-8'
                              : getTreeIconByType(item.id.replace('CREATE_', '')).color
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
      <div class="col-8 q-pl-md">
        <q-card v-if="editItem !== undefined">
          <q-card-section>
            <div class="title_wrap">
              <h3>{{ editItem.id === undefined ? $t('설비 추가') : $t('설비 수정') }}</h3>
              <span class="field-required-indicate">{{ $t('필수 입력 필드') }}</span>
            </div>
            <q-form ref="form" lazy-validation>
              <div class="row">
                <cascader
                  v-model="editItem.location.id"
                  class="input-required col"
                  :label="$t('라인')"
                  :titles="[$t('사이트'), $t('공장'), $t('라인')]"
                  :items="locations"
                  item-value="id"
                  item-text="name"
                  clearable
                  :valid-depth="3"
                  :valid-depth-msg="$t('설비는 라인 하위에만 추가 할 수 있습니다.')"
                  :rules="[$rules.required]"
                />
              </div>
              <div class="row">
                <filterable-select
                  v-model="editItem.toolType"
                  :label="$t('설비분류')"
                  :options="toolTypeOptions"
                  option-value="value"
                  option-label="text"
                  :rules="[$rules.required]"
                  class="input-required col-6 q-pr-md"
                />
                <filterable-select
                  v-model="editItem.type"
                  :label="$t('타입')"
                  :options="typeOptions"
                  option-value="value"
                  option-label="text"
                  :rules="[$rules.required]"
                  class="input-required col-6 q-pl-md"
                />
              </div>
              <div class="row">
                <q-input
                  v-model="editItem.name"
                  :label="$t('이름')"
                  :rules="[$rules.required]"
                  class="input-required col"
                />
              </div>
            </q-form>
            <div class="row justify-end footer_btnWrap">
              <q-btn v-permission:tool="editItem?.id ? 'update' : 'create'" class="sBtn" @click="saveTool">{{
                $t('저장')
              }}</q-btn>
            </div>
          </q-card-section>
        </q-card>
        <q-card v-else-if="toolTree.selected">
          <q-card-section>
            <div class="title_wrap">
              <h3>
                {{ $t('설비 목록') }}
              </h3>
              <div class="btn_wrap">
                <q-btn v-permission:tool.create @click="onCreate" class="add_btn with_icon_btn sBtn">{{
                  $t('추가')
                }}</q-btn>
                <q-btn
                  v-permission:tool.delete
                  :disable="toolGrid.selected.length === 0"
                  @click="confirmDelete(toolGrid.selected)"
                  class="delete_btn with_icon_btn sBtn"
                  >{{ $t('삭제') }}</q-btn
                >
              </div>
            </div>
            <simple-table
              v-model:selected="toolGrid.selected"
              :rows="toolGrid.items"
              :columns="toolGrid.columns"
              flat
              row-key="id"
              selection="multiple"
            >
              <template v-slot:body="props">
                <q-tr
                  class="cursor-pointer"
                  :props="props"
                  @click="props.selected = !props.selected"
                  @dblclick="selectTreeToolItem(props)"
                >
                  <q-td v-for="col in props.cols" :key="col.name" :props="props">
                    {{ col.value }}
                  </q-td>
                </q-tr>
              </template>
            </simple-table>
          </q-card-section>
        </q-card>
      </div>
    </div>
  </q-page>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import useUI from 'src/common/module/ui'
import LocationService from 'src/services/modeling/LocationService.js'
import ToolService from 'src/services/modeling/ToolService.js'
import Cascader from 'src/components/cascader/Cascader.vue'
import { useAuthStore } from 'src/stores/auth'

const ui = useUI()
const { t } = useI18n()
const { isPermitted } = useAuthStore()

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
    { name: 'factory', align: 'left', label: t('공장'), field: (row) => row.location.parent.name, sortable: false },
    { name: 'line', align: 'left', label: t('라인'), field: (row) => row.location.name, sortable: false },
    { name: 'name', align: 'left', label: t('설비명'), field: 'name', sortable: false },
    { name: 'toolType', align: 'left', label: t('설비 분류'), field: 'toolType', sortable: false },
    { name: 'type', align: 'left', label: t('타입'), field: 'type', sortable: false },
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

const typeOptions = ref(['PROCESS', 'MEASURE'])
const toolTypeOptions = ref(['MAIN', 'SUB'])

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
//설비정보 저장
const saveTool = async () => {
  const success = await form.value.validate()
  if (success) {
    ui.loading.show()
    let responseTool = null
    if (editItem.value.id) {
      try {
        responseTool = await ToolService.modifyTool(editItem.value.id, {
          ...editItem.value,
          locationId: editItem.value.location.id,
        })
        ui.notify.success(t('저장되었습니다.'))
      } finally {
        ui.loading.hide()
      }
    } else {
      try {
        responseTool = await ToolService.createTool({ ...editItem.value, locationId: editItem.value.location.id })
        ui.notify.success(t('저장되었습니다.'))
      } finally {
        ui.loading.hide()
      }
    }
    ui.loading.hide()

    editItem.value = undefined
    toolTree.selected = null
    await loadToolTree(responseTool.location.id)
    await loadToolListUnderLocation(responseTool.location.id)
  } else {
    ui.notify.warning(t('유효하지 않은 입력값이 있습니다.'))
  }
}

const onCreate = () => {
  const location = tree.value.getNodeByKey(toolTree.selected)
  editItem.value = {
    location: { id: location.id },
    name: '',
    toolType: '',
    type: '',
  }
}

const confirmDelete = (items) => {
  ui.confirm(
    t('위치 삭제'),
    t('하위의 모든 위치가 삭제됩니다. 해당 위치를 삭제하시겠습니까?'),
    t('삭제'),
    t('취소'),
  ).onOk(() => deleteTool(items))
}

const selectTreeToolItem = (evt) => {
  if (evt.row) {
    if (!toolTree.isExpandAll) {
      toggleExpandTree()
    }
    toolTree.selected = findTreeItemById(evt.row.id)?.treeId
    onTreeSelected(toolTree.selected)
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

const onTreeSelected = (id) => {
  const location = tree.value.getNodeByKey(id)
  if (location) {
    toolGrid.value.items = []
    toolGrid.value.selected = []
    if (location.type === 'TOOL') {
      loadToolInfo(location)
    } else {
      editItem.value = undefined
      loadToolListUnderLocation(location.id)
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

const showTreeContextMenu = (e, node) => {
  treeContextMenu.value.show = false
  treeContextMenu.value.x = e.x
  treeContextMenu.value.y = e.y
  const menuItems = []
  // if (hasCreatePermission.value) {
  if (node.type === 'LINE') {
    if (isPermitted('tool', 'create')) {
      menuItems.push({
        label: t('설비 추가'),
        id: 'CREATE',
        location: node,
      })
    }
  } else if (node.type === 'TOOL') {
    if (isPermitted('tool', 'delete')) {
      menuItems.push({
        label: t('설비 삭제'),
        id: 'DELETE',
        tool: node,
      })
    }
  }
  treeContextMenu.value.menuItems = menuItems
  treeContextMenu.value.show = true
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

const findTreeItemById = (id) => {
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
  return expandTreeData(toolTree.items).find((v) => v.id === id && v.type === 'TOOL')
}

watch(
  () => editItem.value,
  () => {
    if (form.value) {
      form.value.resetValidation()
    }
  },
)

const form = ref(null)
loadOptions()

loadToolTree()
</script>

<style lang="scss" scoped>
:deep(.q-tree),
.tree-empty {
  height: 70vh;
}

.tree-context-menu .q-list {
  width: 190px !important;
}
</style>
