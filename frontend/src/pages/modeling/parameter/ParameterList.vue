<template>
  <q-page class="q-pa-sm">
    <q-card class="cust_subPage">
      <q-card-section class="row search_section_wrap">
        <q-item class="col-2 search_left">
          <!-- <span class="text-h5 text-weight-bold q-mt-sm">{{ $t('파라미터') }}</span> -->
          <q-item-section>
            <ToolSelectBox v-model="selectedTool" @update:model-value="getParametersByTool" />
          </q-item-section>
        </q-item>
        <q-item class="col-8 search_right">
          <q-item-section>
            <div class="row items-center no-wrap q-gutter-x-sm">
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
              <q-btn v-permission:parameter.create @click="clickCopytoOtherTool" class="copy_btn with_icon_btn sBtn">{{
                $t('설비에서 복사')
              }}</q-btn>
              <ParameterCopy
                v-if="showParameterCopy"
                :copyToolId="copyToolId"
                @close="showParameterCopy = false"
              ></ParameterCopy>
              <q-btn v-permission:parameter.create @click="clickCreate" class="add_btn with_icon_btn sBtn secondary">{{
                $t('추가')
              }}</q-btn>
              <q-btn
                v-permission:parameter.update
                class="edit_btn with_icon_btn sBtn secondary"
                :disabled="selected == null || selected.length == 0"
                @click="clickModify"
                >{{ $t('수정') }}</q-btn
              >
              <q-btn
                v-permission:parameter.delete
                class="delete_btn with_icon_btn sBtn secondary"
                :disabled="selected == null || selected.length == 0"
                @click="clickDelete"
                >{{ $t('삭제') }}</q-btn
              >
              <parameter-modify ref="parameterModify" @close="onCloseModify"></parameter-modify>
            </div>
          </q-item-section>
        </q-item>
      </q-card-section>
      <simple-table
        bordered
        :rows="rows"
        :columns="columns"
        :filter="searchText"
        :filter-method="tableFilterMethod"
        row-key="name"
        color="amber"
        selection="single"
        v-model:selected="selected"
      >
        <template v-slot:body="props">
          <q-tr class="cursor-pointer" :props="props" @click="props.selected = !props.selected">
            <q-td v-for="col in props.cols" :key="col.name" :props="props">
              {{ col.name == 'createAt' ? date.formatDate(col.value, 'YYYY-MM-DD HH:mm:ss') : col.value }}
            </q-td>
          </q-tr>
        </template>
      </simple-table>
    </q-card>
  </q-page>
</template>

<script setup>
import { ref } from 'vue'
import { date } from 'quasar'
// eslint-disable-next-line no-unused-vars
import ParameterModify from 'src/pages/modeling/parameter/ParameterModify.vue'
import ParameterCopy from 'src/pages/modeling/parameter/ParameterCopy.vue'
import ParameterService from 'src/services/modeling/ParameterService'
import useUI from 'src/common/module/ui'
import { pt, t } from 'src/plugins/i18n'
import ToolSelectBox from 'components/form/ToolSelectBox.vue'

const ui = useUI()

const parameterModify = ref(null, null)

const showParameterCopy = ref(false)

const copyToolId = ref(null)

const columns = ref([
  { name: 'id', align: 'left', label: t('Id'), field: 'id' },
  {
    name: 'toolId',
    align: 'left',
    label: t('설비명'),
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
  { name: 'unit', align: 'left', label: t('단위'), field: 'unit' },
  { name: 'type', align: 'left', label: t('타입'), field: 'type' },
  { name: 'dataType', align: 'left', label: t('데이터 타입'), field: 'dataType' },
  { name: 'metaValue', align: 'left', label: t('메타데이터'), field: 'metaValue' },
  { name: 'target', align: 'left', label: t('TARGET'), field: 'target' },
  { name: 'lsl', align: 'left', label: t('LSL'), field: 'lsl' },
  { name: 'lcl', align: 'left', label: t('LCL'), field: 'lcl' },
  { name: 'ucl', align: 'left', label: t('UCL'), field: 'ucl' },
  { name: 'usl', align: 'left', label: t('USL'), field: 'usl' },
  { name: 'createBy', align: 'left', label: t('생성자'), field: 'createBy' },
  { name: 'createAt', align: 'left', label: t('생성일시'), field: 'createAt' },
])

const rows = ref([])
const selected = ref([])
const selectedTool = ref(null)
const searchText = ref('')

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

const getParametersByTool = async (tool) => {
  const { id } = tool
  rows.value = await ParameterService.getParameters({ toolId: id })
  selected.value = []
}

const clickCopytoOtherTool = () => {
  copyToolId.value = selectedTool.value.id
  showParameterCopy.value = true
}

const clickModify = async () => {
  let data = null
  if (selected.value.length > 0) {
    data = selected.value[0]
  }
  await parameterModify.value.open(data, null)
}
const clickCreate = () => {
  parameterModify.value.open(null, selectedTool)
}
const clickDelete = () => {
  ui.confirm(t('삭제'), t('선택된 항목을 삭제 하시겠습니까?'), t('삭제'), t('취소')).onOk(deleteParameter)
}
const onCloseModify = async (value) => {
  if (value == 'ok') {
    await getParametersByTool(selectedTool.value)
  }
}
const deleteParameter = async () => {
  try {
    await ParameterService.deleteParameter(selected.value[0].id)
    ui.notify.success(t('삭제 되었습니다.'))
  } catch (error) {
    error.response.data.extraData.datas.forEach((message) => {
      if (error.response?.data.code === 'ERROR_CONSTRAINT_VIOLATION') {
        ui.notify.error(message.path) //임시
      }
    })
  }
  await getParametersByTool(selectedTool.value)
}
</script>
