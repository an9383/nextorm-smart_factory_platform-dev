<template>
  <q-page class="q-pa-sm">
    <q-card class="cust_subPage">
      <q-card-section class="row search_section_wrap">
        <q-item class="col-8 search_left">
          <date-time-range-section v-model:from-date="searchDates.from" v-model:to-date="searchDates.to" />
        </q-item>
        <q-item class="col-4 search_right">
          <q-item-section>
            <q-btn @click="fetchHistories" class="search_btn with_icon_btn sBtn">
              {{ $t('조회') }}
            </q-btn>
          </q-item-section>
        </q-item>
      </q-card-section>
      <simple-table bordered :rows="tableRows" :columns="columns" row-key="id" color="amber">
        <template v-slot:body="props">
          <q-tr class="cursor-pointer" :props="props">
            <q-td v-for="col in props.cols" :key="col.name" :props="props">
              <q-btn v-if="col.name === 'toFaultDataView'" @click="handleToFaultDataViewClick(props.row)">
                {{ $t('보기') }}
              </q-btn>
              <div v-else>
                {{ col.value }}
              </div>
            </q-td>
          </q-tr>
        </template>
      </simple-table>
    </q-card>
  </q-page>

  <fault-data-viewer
    v-if="showFaultDataViewer"
    :parameter-id="faultDataViewerProps.parameterId"
    :fault-at="faultDataViewerProps.faultAt"
    @close="handleFaultDataViewerClose"
  />
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import useUI from 'src/common/module/ui'
import OcapService from 'src/services/ocap/OcapService'
import { date } from 'quasar'
import DateTimeRangeSection from 'components/form/dateTime/DateTimeRangeSection.vue'
import FaultDataViewer from 'pages/ocap/alarmHistory/FaultDataViewer.vue'

const defaultSearchDates = {
  from: new Date(new Date().setHours(0, 0, 0, 0)),
  to: new Date(),
}

const { t } = useI18n()
const ui = useUI()

const showFaultDataViewer = ref(false)
const faultDataViewerProps = ref({
  parameterId: undefined,
  faultAt: undefined,
})

const tableRows = ref([])
const searchDates = ref({ ...defaultSearchDates })

const columns = ref([
  { name: 'name', align: 'left', label: t('이름'), field: 'name' },
  { name: 'toolName', align: 'left', label: t('설비'), field: 'toolName' },
  { name: 'parameterName', align: 'left', label: t('파라미터'), field: 'parameterName' },
  {
    name: 'faultAt',
    align: 'left',
    label: t('발생 시간'),
    field: 'faultAt',
    format: (val) => (val ? date.formatDate(val, 'YYYY-MM-DD HH:mm:ss') : '-'),
  },
  { name: 'alarmCondition', align: 'left', label: t('발생 알람'), field: 'alarmCondition' },
  {
    name: 'toFaultDataView',
    align: 'left',
    label: t('데이터 확인'),
    style: 'width: 200px',
  },
])

const handleToFaultDataViewClick = async (tableRow) => {
  faultDataViewerProps.value = {
    parameterId: tableRow.parameterId,
    faultAt: tableRow.faultAt,
  }
  showFaultDataViewer.value = true
}

const handleFaultDataViewerClose = async () => {
  faultDataViewerProps.value = {}
  showFaultDataViewer.value = false
}

const fetchHistories = async () => {
  try {
    ui.loading.show()
    const { from, to } = searchDates.value
    tableRows.value = await OcapService.getOcapAlarmHistories(from, to)
  } catch (error) {
    console.error('알람 히스토리 조회 실패:', error)
    ui.notify.error(t('알람 히스토리 조회에 실패했습니다.'))
  } finally {
    ui.loading.hide()
  }
}

onMounted(() => {
  fetchHistories()
})
</script>

<style scoped lang="scss"></style>
