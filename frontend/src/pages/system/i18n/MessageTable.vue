<template>
  <q-table
    class="table sticky-header"
    flat
    bordered
    :rows="rows"
    :columns="columns"
    row-key="id"
    :filter="filter"
    :hide-bottom="true"
    :pagination="{
      rowsPerPage: 0,
    }"
    v-model:selected="selectedRows"
    selection="multiple"
    separator="vertical"
  >
    <template v-slot:top-right>
      <q-input class="search-input" filled dense v-model="filter" input-class="text-white" :placeholder="$t('찾기')">
        <template v-slot:append>
          <q-icon color="white" name="search" />
        </template>
      </q-input>
    </template>

    <template v-slot:header-cell="props">
      <q-th :key="props.col.name" :props="props"> {{ $t(props.col.label) }} </q-th>
    </template>

    <template v-slot:body-cell="props">
      <q-td :props="props">
        <q-input
          :disable="!isEditableCell(props)"
          v-model="props.row[props.col.name]"
          :input-class="`text-${props.col.align}`"
          placeholder="please input"
          dense
          borderless
          @update:modelValue="handleInputChange(props)"
        />
      </q-td>
    </template>
  </q-table>
</template>

<script setup>
import { ref, toRefs, watch } from 'vue'
import { PARAMETER_KEY_PREFIX, t } from 'src/plugins/i18n'
import { useAuthStore } from 'src/stores/auth'

const KEY_COLUMN_FIELD_NAME = 'key'
const { isPermitted } = useAuthStore()

// 권한 확인
const hasCreatePermission = isPermitted('i18n', 'create')
const hasUpdatePermission = isPermitted('i18n', 'update')

const props = defineProps({
  languages: {
    type: Array,
    default: () => [],
  },
  messages: {
    type: Object,
    require: true,
  },
})

const createColumns = (languages) => {
  const dynamicColumns = languages.map((lang) => {
    return {
      name: lang,
      align: 'center',
      label: lang,
      field: lang,
    }
  })

  return [
    {
      name: 'key',
      required: true,
      label: t('키'),
      field: KEY_COLUMN_FIELD_NAME,
      align: 'center',
    },
    ...dynamicColumns,
  ]
}

const { languages, messages } = toRefs(props)

const columns = ref(createColumns(languages.value))
const rows = ref([])
const selectedRows = ref([])

const filter = ref('')
const emit = defineEmits(['update:rows', 'update:messages', 'update:selectedRows'])

watch(languages, (newLanguages) => {
  columns.value = createColumns(newLanguages)
})

watch(rows, (newRows) => {
  emit('update:rows', newRows)
})

watch(messages, (newMessages) => {
  emit('update:messages', newMessages)
})

watch(selectedRows, (newSelectedRows) => {
  emit('update:selectedRows', newSelectedRows)
})

const isEditableCell = (props) => {
  const { field } = props.col
  const { id, isNew } = props.row

  if (isNew) {
    return hasCreatePermission
  }

  if (!hasUpdatePermission) {
    return false
  }

  if (field === KEY_COLUMN_FIELD_NAME && id.startsWith(PARAMETER_KEY_PREFIX)) {
    return false
  }
  return field !== KEY_COLUMN_FIELD_NAME
}

const handleInputChange = (props) => {
  const isNew = props.row.isNew
  if (!isNew) {
    const original = messages.value[props.row.key]
    props.row.isEdited = languages.value.some((lang) => props.row[lang] !== original[lang])
  }
}
</script>

<style lang="scss" scoped>
.table {
  height: 100%;
}

.sticky-header:deep {
  .q-table__top,
  .q-table__bottom,
  thead tr:first-child th {
    /* bg color is important for th; just specify one */
    background-color: $primary;
  }

  thead tr th {
    position: sticky;
    z-index: 1;
  }

  /* this will be the loading indicator */
  thead tr:last-child th {
    /* height of all previous header rows */
    top: 48px;
  }

  thead tr:first-child th {
    top: 0;
  }

  /* prevent scrolling behind sticky top row on focus */
  tbody {
    /* height of all previous header rows */
    scroll-margin-top: 48px;
  }
}
.search-input {
  width: 300px;
}
</style>
