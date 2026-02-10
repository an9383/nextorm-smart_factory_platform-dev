<template>
  <q-card-section class="q-pb-none" v-if="isNoData">
    <div class="flex justify-between q-mb-sm">
      <q-chip square icon="stacked_line_chart" class="sBtn secondary">
        <span class="q-pl-sm">{{ props.periodType === 'DAILY' ? $t('일자별 차트') : $t('시간별 차트') }}</span>
      </q-chip>
      <div>
        <q-btn dense @click="toggleTable" icon="table_chart" class="sBtn">
          <span class="q-pl-sm">{{ isTableView === true ? $t('테이블 닫기') : $t('테이블 보기') }}</span>
        </q-btn>
        <q-btn
          class="q-ml-xs sBtn"
          :title="t('PDF 출력')"
          dense
          icon="print"
          square
          text-color="white"
          @click="openPrintPdfPopup"
        />
        <q-dialog v-model="isPrintPdfPopUp" persistent>
          <q-card style="min-width: 350px">
            <q-card-section>
              <div class="text-h6">{{ $t('비고를 입력하세요.') }}</div>
            </q-card-section>

            <q-card-section class="q-pt-none">
              <q-input type="textarea" outlined v-model="note" autofocus class="full-width" />
            </q-card-section>

            <q-card-actions align="right" class="text-primary">
              <q-btn flat :label="t('취소')" color="negative" v-close-popup />
              <q-btn flat :label="t('출력')" color="primary" @click="printPdf" v-close-popup />
            </q-card-actions>
          </q-card>
        </q-dialog>
      </div>
    </div>
  </q-card-section>
  <q-card-section class="q-pt-none" v-if="isNoData">
    <q-slide-transition>
      <BoxPlotChart :box-plot-data="boxPlotData" @click:series="seriesClick" />
    </q-slide-transition>
    <q-slide-transition>
      <div ref="table">
        <simple-table
          v-if="isTableView"
          :rows="filteredRows"
          :columns="columns"
          row-key="startTime"
          color="amber"
          selection="single"
          v-model:selected="selectedRow"
          :pagination="{ rowsPerPage: 5 }"
          class="full-width"
        >
          <template v-slot:body="props">
            <q-tr class="cursor-pointer" :props="props" @click="tableRowClick(props)">
              <q-td v-for="col in props.cols" :key="col.name" :props="props">
                {{ col.value }}
              </q-td>
            </q-tr>
          </template>
        </simple-table>
      </div>
    </q-slide-transition>
  </q-card-section>
  <q-card-section v-else class="col no-data">
    <div style="text-align: center">
      <q-icon class="flex-center" name="mdi-database-remove" size="50px" color="primary" />
      <div class="text-body-2 q-my-sm">
        {{ $t('조회된 데이터가 없습니다.') }}
      </div>
    </div>
  </q-card-section>
</template>

<script setup>
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import useUI from 'src/common/module/ui'
import { pt, t } from 'src/plugins/i18n'
import { date } from 'quasar'
import { DATE_FORMAT, DATE_TIME_FORMAT } from 'src/common/constant/format'
import BoxPlotChart from 'components/chart/BoxPlotChart.vue'
import SummaryDataService from 'src/services/parameterData/SummaryDataService'

const ui = useUI()
const emit = defineEmits(['click:series'])
const table = ref()

const summaryDatas = ref([])
const isTableView = ref(false)
const selectedRow = ref()
const periodTypes = { daily: 'DAILY', hourly: 'HOURLY' }
const isPrintPdfPopUp = ref(false)
const note = ref('')
const isNoData = ref(false)

const props = defineProps({
  periodType: {
    type: String,
    validator(value) {
      return ['DAILY', 'HOURLY'].includes(value)
    },
  },
  parameterIds: {
    type: Array,
    required: true,
  },
  fromDate: {
    type: Date,
    required: true,
  },
  toDate: {
    type: Date,
    required: true,
  },
  checkedParameterIds: {
    type: Array,
    required: false,
  },
})

const columns = [
  {
    name: 'startTime',
    label: t('시작시간'),
    align: 'left',
    field: 'startTime',
    format: (val) => date.formatDate(val, DATE_TIME_FORMAT),
    sortable: true,
  },
  {
    name: 'endTime',
    label: t('종료시간'),
    align: 'left',
    field: 'endTime',
    format: (val) => date.formatDate(val, DATE_TIME_FORMAT),
    sortable: true,
  },
  {
    name: 'name',
    label: t('파라미터명'),
    align: 'left',
    field: 'name',
    sortable: true,
    format: (val) => pt(val),
  },
  {
    name: 'dataType',
    label: t('데이터타입'),
    align: 'left',
    field: 'dataType',
    sortable: true,
  },
  {
    name: 'avg',
    label: t('평균값'),
    align: 'left',
    field: 'avg',
    sortable: true,
  },
  {
    name: 'median',
    label: t('중간값'),
    align: 'left',
    field: 'median',
    sortable: true,
  },
  {
    name: 'min',
    label: t('최소값'),
    align: 'left',
    field: 'min',
    sortable: true,
  },
  {
    name: 'max',
    label: t('최대값'),
    align: 'left',
    field: 'min',
    sortable: true,
  },
  {
    name: 'specLimit',
    label: t('Spec Limit'),
    align: 'left',
    field: 'specLimit',
    sortable: true,
  },
  {
    name: 'controlLimit',
    label: t('Control Limit'),
    align: 'left',
    field: 'controlLimit',
    sortable: true,
  },
]

onMounted(() => {
  loadReportSummaryData()
})
watch([() => props.parameterIds, () => props.toDate], () => {
  loadReportSummaryData()
})

const toggleTable = () => {
  isTableView.value = !isTableView.value
  if (isTableView.value) {
    moveScroll()
  }
}
const moveScroll = () => {
  nextTick(() => {
    table.value.scrollIntoView({ behavior: 'smooth' })
  })
}

const openPrintPdfPopup = () => {
  isPrintPdfPopUp.value = true
}

const printPdf = async () => {
  ui.loading.show()
  const pdfByteData = await SummaryDataService.getParameterReportPdf(
    props.checkedParameterIds,
    new Date(props.fromDate),
    new Date(props.toDate),
    props.periodType,
    note.value,
  )
  ui.loading.hide()

  const byteChar = atob(pdfByteData)
  const byteNum = Array.from(byteChar, (char) => char.charCodeAt(0))

  const byteArray = new Uint8Array(byteNum)
  const blob = new Blob([byteArray], { type: 'application/pdf' })
  const pdfUrl = URL.createObjectURL(blob)

  window.open(pdfUrl)
}

const loadReportSummaryData = async () => {
  isTableView.value = false

  ui.loading.show()
  summaryDatas.value = await SummaryDataService.getSumBaseAtSummaryDataByPeriodType(
    props.parameterIds,
    new Date(props.fromDate),
    new Date(props.toDate),
    props.periodType,
  )
  const mergeDatas = summaryDatas.value.flatMap((data) => data.summaryReportDatas)
  ui.loading.hide()
  if (mergeDatas.length === 0) {
    isNoData.value = false
    ui.notify.warning(t('조회된 데이터가 없습니다.'))
  } else {
    isNoData.value = true
  }
}

const filteredRows = computed(() => {
  return summaryDatas.value
    .filter((v) => props.checkedParameterIds.includes(v.parameterId))
    .reduce((acc, v) => {
      return acc.concat(
        v.summaryReportDatas.map((data) => ({
          name: v.name,
          dataType: v.dataType,
          startTime: data.startTime,
          endTime: data.endTime,
          max: data.max,
          q3: data.q3,
          median: data.median,
          q1: data.q1,
          min: data.min,
          avg: data.avg,
          specLimit: data.specLimit,
          controlLimit: data.controlLimit,
        })),
      )
    }, [])
    .sort((a, b) => new Date(a.startTime) - new Date(b.startTime))
})

const boxPlotData = computed(() => {
  const dateFormat = props.periodType === periodTypes.daily ? DATE_FORMAT : DATE_TIME_FORMAT
  return summaryDatas.value
    .filter((v) => props.checkedParameterIds.includes(v.parameterId))
    .map((data) => {
      const seriesData = data.summaryReportDatas.map((item) => {
        const xAxisDate = date.formatDate(item.startTime, dateFormat)
        return { x: xAxisDate, y: { min: item.min, q1: item.q1, median: item.median, q3: item.q3, max: item.max } }
      })
      return { name: data.name, seriesData: seriesData }
    })
})

const seriesClick = async (e) => {
  if (props.periodType === periodTypes.daily) {
    const subChartData = {
      parameterIds: props.parameterIds,
      periodType: periodTypes.hourly,
      fromDate: new Date(e.name).setHours(0, 0, 0),
      toDate: new Date(e.name).setHours(24, 0, 0, 0),
    }
    emit('click:series', subChartData)
  } else {
    const trendChartData = {
      parameterIds: props.parameterIds,
      toDate: e.name,
      periodType: props.periodType,
    }
    emit('click:series', trendChartData)
  }
}

const tableRowClick = (row) => {
  const tableClickParameter = {
    name: row.row.startTime,
  }
  seriesClick(tableClickParameter)
  row.selected = !row.selected
}
</script>

<style scoped>
.no-data {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 500px;
}
</style>
