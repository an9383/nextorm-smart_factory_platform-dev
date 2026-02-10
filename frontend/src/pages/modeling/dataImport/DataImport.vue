<!-- eslint-disable no-unused-vars -->
<template>
  <q-page class="q-pa-sm">
    <q-card class="cust_subPage">
      <q-card-section class="row search_section_wrap">
        <q-item class="col-4 search_left">
          <q-item-section class="cust-input-file-wrap">
            <label for="file" class="title-label">
              <div class="btn-upload">파일을 선택해주세요</div>
            </label>
            <label for="file" class="icon"><q-icon name="attach_file" /></label>
            <input type="file" name="file" id="file" accept=".csv" @change="onChage" class="cust-input-file" />
          </q-item-section>
        </q-item>
        <q-item class="col-4 search_right">
          <q-item-section>
            <q-btn class="save_btn with_icon_btn sBtn secondary" v-permission:dataImport.create @click="SaveData">{{
              $t('데이터 저장')
            }}</q-btn>
          </q-item-section>
        </q-item>
      </q-card-section>
      <simple-table :title="$t('Import Data')" :rows="rows" :columns="columns" :row-key="rowKey" />
    </q-card>
  </q-page>
</template>

<script setup>
import { ref } from 'vue'

import useUI from 'src/common/module/ui'
import { useI18n } from 'vue-i18n'

import DataImportService from 'src/services/modeling/DataImportService.js'

const { t } = useI18n()
const ui = useUI()

const rows = ref([])
const columns = ref([])
const rowKey = ref('')

function onChage(e) {
  const theFile = e.target.files[0] //우선 1개만 보여줄꺼니까 크기 1로 지정
  const reader = new FileReader()
  reader.onload = (finishedEvent) => {
    let dataSplit = finishedEvent.target.result.split('\n')

    const headerData = dataSplit[0].split(',')
    columns.value = headerData.map((v) => {
      return { name: v, label: v, field: (row) => row[v] }
    })
    rows.value = []
    let datas = []
    for (let i = 1; i < dataSplit.length; i++) {
      const row = dataSplit[i].split(',')
      let data = {}
      for (let iColumn = 0; iColumn < columns.value.length; iColumn++) {
        data[columns.value[iColumn].name] = row[iColumn]
      }
      datas.push(data)
    }
    rows.value = datas
  }
  reader.readAsText(theFile)
}

async function SaveData() {
  ui.loading.show()
  for (let i = 1; i < columns.value.length; i++) {
    let dataImports = { parameterName: columns.value[i].name }
    let datas = []
    ui.loading.show({
      message: columns.value[i].name + t('진행율') + ':' + i.toString() + '/' + columns.value.length.toString(),
    })
    let iSendIndex = 0
    for (let irow = 0; irow < rows.value.length; irow++) {
      console.log('진행:', i, ' ', irow)
      if (rows.value[irow][columns.value[0].name] == '') continue
      let dataImport = [
        new Date(rows.value[irow][columns.value[0].name]).toISOString(),
        rows.value[irow][columns.value[i].name],
      ]
      datas.push(dataImport)

      if (iSendIndex++ > 10000) {
        iSendIndex = 0
        dataImports['datas'] = datas
        await DataImportService.saveImportData(dataImports)
        datas = []
        dataImports['datas'] = []
        ui.loading.show({
          message:
            columns.value[i].name +
            ' ' +
            t('진행율') +
            ':' +
            i.toString() +
            '/' +
            columns.value.length.toString() +
            '  ' +
            t('내부 진행율') +
            ':' +
            irow.toString() +
            '/' +
            rows.value.length.toString(),
        })
      }
    }
    if (datas.length > 0) {
      dataImports['datas'] = datas
      await DataImportService.saveImportData(dataImports)
    }
  }
  ui.loading.hide()
}
</script>

<style scoped>
.header {
  font-weight: bold;
  margin-bottom: 10px;
}

.action {
  display: flex;
  align-items: center;
  padding: 3px;
}

.action_button {
  display: flex;
  gap: 10px;
}

.q-btn {
  min-width: 100px;
}

.q-select {
  min-width: 150px;
  margin-left: 50px;
}
.cust-input-file-wrap {
  position: relative;
}
.cust-input-file-wrap label.title-label {
  position: absolute;
  top: -2px;
  left: 12px;
  font-size: 12px;
  color: #7983af;
}
.cust-input-file-wrap label.icon {
  position: absolute;
  font-size: 16px;
  color: rgba(0, 0, 0, 0.54);
  left: 12px;
  bottom: 4px;
}
.cust-input-file-wrap input[type='file'].cust-input-file {
  height: 30px;
  padding: 0 30px 0 40px;
  border: 1px solid #ddd;
  border-radius: 4px;
  line-height: 30px;
  font-size: 12px;
}
.cust-input-file-wrap input[type='file'].cust-input-file:active {
  position: relative;
}
.cust-input-file-wrap input[type='file'].cust-input-file:active:after {
  position: absolute;
  content: '';
  bottom: 0;
  left: 0;
  width: 100%;
  height: 2px;
  background: var(--q-primary);
}
.cust-input-file-wrap input[type='file'].cust-input-file::file-selector-button {
  display: none;
}
</style>
