<template>
  <q-dialog v-model="modelValue" persistent>
    <q-card class="dialog-container">
      <q-card-section class="bg-secondary text-white">
        <div class="text-h6">{{ title }}</div>
      </q-card-section>
      <q-card-section>
        <q-form ref="form">
          <div class="row">
            <q-input
              v-model="inputData.name"
              :label="$t('권한명')"
              :rules="[$rules.required]"
              class="input-required col-4 q-pr-sm"
            />
            <q-input v-model="inputData.description" :label="$t('설명')" class="col-8 q-pl-sm" />
          </div>
          <q-table
            flat
            bordered
            :rows="flattenedRows"
            :columns="columns"
            row-key="name"
            :pagination="{
              page: 1,
              rowsPerPage: 0,
            }"
            hide-pagination
            separator="cell"
          >
            <template v-slot:body-cell-title="props">
              <q-td :props="props">
                <div :style="{ paddingLeft: props.row._level * 20 + 'px' }">
                  <q-icon
                    v-if="props.row._children?.length > 0"
                    :name="props.row._expanded ? 'remove_circle' : 'add_circle'"
                    @click="props.row._expanded = !props.row._expanded"
                    class="cursor-pointer q-mr-md"
                    size="xs"
                    color="primary"
                  />
                  <span :class="{ 'q-ml-md': !props.row._children?.length, 'text-body2': true }">
                    {{ props.row.title }}
                  </span>
                </div>
              </q-td>
            </template>
            <template v-slot:body-cell-all="props">
              <q-td :props="props">
                <q-checkbox
                  size="md"
                  :model-value="isAllChecked(props.row)"
                  @update:model-value="checkAllPermission(props.row, $event)"
                />
              </q-td>
            </template>
            <template v-slot:body-cell-create="props">
              <q-td :props="props">
                <q-checkbox
                  size="md"
                  :model-value="isChecked(props.row, 'create')"
                  @update:model-value="checkPermission(props.row, 'create', $event)"
                />
              </q-td>
            </template>
            <template v-slot:body-cell-update="props">
              <q-td :props="props">
                <q-checkbox
                  size="md"
                  :model-value="isChecked(props.row, 'update')"
                  @update:model-value="checkPermission(props.row, 'update', $event)"
                />
              </q-td>
            </template>
            <template v-slot:body-cell-delete="props">
              <q-td :props="props">
                <q-checkbox
                  size="md"
                  :model-value="isChecked(props.row, 'delete')"
                  @update:model-value="checkPermission(props.row, 'delete', $event)"
                />
              </q-td>
            </template>
            <template v-slot:body-cell-view="props">
              <q-td :props="props">
                <q-checkbox
                  size="md"
                  :model-value="isChecked(props.row, 'view')"
                  @update:model-value="checkPermission(props.row, 'view', $event)"
                  :disable="isViewDisabled(props.row)"
                />
              </q-td>
            </template>
          </q-table>
        </q-form>
      </q-card-section>
      <q-separator />
      <q-card-actions class="justify-between">
        <span class="field-required-indicate">{{ $t('필수 입력 필드') }}</span>
        <div>
          <q-btn flat color="negative" :label="$t('닫기')" @click="close" />
          <q-btn flat color="primary" :label="$t('저장')" @click="saveRole" />
        </div>
      </q-card-actions>
      <inner-loading v-model="isLoading" />
    </q-card>
  </q-dialog>
</template>

<script setup>
import { computed, defineEmits, onMounted, ref } from 'vue'
import { t } from '/src/plugins/i18n'
import useUI from 'src/common/module/ui'
import allMenus from '/src/common/constant/menu.js'
import _ from 'lodash'
import RoleService from 'src/services/system/RoleService'

const { roleId } = defineProps({
  roleId: {
    type: Number,
    default: null,
  },
})
const ui = useUI()
const isLoading = ref(false)
const emit = defineEmits(['save'])
const form = ref()

const title = computed(() => (roleId ? t('권한 수정') : t('권한 등록')))

const inputData = ref({ name: '', description: '' })

const columns = [
  { name: 'title', label: t('메뉴'), align: 'center', field: 'id', classes: 'text-left col-6', style: 'width: 40%' },
  { name: 'all', label: t('전체'), align: 'center', field: 'all', style: 'width: 12%' },
  { name: 'create', label: t('생성'), align: 'center', field: 'create', style: 'width: 12%' },
  { name: 'update', label: t('수정'), align: 'center', field: 'create', style: 'width: 12%' },
  { name: 'delete', label: t('삭제'), align: 'center', field: 'create', style: 'width: 12%' },
  { name: 'view', label: t('읽기'), align: 'center', field: 'create', style: 'width: 12%' },
]
const rows = ref([])

const flattenedRows = computed(() => {
  const flatten = (rows, level = 0) => {
    return rows.reduce((acc, row) => {
      row._level = level
      acc.push(row)
      if (row._expanded === true && row._children?.length > 0) {
        acc = acc.concat(flatten(row._children, level + 1))
      }
      return acc
    }, [])
  }
  return flatten(rows.value)
})

const initData = () => {
  if (roleId) {
    loadRole(roleId)
  } else {
    rows.value = generateMenuData([
      {
        id: 'root',
        title: 'root',
        children: _.cloneDeep(allMenus),
      },
    ])
  }
}

onMounted(initData)

const generateMenuData = (menus, parentId) => {
  return menus.map((item) => {
    const { id, title, create, update, view } = item
    const _delete = item.delete

    const menuData = {
      id: id,
      title,
      parentId,
      _expanded: false,
    }

    if (item.children?.length > 0) {
      const children = item.children.map((child) => ({ create, update, view, delete: _delete, ...child }))
      menuData._children = generateMenuData(children, id)
      menuData._expanded = false
    } else {
      menuData.create = create
      menuData.update = update
      menuData.delete = _delete
      menuData.view = view
    }

    return menuData
  })
}

const close = () => {
  emit('update:modelValue', false)
}

/**
 * 메뉴의 모든 권한이 체크 되어있는지 여부
 * @param {*} row
 */
const isAllChecked = (row) => {
  const checkedCount = [
    isChecked(row, 'create'),
    isChecked(row, 'update'),
    isChecked(row, 'delete'),
    isChecked(row, 'view'),
  ].filter((v) => v === true).length
  if (checkedCount === 4) {
    return true
  } else if (checkedCount > 0) {
    return null
  } else {
    return false
  }
}

/**
 * View 권한 체크박스 disable 여부
 * @param {*} row
 */
const isViewDisabled = (row) => {
  return (
    //다른 권한이 한개라도 있는 경우 view 권한은 disable
    isChecked(row, 'create') ||
    isChecked(row, 'update') ||
    isChecked(row, 'delete') ||
    //자식 메뉴의 View 권한이 있는 경우 부모 메뉴의 view 권한은 disable 처리
    row._children?.find((child) => isChecked(child, 'view')) !== undefined
  )
}

/**
 * 메뉴 권한 체크박스 체크 여부
 * @param {*} row
 * @param {*} type "create", "update", "delete", "view"
 */
const isChecked = (row, type) => {
  if (row._children?.length > 0) {
    //자식 메뉴가 있는 경우
    const checkedChildren = row._children.filter((child) => isChecked(child, type) === true)
    if (checkedChildren?.length === row._children?.length) {
      // 자식메뉴가 모두 체크된 경우만 checked
      return true
    } else if (
      checkedChildren?.length > 0 ||
      row._children.filter((child) => isChecked(child, type) === null).length > 0
    ) {
      //자식 메뉴가 하나 이상 체크된 경우 indeterminate
      return null
    } else {
      //자식 메뉴가 모두 체크되지 않은 경우 unchecked
      return false
    }
  } else {
    //자식 메뉴가 없는 경우 자기 자신의 체크여부 판단
    return row[type] === true
  }
}

/**
 * 메뉴의 모든 권한 체크 처리
 * @param {*} row
 * @param {*} isChecked
 */
const checkAllPermission = (row, isChecked) => {
  checkPermission(row, 'create', isChecked)
  checkPermission(row, 'update', isChecked)
  checkPermission(row, 'delete', isChecked)
  checkPermission(row, 'view', isChecked)
}

/**
 * 메뉴의 특정 권한 체크 처리
 * @param {*} row
 * @param {*} type "create", "update", "delete", "view"
 * @param {*} isChecked
 */
const checkPermission = (row, type, isChecked) => {
  if (row._children?.length > 0) {
    //자식 메뉴가 있는 경우 모든 자식 메뉴의 권한을 check/uncheck 한다.
    row._children.forEach((child) => {
      checkPermission(child, type, isChecked)
    })
  } else {
    row[type] = isChecked
    if (type !== 'view' && isChecked === true) {
      //create, update, delete 권한이 부여되는 경우 view 권한은 자동 부여
      row.view = true
    }
  }
}

const organizeMenuPermission = () => {
  const organize = (item) => {
    const isCreateChecked = isChecked(item, 'create') === true
    const isUpdateChecked = isChecked(item, 'update') === true
    const isDeleteChecked = isChecked(item, 'delete') === true
    const isViewChecked = isChecked(item, 'view') === true
    const isAllChecked = isCreateChecked && isUpdateChecked && isDeleteChecked && isViewChecked

    return {
      id: item.id,
      children: isAllChecked ? [] : item._children?.map((child) => organize(child)) || [],
      create: isCreateChecked,
      update: isUpdateChecked,
      delete: isDeleteChecked,
      view: isViewChecked,
    }
  }
  return organize(rows.value[0])
}

const loadRole = async (id) => {
  isLoading.value = true
  try {
    const role = await RoleService.getRole(id)
    inputData.value = {
      name: role.name,
      description: role.description,
    }
    rows.value = generateMenuData(
      _.merge(
        [
          {
            id: 'root',
            title: 'root',
            children: _.cloneDeep(allMenus),
          },
        ],
        [JSON.parse(role.permission).menu],
      ),
    )
  } finally {
    isLoading.value = false
  }
}

const saveRole = async () => {
  if (!(await form.value.validate())) {
    ui.notify.warning(t('유효하지 않은 입력값이 있습니다.'))
    return
  }
  const request = {
    ...inputData.value,
    permission: JSON.stringify({ menu: organizeMenuPermission() }),
  }
  try {
    isLoading.value = true
    if (roleId) {
      await RoleService.modifyRole(roleId, { ...request })
    } else {
      await RoleService.createRole(request)
    }
    ui.notify.success(t('저장 되었습니다.'))
    emit('save')
    close()
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped lang="scss">
.dialog-container {
  width: 900px;
  max-width: 900px;
}

:deep(.q-table) {
  th {
    font-weight: bold;
  }
}

.q-checkbox {
  :deep(.q-checkbox__inner--indet) {
    .q-checkbox__bg {
      color: var(--q-secondary);
      background-color: var(--q-secondary);
    }
  }
}
</style>
