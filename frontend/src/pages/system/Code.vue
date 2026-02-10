<template>
  <q-page class="q-pa-sm">
    <div class="row full-height">
      <q-card class="col-4">
        <div class="row q-pa-md top-search-wrap" style="padding: 12px 16px">
          <q-input
            v-model="codeTree.search"
            standout="bg-blue-grey-5"
            :placeholder="$t('코드 검색')"
            color="white"
            dense
            clearable
            clear-icon="close"
            class="col"
            input-class="text-white"
          />
          <q-btn flat round color="white" @click="toggleExpandTree" :icon="treeExpandIcon" class="col-auto q-ml-sm" />
        </div>
        <q-card-section class="q-pt-sm overflow-auto">
          <q-tree
            v-if="codeTree.items.length > 0"
            ref="tree"
            :nodes="codeTree.items"
            node-key="treeId"
            label-key="name"
            :filter="codeTree.search"
            class="selectable"
            selected-color="primary"
            no-selection-unset
            v-model:selected="codeTree.selected"
            v-model:expanded="codeTree.expanded"
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
                <q-menu touch-position context-menu class="tree-context-menu">
                  <q-list>
                    <q-item
                      v-for="(item, index) in treeContextMenu.menuItems"
                      :key="index"
                      clickable
                      v-close-popup
                      @click="onClickTreeContextMenu(item)"
                    >
                      <q-item-section avatar>
                        <q-icon :color="item.color" :name="item.icon" size="sm" />
                      </q-item-section>
                      <q-item-section>
                        {{ item.label }}
                      </q-item-section>
                    </q-item>
                  </q-list>
                </q-menu>
              </div>
            </template>
          </q-tree>
          <div v-else class="tree-empty flex flex-center">
            <div class="column">
              <div class="text-subtitle2 q-mb-sm">
                {{ $t('등록된 카테고리가 없습니다.') }}
              </div>
              <q-btn
                v-if="editItem === undefined"
                color="secondary"
                :label="$t('카테고리 등록')"
                icon="data_array"
                @click="
                  onClickTreeContextMenu({
                    id: 'CREATE_CATEGORY',
                  })
                "
              />
            </div>
          </div>
        </q-card-section>
      </q-card>
      <div v-if="editItem?.type === 'CATEGORY'" class="col-8 q-px-md">
        <q-card>
          <q-card-section>
            <div class="title_wrap">
              <h3 class="text-h6">{{ $t('카테고리') }} {{ editItem?.id ? $t('수정') : $t('등록') }}</h3>
              <span class="field-required-indicate">{{ $t('필수 입력 필드') }}</span>
            </div>
            <q-form ref="form" lazy-validation>
              <div class="row">
                <q-input
                  v-model="editItem.category"
                  :label="$t('카테고리')"
                  :rules="[$rules.required]"
                  class="input-required col-6 q-pr-md"
                  :disable="editItem.id !== undefined"
                />
                <q-input
                  v-model="editItem.name"
                  :label="$t('카테고리명')"
                  :rules="[$rules.required]"
                  class="input-required col-6"
                />
              </div>
              <div class="row">
                <q-input v-model="editItem.description" :label="$t('설명')" :rules="[$rules.maxLength(300)]" />
              </div>
            </q-form>
            <div class="row justify-end footer_btnWrap">
              <q-btn v-permission:code="editItem?.id ? 'update' : 'create'" class="sBtn" @click="saveCategory">{{
                $t('저장')
              }}</q-btn>
            </div>
          </q-card-section>
        </q-card>
        <q-card class="q-mt-md" v-if="editItem.id !== undefined">
          <q-card-section>
            <div class="title_wrap row">
              <h3>
                {{ $t('코드 목록') }}
              </h3>
              <div class="col-8 flex cnt-flex-end">
                <q-btn
                  v-permission:code.update
                  v-if="!categoryCodeGrid.isSortMode"
                  push
                  class="downup_btn with_icon_btn sBtn secondary"
                  @click="categoryCodeGrid.isSortMode = true"
                >
                  {{ $t('코드 순서 변경') }}
                  <!-- <q-tooltip anchor="top middle" self="bottom middle" :offset="[10, 10]">
                    {{ $t('코드 순서 변경') }}
                  </q-tooltip> -->
                </q-btn>
                <q-btn v-else push class="save_btn with_icon_btn sBtn secondary" @click="saveCodeSort">
                  {{ $t('순서 저장') }}
                </q-btn>
                <q-btn
                  v-permission:code.create
                  @click="formCreateCode(editItem.id, editItem.name)"
                  push
                  class="add_btn with_icon_btn sBtn secondary"
                >
                  {{ $t('추가') }}
                </q-btn>
                <q-btn
                  v-permission:code.delete
                  :disable="categoryCodeGrid.selected.length === 0"
                  @click="deleteCodes(categoryCodeGrid.selected)"
                  push
                  class="delete_btn with_icon_btn sBtn secondary"
                >
                  {{ $t('삭제') }}
                </q-btn>
              </div>
            </div>
            <q-markup-table>
              <thead>
                <tr>
                  <th class="text-left"></th>
                  <th class="text-left">{{ $t('코드명') }}</th>
                  <th class="text-center">{{ $t('설명') }}</th>
                </tr>
              </thead>
              <draggable
                v-if="categoryCodeGrid.items.length > 0"
                v-model="categoryCodeGrid.items"
                item-key="id"
                tag="tbody"
                :animation="200"
                ghostClass="draggable-ghost"
                handle=".drag-icon"
              >
                <template #item="{ element }">
                  <tr
                    :class="`list-group-item ${categoryCodeGrid.selected.find((codeId) => codeId === element.id) ? 'selected' : ''}`"
                    :key="element.id"
                    @click="onClickCategoryCode(element.id)"
                  >
                    <td>
                      <q-icon v-if="categoryCodeGrid.isSortMode" name="drag_handle" class="drag-icon" size="sm" />
                    </td>
                    <td>
                      <span>
                        {{ element.name }}
                      </span>
                    </td>
                    <td>
                      <span>
                        {{ element.description }}
                      </span>
                    </td>
                  </tr>
                </template>
              </draggable>
              <tbody v-else>
                <tr>
                  <td colspan="3" class="text-center text-caption">{{ $t('등록된 코드가 없습니다.') }}</td>
                </tr>
              </tbody>
            </q-markup-table>
          </q-card-section>
        </q-card>
      </div>
      <div v-else-if="editItem?.type === 'CODE'" class="col-8 q-px-md">
        <q-card>
          <q-card-section>
            <div class="title_wrap">
              <h3>{{ $t('코드') }} {{ editItem?.id ? $t('수정') : $t('등록') }}</h3>
              <span class="field-required-indicate">{{ $t('필수 입력 필드') }}</span>
            </div>
            <q-form ref="form" lazy-validation>
              <div class="row">
                <q-input
                  :model-value="editItem.categoryName"
                  :label="$t('카테고리명')"
                  :rules="[$rules.required]"
                  class="input-required col-6 q-pr-md"
                  disable
                />
                <q-input
                  v-model="editItem.code"
                  :label="$t('코드')"
                  :rules="[$rules.required]"
                  class="input-required col-6"
                  :disable="editItem.id !== undefined"
                />
              </div>
              <div class="row">
                <q-input
                  v-model="editItem.name"
                  :label="$t('코드명')"
                  :rules="[$rules.required]"
                  class="input-required col-6 q-pr-md"
                />
                <q-input class="col-6" v-model="editItem.value" :label="$t('코드값')" />
              </div>
              <div class="row">
                <q-input v-model="editItem.description" :label="$t('설명')" :rules="[$rules.maxLength(300)]" />
              </div>
            </q-form>
          </q-card-section>
        </q-card>
        <q-card class="q-mt-md">
          <q-card-section>
            <div class="title_wrap row">
              <h3>
                {{ $t('하위 코드 목록') }}
              </h3>
              <div class="col-3">
                <q-form ref="form">
                  <filterable-select
                    :model-value="editItem.childCategoryId"
                    :options="childCategories"
                    option-value="id"
                    option-label="name"
                    :label="$t('하위 카테고리')"
                    dense
                    filled
                    clearable
                    emit-value
                    map-options
                    @update:model-value="onChangeChildCategory"
                  />
                </q-form>
              </div>
            </div>
            <q-table
              v-model:selected="childCodeGrid.selected"
              :rows="childCategoryCodes"
              :columns="childCodeGrid.columns"
              hide-pagination
              flat
              row-key="id"
              selection="multiple"
            >
              <template v-slot:no-data>
                <div class="text-center text-caption col q-py-lg">
                  <q-icon name="info" size="sm" color="info" class="q-mr-sm" />
                  {{
                    editItem.childCategoryId
                      ? $t('카테고리에 등록된 코드가 없습니다.')
                      : $t('하위 카테고리를 선택 해 주세요.')
                  }}
                </div>
              </template>
            </q-table>
            <div class="row justify-end footer_btnWrap">
              <q-btn v-permission:code="editItem?.id ? 'update' : 'create'" class="sBtn" @click="saveCode">{{
                $t('저장')
              }}</q-btn>
            </div>
          </q-card-section>
        </q-card>
        <q-card v-if="editItem.type === 'CODE' && editItem.id" class="section-top-line">
          <q-card-section>
            <div class="title_wrap">
              <h3>
                {{ $t('참조 코드 목록') }}
              </h3>
            </div>
            <q-table :rows="parentCodeGrid.items" :columns="parentCodeGrid.columns" hide-pagination flat row-key="id">
              <template v-slot:no-data>
                <div class="text-center text-caption col q-py-lg">
                  <q-icon name="info" size="sm" color="info" class="q-mr-sm" />
                  {{ $t('참조하고 있는 코드가 없습니다.') }}
                </div>
              </template>
            </q-table>
          </q-card-section>
        </q-card>
      </div>
    </div>
  </q-page>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import useUI from 'src/common/module/ui'
import CodeService from 'src/services/system/CodeService'
import draggable from 'vuedraggable'
import { findTreeItem } from 'src/common/treeUtil'
import { useAuthStore } from 'src/stores/auth'

const ui = useUI()
const { t } = useI18n()
const { isPermitted } = useAuthStore()

const tree = ref(null)
const form = ref(null)

const editItem = ref()

const codeTree = ref({
  isExpandAll: false,
  search: '',
  items: [],
  selected: [],
  expanded: [],
})
const treeContextMenu = ref({
  show: false,
  x: 0,
  y: 0,
  menuItems: [],
})
const categoryCodeGrid = ref({
  items: [],
  selected: [],
})
const childCodeGrid = ref({
  columns: [
    { name: 'name', align: 'left', label: t('코드명'), field: 'name', sortable: false },
    { name: 'description', align: 'left', label: t('설명'), field: 'description', sortable: false },
  ],
  items: [],
  selected: [],
})
const parentCodeGrid = ref({
  columns: [
    { name: 'name', align: 'left', label: t('카테고리명'), field: 'categoryName', sortable: false },
    { name: 'name', align: 'left', label: t('코드명'), field: 'name', sortable: false },
    { name: 'description', align: 'left', label: t('설명'), field: 'description', sortable: false },
  ],
  items: [],
})

const getTreeIconByType = (type) => {
  if (type === 'CATEGORY') {
    return {
      icon: 'mdi-data-array',
      color: 'primary',
    }
  } else {
    return {
      icon: 'mdi-data-object',
      color: 'secondary',
    }
  }
}

const toggleExpandTree = () => {
  if (codeTree.value.isExpandAll) {
    tree.value.collapseAll()
    codeTree.value.isExpandAll = !codeTree.value.isExpandAll
  } else {
    tree.value.expandAll()
    codeTree.value.isExpandAll = !codeTree.value.isExpandAll
  }
}

const onTreeSelected = async (id) => {
  categoryCodeGrid.value.items = []
  categoryCodeGrid.value.selected = []
  const selectedItem = findTreeItem(codeTree.value.items, id, { id: 'treeId' })
  ui.loading.show()
  if (selectedItem.type === 'CATEGORY') {
    //카테고리 정보 조회
    formModifyCategory(selectedItem.id, selectedItem.object.category)
  } else {
    //코드 정보 조회
    formModifyCode(selectedItem.id)
  }
  ui.loading.hide()
}

const showTreeContextMenu = (e, node) => {
  treeContextMenu.value.show = false
  treeContextMenu.value.x = e.x
  treeContextMenu.value.y = e.y
  const menuItems = []
  // if (hasCreatePermission.value) {
  if (node.type === 'CATEGORY') {
    if (isPermitted('code', 'create')) {
      menuItems.push(
        {
          label: t('카테고리 추가'),
          id: 'CREATE_CATEGORY',
          ...getTreeIconByType('CATEGORY'),
        },
        {
          label: t('코드 추가'),
          id: 'CREATE_CODE',
          item: node,
          ...getTreeIconByType('CODE'),
        },
      )
    }
    if (isPermitted('code', 'delete')) {
      menuItems.push({
        label: t('삭제'),
        id: 'DELETE_CATEGORY',
        item: node,
        icon: 'mdi-delete-forever',
        color: 'red',
      })
    }
  } else {
    if (isPermitted('code', 'delete')) {
      menuItems.push({
        label: t('삭제'),
        id: 'DELETE_CODE',
        item: node,
        icon: 'mdi-delete-forever',
        color: 'red',
      })
    }
  }
  treeContextMenu.value.menuItems = menuItems
  treeContextMenu.value.show = true
}

const onClickTreeContextMenu = (menuItem) => {
  if (menuItem.id === 'CREATE_CATEGORY') {
    formCreateCategory()
  } else if (menuItem.id === 'CREATE_CODE') {
    formCreateCode(menuItem.item.id, menuItem.item.name)
  } else if (menuItem.id === 'DELETE_CATEGORY') {
    deleteCategory(menuItem.item.id)
  } else if (menuItem.id === 'DELETE_CODE') {
    deleteCodes([menuItem.item.id])
  }
}

const formCreateCategory = () => {
  editItem.value = {
    type: 'CATEGORY',
    category: undefined,
    name: undefined,
    description: undefined,
  }
  codeTree.value.selected = []
}

const formModifyCategory = async (categoryId, category) => {
  categoryCodeGrid.value.isSortMode = false
  const [categoryInfo, categoryCodes] = await Promise.all([
    CodeService.getCategory(categoryId),
    CodeService.getCodesByCategory(category),
  ])
  editItem.value = {
    type: 'CATEGORY',
    ...categoryInfo,
  }
  categoryCodeGrid.value.items = categoryCodes
}

const formCreateCode = (categoryId, categoryName) => {
  editItem.value = {
    type: 'CODE',
    categoryId,
    categoryName,
    code: undefined,
    name: undefined,
    value: undefined,
    description: undefined,
    childCategoryId: undefined,
  }
  codeTree.value.selected = []
}

const formModifyCode = async (codeId) => {
  const code = await CodeService.getCode(codeId)
  editItem.value = {
    type: 'CODE',
    ...code,
  }
  onChangeChildCategory(code.childCategoryId)
  childCodeGrid.value.selected = code.childCodes
  parentCodeGrid.value.items = code.parentCodes
}

const onChangeChildCategory = async (categoryId) => {
  childCodeGrid.value.selected = []
  if (categoryId) {
    const selectedCategory = codeTree.value.items.find((category) => category.id === categoryId)
    editItem.value = {
      ...editItem.value,
      childCategoryId: selectedCategory.id,
    }
  } else {
    editItem.value = {
      ...editItem.value,
      childCategoryId: undefined,
    }
  }
}

const onClickCategoryCode = (codeId) => {
  const isContains = categoryCodeGrid.value.selected.find((id) => id === codeId)
  if (isContains) {
    categoryCodeGrid.value.selected = categoryCodeGrid.value.selected.filter((id) => id !== codeId)
  } else {
    categoryCodeGrid.value.selected = [...categoryCodeGrid.value.selected, codeId]
  }
}

const selectTreeItemCode = (codeId, categoryId) => {
  const codeTreeId = `CODE_${codeId}`
  codeTree.value.selected = codeTreeId
  codeTree.value.expanded = [...new Set(codeTree.value.expanded).add(`CATEGORY_${categoryId}`)]
  onTreeSelected(codeTreeId)
}

const selectTreeItemCategory = (categoryId) => {
  const categoryTreeId = `CATEGORY_${categoryId}`
  codeTree.value.selected = categoryTreeId
  onTreeSelected(categoryTreeId)
}

const saveCodeSort = async () => {
  ui.loading.show()
  try {
    await CodeService.sortCodes(
      editItem.value.id,
      categoryCodeGrid.value.items.map((code) => code.id),
    )
    ui.notify.success(t('저장 되었습니다.'))
    categoryCodeGrid.value.isSortMode = false
    await loadCodeTree()
    selectTreeItemCategory(editItem.value.id)
  } finally {
    ui.loading.hide()
  }
}

const saveCategory = async () => {
  if (!(await form.value.validate())) {
    ui.notify.warning('유효하지 않은 입력값이 있습니다.')
    return
  }
  ui.loading.show()
  try {
    let result = null
    if (editItem.value.id === undefined) {
      result = await CodeService.createCategory(editItem.value)
    } else {
      result = await CodeService.modifyCategory(editItem.value.id, editItem.value)
    }
    ui.notify.success(t('저장 되었습니다.'))
    await loadCodeTree()
    selectTreeItemCategory(result.id)
  } finally {
    ui.loading.hide()
  }
}

const saveCode = async () => {
  if (!(await form.value.validate())) {
    ui.notify.warning('유효하지 않은 입력값이 있습니다.')
    return
  }
  ui.loading.show()
  try {
    const data = {
      ...editItem.value,
      childCodeIds: childCodeGrid.value.selected.map((code) => code.id),
    }
    let result = null
    if (editItem.value.id === undefined) {
      result = await CodeService.createCode(data)
    } else {
      result = await CodeService.modifyCode(data.id, data)
    }
    ui.notify.success(t('저장 되었습니다.'))
    await loadCodeTree()
    selectTreeItemCode(result.id, result.categoryId)
  } finally {
    ui.loading.hide()
  }
}

const deleteCategory = (categoryId) => {
  ui.confirm(
    t('카테고리 삭제'),
    t('하위의 모든 코드가 삭제됩니다. 해당 카테고리를 삭제하시겠습니까?'),
    t('삭제'),
    t('취소'),
  ).onOk(async () => {
    ui.loading.show()
    try {
      await CodeService.deleteCategory(categoryId)
      ui.notify.success(t('카테고리가 삭제 되었습니다.'))
      loadCodeTree()
    } finally {
      ui.loading.hide()
    }
  })
}

const deleteCodes = (codeIds) => {
  ui.confirm(
    t('코드 삭제'),
    t('코드와 연결된 데이터가 있는 경우 데이터가 정상적으로 나오지 않을 수 있습니다.삭제하시겠습니까?'),
    t('삭제'),
    t('취소'),
  ).onOk(async () => {
    ui.loading.show()
    try {
      const codes = await CodeService.deleteCodes(codeIds)
      ui.notify.success(t('코드가 삭제 되었습니다.'))
      await loadCodeTree()
      selectTreeItemCategory(codes[0].categoryId)
    } finally {
      ui.loading.hide()
    }
  })
}

const loadCodeTree = async () => {
  ui.loading.show()
  codeTree.value.selected = []
  codeTree.value.items = await CodeService.getCodeTree()
  ui.loading.hide()
}

const childCategories = computed(() => {
  if (editItem.value?.type === 'CODE') {
    return codeTree.value.items
      .filter((codeCategory) => codeCategory.id !== editItem.value.categoryId)
      .map((codeCategory) => codeCategory.object)
  } else {
    return []
  }
})

const childCategoryCodes = computed(() => {
  const selectedCategory =
    editItem.value.childCategoryId &&
    codeTree.value.items.find((category) => category.id === editItem.value.childCategoryId)
  return selectedCategory?.children.map((code) => code.object) || []
})

const treeExpandIcon = computed(() => (codeTree.value.isExpandAll ? 'mdi-collapse-all' : 'mdi-expand-all'))

onMounted(loadCodeTree)
</script>

<style lang="scss" scoped>
:deep(.q-tree),
.tree-empty {
  height: 70vh;
}

.tree-context-menu .q-list {
  width: 190px !important;
}

.drag-icon {
  cursor: move;
}

.draggable-ghost {
  opacity: 0.5;
  background: var(--q-grey-5);
}

.q-markup-table {
  tr {
    cursor: pointer;
  }
}
</style>
