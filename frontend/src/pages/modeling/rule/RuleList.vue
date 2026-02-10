<template>
  <q-page class="q-pa-sm">
    <q-card class="cust_subPage">
      <q-card-section class="row search_section_wrap">
        <!-- <div class="text-h5 text-weight-bold q-mt-sm">{{ $t('규칙') }}</div> -->
        <q-item class="search_left"></q-item>
        <q-item class="col-4 search_right">
          <q-item-section>
            <q-btn v-permission:rule.create @click="clickCreate" class="add_btn with_icon_btn sBtn secondary">{{
              $t('추가')
            }}</q-btn>
            <q-btn
              v-permission:rule.update
              :disabled="selected == null || selected.length == 0"
              @click="clickModify"
              class="edit_btn with_icon_btn sBtn secondary"
              >{{ $t('수정') }}</q-btn
            >
            <q-btn
              v-permission:rule.delete
              :disabled="selected == null || selected.length == 0"
              @click="clickDelete"
              class="delete_btn with_icon_btn sBtn secondary"
              >{{ $t('삭제') }}</q-btn
            >
          </q-item-section>
          <rule-modify ref="ruleModify" @close="onCloseModify"></rule-modify>
        </q-item>
      </q-card-section>
      <simple-table
        bordered
        :rows="rows"
        :columns="columns"
        row-key="name"
        color="amber"
        selection="single"
        :pagination="{ rowsPerPage: 10 }"
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
import RuleModify from '/src/pages/modeling/rule/RuleModify.vue'
import RuleService from '/src/services/modeling/RuleService.js'
import useUI from 'src/common/module/ui'
import { t } from '/src/plugins/i18n'

const ui = useUI()
const ruleModify = ref(null, null)

const columns = ref([
  { name: 'id', align: 'left', label: t('Id'), field: 'id' },
  { name: 'name', align: 'left', label: t('이름'), field: 'name', sortable: true },
  { name: 'className', align: 'left', label: t('클래스명'), field: 'className' },
  {
    name: 'description',
    align: 'left',
    label: t('설명'),
    field: 'description',
  },
])

const rows = ref([])
const selected = ref([])

const clickModify = () => {
  let data = null
  if (selected.value.length > 0) {
    data = selected.value[0]
  }
  ruleModify.value.open(data)
}
const clickCreate = () => {
  ruleModify.value.open(null)
}
const clickDelete = () => {
  ui.confirm(t('삭제'), t('선택된 항목을 삭제 하시겠습니까?'), t('삭제'), t('취소')).onOk(deleteRule)
}
const onCloseModify = (value) => {
  console.log(value)
  if (value == 'ok') {
    getData()
  }
}
const deleteRule = async () => {
  try {
    await RuleService.deleteRule(selected.value[0].id)
    ui.notify.success(t('삭제 되었습니다.'))
  } catch (error) {
    error.response?.data.extraData.datas.forEach((message) => {
      if (error.response.data.code === 'ERROR_CONSTRAINT_VIOLATION') {
        ui.notify.error(message.path) //임시
      }
    })
  }
  getData()
}

const getData = async () => {
  rows.value = await RuleService.getRules()
}

getData()
</script>
