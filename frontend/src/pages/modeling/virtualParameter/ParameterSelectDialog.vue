<template>
  <q-dialog v-model="isShow" persistent style="height: 80vh" class="ParameterSelectDialogWrap">
    <q-card style="width: 1280px; max-width: 120vw">
      <q-separator />
      <q-card-section class="flex full-width table-section" style="flex: 1; overflow: auto">
        <q-table
          class="full-width"
          :title="$t('파라미터')"
          :rows="tableRows"
          :columns="parameterTableColumns"
          row-key="id"
          selection="multiple"
          v-model:selected="selectedParameters"
          @update:selected="handleUpdatedSelect"
          :filter="filter"
          :filter-method="filterMethod"
          :hide-bottom="true"
          :pagination="{
            rowsPerPage: 0,
          }"
        >
          <template v-slot:top-right>
            <q-input dense v-model="filter" :placeholder="$t('검색')">
              <template v-slot:append>
                <q-icon name="search" />
              </template>
            </q-input>
          </template>
        </q-table>
      </q-card-section>
      <q-separator />
      <q-card-actions class="cnt-flex-end">
        <q-btn flat color="negative" :label="$t('닫기')" @click="handleCancelButton" />
        <q-btn flat color="primary" :label="$t('선택')" @click="handleSelectButton" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup>
import { onBeforeMount, ref } from 'vue'
import ParameterService from 'src/services/modeling/ParameterService'
import { parameterTableColumnsDefinition } from 'src/pages/modeling/virtualParameter/virtualParameter'
import useUI from 'src/common/module/ui'
import { t } from 'src/plugins/i18n'

const props = defineProps({
  toolId: {
    type: Number,
    required: true,
  },
  selectedParameterIds: {
    type: Array,
    required: false,
    default: () => [],
  },
  requiredIncludeParameterIds: {
    type: Array,
    required: false,
    default: () => [],
  },
  listingExcludeIds: {
    type: Array,
    required: false,
    default: () => [],
  },
})

const { notify } = useUI()

const emit = defineEmits({
  close: () => true,
  ok: (selectedParameters) => selectedParameters,
})

const isShow = ref(true)
const tableRows = ref([])
const parameterTableColumns = parameterTableColumnsDefinition
const selectedParameters = ref([])
const filter = ref('')

const filterMethod = (rows, terms, cols, getCellValue) => {
  const q = (terms || '').toString().toLowerCase()
  const nameColumn = cols.find((col) => col.name === 'name')
  return rows.filter((row) => {
    const cellValue = (getCellValue(nameColumn, row) || '').toString().toLowerCase()
    return cellValue.includes(q)
  })
}

const handleUpdatedSelect = (updatedSelectedParameters) => {
  if (props.requiredIncludeParameterIds.length > 0) {
    const parameterIds = updatedSelectedParameters.map((parameter) => parameter.id)
    const notIncludeRequiredParameterIds = props.requiredIncludeParameterIds.filter((id) => !parameterIds.includes(id))
    if (notIncludeRequiredParameterIds.length > 0) {
      notify.warning(t('Blockly에서 사용중인 파라미터는 필수로 선택되어야 합니다.'))
      const requireParameters = tableRows.value.filter((parameter) =>
        notIncludeRequiredParameterIds.includes(parameter.id),
      )
      selectedParameters.value = [...updatedSelectedParameters, ...requireParameters]
    }
  }
}

const handleCancelButton = () => {
  emit('close')
}

const handleSelectButton = () => {
  emit('ok', selectedParameters.value)
}

const init = async () => {
  const parameters = await ParameterService.getDcpAssignedParametersByToolId(props.toolId)
  tableRows.value = parameters.filter((parameter) => !props.listingExcludeIds.includes(parameter.id))

  if (props.selectedParameterIds.length > 0) {
    const isIncludeSelectedParameterIds = (parameter) => props.selectedParameterIds.includes(parameter.id)
    selectedParameters.value = tableRows.value.filter(isIncludeSelectedParameterIds)
  }
}

onBeforeMount(() => {
  init()
})
</script>

<style lang="scss" scoped>
.table-section {
  max-height: 60vh !important;
}
.ParameterSelectDialogWrap {
  .q-card__section:deep {
    padding: 0;
    .q-table__top {
      height: 64px;
      border-bottom: 1px solid #ddd;
      .q-table__control {
        label.q-field .q-field__inner {
          display: flex;
          align-items: flex-end;
        }
        label.q-field .q-field__inner .q-field__control {
          width: 100%;
          height: 30px;
          min-height: 30px;
          padding: 0 12px;
          background-color: #fff;
          border: 1px solid #ddd;
          border-radius: 4px;
          justify-content: space-between;
          gap: 10px;

          > div {
            height: 100%;
            padding: 0;
          }
          .q-field__control-container .q-field__native {
            min-height: unset;
            padding: 0;
            text-overflow: ellipsis;
            white-space: nowrap;
            overflow: hidden;
            flex-wrap: nowrap;
          }

          .q-field__prepend + .q-field__control-container .q-field__label {
            left: -24px;
          }

          .q-field__append {
          }

          .q-icon {
            font-size: 16px;
          }

          &::before {
            background-color: #fff;
            border: none;
          }

          &::after {
            background-color: var(--mainColor);
          }
        }
      }
    }
    .q-table__middle {
      padding: 12px 16px;
    }
  }
}
</style>
