<template>
  <q-page class="q-pa-sm">
    <q-card class="cust_subPage">
      <q-card-section class="row search_section_wrap">
        <q-item class="col-2 search_left">
          <!-- <span class="text-h5 text-weight-bold q-mt-sm">{{ $t('가상 파라미터') }}</span> -->
          <q-item-section>
            <filterable-select
              v-model="selectedTool"
              :options="toolList"
              option-value="id"
              option-label="name"
              :label="$t('설비')"
              @update:model-value="handleSelectChange"
            />
          </q-item-section>
        </q-item>
        <q-item class="col-8 search_right">
          <q-item-section>
            <div class="row items-center no-wrap q-gutter-x-sm q-pa-sm">
              <q-input
                v-model="searchText"
                dense
                outlined
                clearable
                :placeholder="$t('파라미터명, 화면표시')"
                style="width: 280px"
              >
                <template v-slot:append>
                  <q-icon name="search" class="text-grey-7" style="pointer-events: none" />
                </template>
              </q-input>
              <q-btn
                v-permission:virtualParameter.create
                @click="handleClickButtonClick"
                class="add_btn with_icon_btn sBtn secondary"
              >
                {{ $t('추가') }}
              </q-btn>
              <q-btn
                v-permission:virtualParameter.update
                class="edit_btn with_icon_btn sBtn secondary"
                :disabled="selectedParameter == null"
                @click="handleModifyButtonClick"
                >{{ $t('수정') }}</q-btn
              >
              <q-btn
                v-permission:virtualParameter.delete
                class="delete_btn with_icon_btn sBtn secondary"
                :disabled="selectedParameter == null"
                @click="handleDeleteButtonClick"
                >{{ $t('삭제') }}</q-btn
              >
              <VirtualParameterDialog
                ref="parameterDialog"
                @save-success="handleParameterDialogSaveSuccess"
                :selected-tool-id="selectedTool?.id"
              />
            </div>
          </q-item-section>
        </q-item>
      </q-card-section>
      <simple-table
        bordered
        :rows="rows"
        :columns="columns"
        :pagination="{ rowsPerPage: 10 }"
        :filter="searchText"
        :filter-method="tableFilterMethod"
        row-key="id"
        color="amber"
        selection="single"
        v-model:selected="selectedRow"
      >
        <template v-slot:body="props">
          <q-tr class="cursor-pointer" :props="props" @click="props.selected = !props.selected">
            <q-td v-for="col in props.cols" :key="col.name" :props="props">
              {{ col.name === 'createAt' ? date.formatDate(col.value, 'YYYY-MM-DD HH:mm:ss') : col.value }}
            </q-td>
          </q-tr>
        </template>
      </simple-table>
    </q-card>
  </q-page>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { date } from 'quasar'
import _ from 'lodash'
import ParameterService from '/src/services/modeling/ParameterService'
import ToolService from '/src/services/modeling/ToolService'
import useUI from 'src/common/module/ui'
import { pt, t } from '/src/plugins/i18n'
import VirtualParameterDialog from 'pages/modeling/virtualParameter/VirtualParameterDialog.vue'

const ui = useUI()

const parameterDialog = ref(null)
const toolList = ref()

const columns = ref([
  { name: 'id', align: 'left', label: t('Id'), field: 'id' },
  {
    name: 'toolId',
    align: 'left',
    label: t('설비'),
    field: 'toolName',
  },
  { name: 'name', align: 'left', label: t('파라미터명'), field: 'name', sortable: true },
  {
    name: 'displayName',
    align: 'left',
    label: t('화면 표시'),
    field: 'name',
    sortable: true,
    format: (val, row) => pt(row.name),
  },
  { name: 'type', align: 'left', label: t('타입'), field: 'type' },
  { name: 'dataType', align: 'left', label: t('데이터 타입'), field: 'dataType' },
  { name: 'order', align: 'left', label: t('순서'), field: 'order' },
  { name: 'target', align: 'left', label: t('TARGET'), field: 'target' },
  { name: 'lsl', align: 'left', label: t('LSL'), field: 'lsl' },
  { name: 'lcl', align: 'left', label: t('LCL'), field: 'lcl' },
  { name: 'ucl', align: 'left', label: t('UCL'), field: 'ucl' },
  { name: 'usl', align: 'left', label: t('USL'), field: 'usl' },
  { name: 'createBy', align: 'left', label: t('생성자'), field: 'createBy' },
  { name: 'createAt', align: 'left', label: t('생성일시'), field: 'createAt' },
])

const rows = ref([])
const selectedRow = ref([])
const selectedTool = ref([])
const searchText = ref('')

const selectedParameter = computed(() => selectedRow.value[0] || null)

// 검색어 커스텀 필터 메서드: 파라미터명/화면표시명 대상으로 검색
const tableFilterMethod = (rowsParam, terms) => {
  const q = (terms || '').toString().trim().toLowerCase()
  if (!q) return rowsParam
  return rowsParam.filter((row) => {
    const name = (row.name || '').toString().toLowerCase()
    const displayName = ((pt(row.name || '') || row.name || '') + '').toLowerCase()
    return name.includes(q) || displayName.includes(q)
  })
}

const initTool = async () => {
  if (_.isEmpty) {
    selectedTool.value = toolList.value[0]
    await getVirtualParametersByToolId(selectedTool.value.id)
  }
}
const getToolList = async () => {
  toolList.value = await ToolService.getTools()
}

const handleSelectChange = async (selectTool) => {
  selectedRow.value = []
  await getVirtualParametersByToolId(selectTool.id)
}

const getVirtualParametersByToolId = async (toolId) => {
  rows.value = await ParameterService.getParameters({ toolId, isVirtual: true })
}

const handleClickButtonClick = () => {
  parameterDialog.value.showCreate(selectedTool)
}

const handleModifyButtonClick = () => {
  const parameterId = selectedParameter.value.id
  parameterDialog.value.showModify(parameterId)
}

const handleDeleteButtonClick = () => {
  const onOkCallback = async () => {
    await deleteParameter(selectedParameter.value.id)
  }
  ui.confirm(t('삭제'), '선택된 항목을 삭제 하시겠습니까?', t('삭제'), t('취소')).onOk(onOkCallback)
}

const deleteParameter = async (parameterId) => {
  try {
    await ParameterService.deleteParameter(parameterId)
    ui.notify.success(t('삭제 되었습니다.'))
  } catch (error) {
    error.response.data.extraData.datas.forEach((message) => {
      if (error.response.data.code === 'ERROR_CONSTRAINT_VIOLATION') {
        ui.notify.error(message.path) //임시
      }
    })
  }
  await getVirtualParametersByToolId(selectedTool.value.id)
}

const handleParameterDialogSaveSuccess = async () => {
  await getVirtualParametersByToolId(selectedTool.value.id)
}

onMounted(async () => {
  await getToolList()
  initTool()
})
</script>

<style scoped>
#blocklyDiv {
  z-index: 2000;
}
</style>
