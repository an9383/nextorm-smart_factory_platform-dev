<!-- eslint-disable no-unused-vars -->
<template>
  <q-page class="q-pa-sm">
    <q-card class="cust_subPage">
      <q-card-section class="row search_section_wrap">
        <q-item class="col-2 search_left">
          <q-item-section>
            <ToolSelectBox v-model="selectedTool" @update:model-value="handleToolChange" />
          </q-item-section>
        </q-item>
        <q-item class="col-8 search_right">
          <q-item-section>
            <q-btn
              v-permission:dcpConfig.create
              @click="clickKafkaModify"
              class="add_btn with_icon_btn sBtn secondary"
              >{{ $t('카프가 설정') }}</q-btn
            >
            <q-btn v-permission:dcpConfig.create @click="clickCreate" class="add_btn with_icon_btn sBtn secondary">{{
              $t('추가')
            }}</q-btn>
            <q-btn
              v-permission:dcpConfig.update
              :disabled="selected == null || selected.length == 0"
              @click="clickModify"
              class="edit_btn with_icon_btn sBtn secondary"
              >{{ $t('수정') }}</q-btn
            >
            <q-btn
              v-permission:dcpConfig.delete
              :disabled="selected == null || selected.length == 0"
              @click="clickDelete"
              class="delete_btn with_icon_btn sBtn secondary"
              >{{ $t('삭제') }}</q-btn
            >
            <DcpConfigModify ref="dcpConfigModify" @close="onCloseModify"></DcpConfigModify>
            <DcpConfigKafkaModify v-if="toolKafkaDialog" :tool-id="selectedTool.id" @close="onToolKafkaDialogClose" />
          </q-item-section>
        </q-item>
      </q-card-section>
      <simple-table
        v-model:selected="selected"
        :rows="rows"
        :columns="columns"
        :pagination="{ rowsPerPage: 10 }"
        row-key="id"
        color="amber"
        selection="single"
        bordered
      >
        <template v-slot:body="props">
          <q-tr class="cursor-pointer" :props="props" @click="props.selected = !props.selected">
            <q-td v-for="col in props.cols" :key="col.name" :props="props">
              {{ col.value }}
            </q-td>
          </q-tr>
        </template>
      </simple-table>
    </q-card>
  </q-page>
</template>

<script setup>
import { onBeforeMount, ref } from 'vue'
import { date } from 'quasar'
import DcpConfigModify from 'src/pages/modeling/dcpConfig/DcpConfigModify.vue'
import DcpConfigService from 'src/services/modeling/DcpConfigService.js'
import useUI from 'src/common/module/ui'
import { t } from 'src/plugins/i18n'
import ToolSelectBox from 'components/form/ToolSelectBox.vue'
import SimpleTable from 'components/common/SimpleTable.vue'
import MetaDataService from 'src/services/modeling/MetaDataService'
import DcpConfigKafkaModify from 'pages/modeling/dcpConfig/DcpConfigKafkaModify.vue'
const ui = useUI()
const toolKafkaDialog = ref(false)
const onToolKafkaDialogClose = () => {
  toolKafkaDialog.value = false
}
const dcpConfigModify = ref(null, null)
const collectorTypeMap = ref({})

const columns = ref([
  { name: 'id', align: 'left', label: t('Id'), field: 'id' },
  { name: 'toolId', align: 'left', label: t('설비'), field: 'toolName' },
  {
    name: 'collectorType',
    align: 'left',
    label: t('수집 유형'),
    field: 'collectorType',
    format: (value) => {
      return collectorTypeMap.value[value]
    },
  },
  { name: 'command', align: 'left', label: t('Command'), field: 'command' },
  {
    name: 'dataInterval',
    align: 'left',
    label: t('데이터 수집 주기(초)'),
    field: 'dataInterval',
  },
  { name: 'createBy', align: 'left', label: t('생성자'), field: 'createBy' },
  {
    name: 'createAt',
    align: 'left',
    label: t('생성 일시'),
    field: 'createAt',
    format: (value) => {
      if (value) {
        return date.formatDate(value, 'YYYY-MM-DD HH:mm:ss')
      }
    },
  },
])

const rows = ref([])
const selected = ref([])
const selectedTool = ref()

const getDcpConfigsAndSetRows = async (toolId) => {
  rows.value = await DcpConfigService.getDcpConfigsByTool(toolId)
}

const handleToolChange = async (newTool) => {
  await getDcpConfigsAndSetRows(newTool.id)
  selected.value = []
}

const clickModify = async () => {
  if (selected.value.length > 0) {
    const id = selected.value[0].id
    try {
      const data = await DcpConfigService.getDcpConfigById(id)
      dcpConfigModify.value.open(data, null)
    } catch (error) {
      ui.notify.error('데이터를 가져오는 중 에러가 발생하였습니다.')
    }
  }
}
const clickKafkaModify = () => {
  if (selectedTool.value) {
    toolKafkaDialog.value = true
  } else {
    ui.notify.error('설정 가능한 설비가 없습니다.')
  }
}
const clickCreate = () => {
  dcpConfigModify.value.open(null, selectedTool)
}
const clickDelete = () => {
  ui.confirm(t('삭제'), t('선택된 항목을 삭제 하시겠습니까?'), t('삭제'), t('취소')).onOk(deleteDcpConfig)
}
const onCloseModify = async (value) => {
  if (value === 'ok') {
    await getDcpConfigsAndSetRows(selectedTool.value.id)
  }
}
const deleteDcpConfig = async () => {
  try {
    await DcpConfigService.deleteDcpConfig(selected.value[0].id)
    ui.notify.success(t('삭제 되었습니다.'))
    await getDcpConfigsAndSetRows(selectedTool.value.id)
  } catch (error) {
    error.response?.data.extraData.datas.forEach((message) => {
      if (error.response.data.code === 'ERROR_CONSTRAINT_VIOLATION') {
        ui.notify.error(message.path) //임시
      }
    })
  }
}

onBeforeMount(async () => {
  const collectorTypes = await MetaDataService.getCollectorTypes()
  collectorTypeMap.value = collectorTypes.reduce((acc, cur) => {
    acc[cur.type] = cur.displayName
    return acc
  }, {})
})
</script>
