<template>
  <q-page class="q-pa-sm">
    <q-card class="cust_subPage">
      <q-card-section class="row search_section_wrap">
        <q-item class="col-2 search_left">
          <!-- <span class="text-h5 text-weight-bold q-mt-sm">{{ $t('파라미터') }}</span> -->
          <q-item-section>
            <ToolSelectBox v-model="selectedTool" @update:model-value="getToolStatusByTool" />
          </q-item-section>
        </q-item>
      </q-card-section>
      <simple-table
        bordered
        :rows="rows"
        :columns="columns"
        row-key="name"
        color="amber"
        selection="single"
        v-model:selected="selected"
      >
        <template v-slot:body="props">
          <q-tr class="cursor-pointer" :props="props" @click="props.selected = !props.selected">
            <q-td v-for="col in props.cols" :key="col.name" :props="props">
              {{
                col.name === 'toolStatusDate'
                  ? date.formatDate(new Date(col.value), 'YYYY-MM-DD')
                  : col.name === 'leadTime'
                    ? formatLeadTime(col.value)
                    : col.name === 'operationRate'
                      ? formatOperationRate(col.value)
                      : col.value
              }}
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
import ToolStatusService from 'src/services/modeling/ToolStatusService'
import { t } from 'src/plugins/i18n'
import ToolSelectBox from 'components/form/ToolSelectBox.vue'

const formatLeadTime = (minutes) => {
  const hours = Math.floor(minutes / 60) // 시간
  const mins = minutes % 60 // 나머지 분
  return `${hours}시간 ${mins}분`
}

const formatOperationRate = (rate) => {
  return `${(rate * 100).toFixed(2)}%` // 소수점 2자리까지 표시
}

const columns = ref([
  { name: 'toolStatusDate', align: 'left', label: t('일자'), field: 'toolStatusDate', sortable: true },
  {
    name: 'toolName',
    align: 'left',
    label: t('설비명'),
    field: 'toolName',
  },
  { name: 'leadTime', align: 'left', label: t('리드타임'), field: 'leadTime' },
  { name: 'operationRate', align: 'left', label: t('가동률'), field: 'operationRate' },
])

const rows = ref([])
const selected = ref([])
const selectedTool = ref(null)

const getToolStatusByTool = async (tool) => {
  const { id } = tool
  rows.value = await ToolStatusService.getToolStatus({ toolId: id })
  selected.value = []
}
</script>
