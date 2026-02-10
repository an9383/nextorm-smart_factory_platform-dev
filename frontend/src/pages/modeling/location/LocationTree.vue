<template>
  <div class="row q-pa-md top-search-wrap" style="padding: 12px 16px">
    <q-input
      v-model="locationTree.search"
      standout="bg-blue-grey-5"
      :placeholder="$t('위치 검색')"
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
      :icon="locationTree.isExpandAll ? 'mdi-collapse-all' : 'mdi-expand-all'"
      class="col-auto q-ml-sm"
    />
  </div>
  <q-card-section class="q-pt-sm overflow-auto">
    <q-tree
      v-if="treeItem.length > 0"
      ref="treeRef"
      :nodes="treeItem"
      node-key="id"
      label-key="name"
      :filter="locationTree.search"
      class="selectable"
      selected-color="primary"
      no-selection-unset
      v-model:selected="locationTree.selected"
      v-model:expanded="locationTree.expanded"
      @update:expanded="onTreeExpanded"
      @update:selected="onTreeSelected"
    >
      <template #default-header="{ node }">
        <div class="flex items-center" @contextmenu.prevent="showTreeContextMenu($event, node)">
          <q-icon :name="getTreeIconByType(node.type).icon" :color="getTreeIconByType(node.type).color" size="sm" />
          <span class="q-ml-sm text-subtitle2">{{ node.name }}</span>
          <q-menu touch-position context-menu class="tree-context-menu">
            <q-list>
              <q-item
                v-for="(item, index) in treeContextMenu.menuItems"
                :key="index"
                clickable
                v-close-popup
                @click="handleContextMenuClick(item)"
              >
                <q-item-section>
                  <q-icon :color="getContextMenuIconColor(item)" :name="getContextMenuIconName(item)" size="25px" />
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
          {{ $t('등록된 위치정보가 없습니다.') }}
        </div>
        <q-btn
          v-if="treeItem.length === 0"
          color="secondary"
          :label="$t('사업장 등록')"
          :icon="getTreeIconByType(LOCATION_TYPE.SITE).icon"
          @click="
            handleContextMenuClick({
              label: t('사업장 추가'),
              actionType: CONTEXT_MENU_ACTION_TYPE.CREATE,
              target: LOCATION_TYPE.SITE,
            })
          "
        />
      </div>
    </div>
  </q-card-section>
</template>

<script setup>
import { reactive, ref, toRefs, watch } from 'vue'
import { useAuthStore } from 'stores/auth'
import _ from 'lodash'
import { useI18n } from 'vue-i18n'
import { CONTEXT_MENU_ACTION_TYPE, LOCATION_TYPE } from 'pages/modeling/location/location'

const TREE_ICON_STYLE = {
  [LOCATION_TYPE.SITE]: {
    icon: 'mdi-office-building',
    color: 'primary',
  },
  [LOCATION_TYPE.FAB]: {
    icon: 'mdi-factory',
    color: 'secondary',
  },
  [LOCATION_TYPE.LINE]: {
    icon: 'mdi-account-network',
    color: 'green',
  },
}

const props = defineProps({
  treeItem: {
    type: Array,
    required: true,
    default: () => [],
  },
})

const { t } = useI18n()
const { isPermitted } = useAuthStore()

const treeRef = ref(null)
const { treeItem } = toRefs(props)

const emit = defineEmits({
  'node-select': null,
  'context-menu-click': null,
})

const locationTree = reactive({
  isExpandAll: false,
  search: '',
  selected: [],
  expanded: [],
})

watch(
  () => treeItem.value,
  () => {
    locationTree.search = ''
    locationTree.selected = []
  },
)

const treeContextMenu = ref({
  show: false,
  x: 0,
  y: 0,
  menuItems: [],
})

const showTreeContextMenu = (e, node) => {
  treeContextMenu.value.x = e.x
  treeContextMenu.value.y = e.y
  const menuItems = []
  if (node.type === LOCATION_TYPE.SITE) {
    if (isPermitted('location', 'create')) {
      menuItems.push(
        {
          label: t('사업장 추가'),
          actionType: CONTEXT_MENU_ACTION_TYPE.CREATE,
          target: LOCATION_TYPE.SITE,
          item: node,
        },
        {
          label: t('공장 추가'),
          actionType: CONTEXT_MENU_ACTION_TYPE.CREATE,
          target: LOCATION_TYPE.FAB,
          item: node,
        },
      )
    }
  } else if (node.type === LOCATION_TYPE.FAB) {
    if (isPermitted('location', 'create')) {
      menuItems.push({
        label: t('라인 추가'),
        actionType: CONTEXT_MENU_ACTION_TYPE.CREATE,
        target: LOCATION_TYPE.LINE,
        item: node,
      })
    }
  }
  if (isPermitted('location', 'delete')) {
    menuItems.push({
      label: t('삭제'),
      actionType: CONTEXT_MENU_ACTION_TYPE.DELETE,
      item: node,
    })
  }
  treeContextMenu.value.menuItems = menuItems
  treeContextMenu.value.show = true
}

const onTreeExpanded = (expandedIds) => {
  locationTree.expanded = expandedIds
}

const handleContextMenuClick = (menuItem) => {
  emit('context-menu-click', {
    node: { ..._.cloneDeep(menuItem.item) },
    context: {
      actionType: menuItem.actionType,
      target: menuItem.target,
    },
  })
}

const getTreeIconByType = (type) => {
  return TREE_ICON_STYLE[type]
}

const getContextMenuIconColor = (contextMenuItem) => {
  const { actionType, target } = contextMenuItem
  if (actionType === CONTEXT_MENU_ACTION_TYPE.DELETE) {
    return 'blue-grey-8'
  }
  return getTreeIconByType(target).color
}

const getContextMenuIconName = (contextMenuItem) => {
  const { actionType, target } = contextMenuItem
  if (actionType === CONTEXT_MENU_ACTION_TYPE.DELETE) {
    return 'mdi-delete-forever'
  }
  return getTreeIconByType(target).icon
}

const toggleExpandTree = () => {
  if (locationTree.isExpandAll) {
    treeRef.value.collapseAll()
    locationTree.isExpandAll = !locationTree.isExpandAll
  } else {
    treeRef.value.expandAll()
    locationTree.isExpandAll = !locationTree.isExpandAll
  }
}

const onTreeSelected = (id) => {
  const currentNode = treeRef.value.getNodeByKey(id)
  const parentNode = treeRef.value.getNodeByKey(currentNode.parent)

  emit('node-select', {
    ..._.cloneDeep(currentNode),
    parentNode: {
      ..._.cloneDeep(parentNode),
    },
  })
}
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
