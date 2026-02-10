<template>
  <q-markup-table class="q-mb-md q-markup-table list-container">
    <draggable
      v-model="userPrompts"
      item-key="id"
      tag="tbody"
      :animation="200"
      ghost-class="draggable-ghost"
      handle=".drag-icon"
      @end="onDragEnd"
    >
      <template #item="{ element }">
        <tr
          :class="`list-group-item ${editingId === element.id ? 'editing' : ''} ${
            selectedId === element.id ? 'selected' : ''
          }`"
          @click="onSelectItem(element.id)"
        >
          <td class="icon-cell">
            <q-icon name="drag_handle" class="drag-icon" size="sm" />
          </td>
          <td class="content-cell">
            <div v-if="editingId !== element.id">
              {{ $t(element.message) }}
            </div>
            <q-input v-else v-model="element.message" dense @blur="stopEditing" @keyup.enter="stopEditing" />
          </td>
          <td class="icon-cell">
            <q-icon name="delete" color="red" class="delete-icon" @click="confirmDelete(element.id)" />
          </td>
        </tr>
      </template>
    </draggable>
  </q-markup-table>
</template>

<script setup>
import { ref, defineModel } from 'vue'
import useUI from 'src/common/module/ui'
import { t } from '/src/plugins/i18n'
import draggable from 'vuedraggable'

const ui = useUI()

const userPrompts = defineModel('userPrompts', {
  type: Array,
  required: true,
  default: () => [],
})

const emit = defineEmits(['removedIds'])

const selectedId = ref(null)
const editingId = ref(null)

const confirmDelete = (id) => {
  ui.confirm(t('삭제'), t('선택된 항목을 삭제 하시겠습니까?'), t('삭제'), t('취소')).onOk(() => {
    const index = userPrompts.value.findIndex((item) => item.id === id)
    if (index !== -1) {
      const [removed] = userPrompts.value.splice(index, 1)
      if (typeof removed.id === 'number') {
        emit('removedIds', removed.id)
      }
    }
  })
}

const onDragEnd = () => {
  userPrompts.value = userPrompts.value.map((item, index) => ({
    ...item,
    sort: index + 1,
  }))
}

const onSelectItem = (id) => {
  selectedId.value = id
  startEditing(id)
}

const startEditing = (id) => {
  editingId.value = id
}

const stopEditing = () => {
  editingId.value = null
}
</script>

<style scoped>
.q-markup-table {
  tr {
    transition: background-color 0.2s;
  }
  td {
    padding: 8px;
    vertical-align: middle;
  }
}

.list-container {
  margin-top: 16px;
  max-height: 200px;
  overflow-y: auto;
}

.icon-cell {
  width: 48px;
  text-align: center;
  cursor: pointer;
}

.content-cell {
  width: 100%;
  word-break: break-word;
}

.list-group-item.selected {
  background-color: #e0e0e0;
}

.list-group-item.editing {
  background-color: #f0f8ff;
}

.drag-icon {
  cursor: move;
}

.draggable-ghost {
  opacity: 0.5;
  background: var(--q-grey-5);
}

.delete-icon {
  cursor: pointer;
}
</style>
