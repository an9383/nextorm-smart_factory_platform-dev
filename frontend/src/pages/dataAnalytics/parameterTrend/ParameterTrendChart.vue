<template>
  <div v-if="datas.length > 0 && props.tableViewable" class="btn-table-view">
    <q-btn dense @click="toggleTable" icon="table_chart" class="sBtn">
      <span class="q-pl-sm">{{ !isTableView ? $t('테이블 보기') : $t('테이블 닫기') }}</span>
    </q-btn>
  </div>
  <div class="q-ma-md column col">
    <trend-chart :datas="datas" show-spec-line class="echart-width-full"></trend-chart>
  </div>
  <q-card ref="chartTableContainer" class="chart-table-container q-mt-md" v-if="isTableView">
    <q-card-section>
      <simple-table :rows="rows" :columns="columns" color="amber">
        <template v-slot:body="props">
          <q-tr :props="props">
            <q-td v-for="col in props.cols" :key="col.name" :props="props">
              {{ col.value }}
            </q-td>
          </q-tr>
        </template>
      </simple-table>
    </q-card-section>
  </q-card>
</template>

<script setup>
import { onMounted, ref, watch, nextTick } from 'vue'
import ParameterDataService from 'src/services/parameterData/ParameterDataService.js'
import TrendChart from 'src/components/chart/TrendChart.vue'
import useUI from 'src/common/module/ui'
import { date } from 'quasar'
import { t, pt } from '/src/plugins/i18n'
import { formatDateTime } from 'src/common/utils'
import { DATE_TIME_FORMAT } from 'src/common/constant/format'

const props = defineProps({
  parameterId: {
    type: Number || String,
    required: true,
  },
  fromDateTime: {
    type: Date,
    required: false,
    default: date.subtractFromDate(Date.now(), { minutes: 10 }),
  },
  toDateTime: {
    type: Date,
    required: false,
    default: new Date(),
  },
  tableViewable: {
    type: Boolean,
    required: false,
    default: false,
  },
})

const chartTableContainer = ref()
const isTableView = ref(false)
const rows = ref([])
const datas = ref([])
const columns = [
  {
    name: 'x',
    label: t('일시'),
    align: 'left',
    field: 'x',
    format: (val) => date.formatDate(val, DATE_TIME_FORMAT),
    sortable: true,
  },
  {
    name: 'y',
    label: t('값'),
    align: 'left',
    field: 'y',
    sortable: true,
  },
  {
    name: 'ucl',
    label: t('UCL'),
    align: 'left',
    field: 'ucl',
    sortable: true,
  },
  {
    name: 'lcl',
    label: t('LCL'),
    align: 'left',
    field: 'lcl',
    sortable: true,
  },
  {
    name: 'usl',
    label: t('USL'),
    align: 'left',
    field: 'usl',
    sortable: true,
  },
  {
    name: 'lsl',
    label: t('LSL'),
    align: 'left',
    field: 'lsl',
    sortable: true,
  },
]

const ui = useUI()

onMounted(() => {
  const { parameterId, fromDateTime, toDateTime } = props
  loadParameterTrendData(parameterId, fromDateTime, toDateTime)
})

watch(
  () => [props.parameterId, props.fromDateTime, props.toDateTime],
  () => {
    const { parameterId, fromDateTime, toDateTime } = props
    loadParameterTrendData(parameterId, fromDateTime, toDateTime)
  },
)

const loadParameterTrendData = async (parameterId, fromDateTime, toDateTime) => {
  datas.value = []

  if (!parameterId || !fromDateTime || !toDateTime) {
    return
  }
  const from = new Date(fromDateTime).toISOString()
  const to = new Date(toDateTime).toISOString()
  ui.loading.show()
  const parameterTrendData = await ParameterDataService.getParameterDataTrend(parameterId, from, to)
  const { rawDatas, parameterName } = parameterTrendData

  if (!rawDatas?.length) {
    ui.notify.warning(t('조회조건에 맞는 데이터가 없습니다.'))
  } else {
    datas.value = [
      {
        name: pt(parameterName),
        trendData: rawDatas.map((k) => ({
          x: formatDateTime(new Date(k.traceAt)),
          y: k.value,
          usl: k.usl,
          lsl: k.lsl,
          ucl: k.ucl,
          lcl: k.lcl,
        })),
      },
    ]
    rows.value = datas.value[0].trendData
  }
  ui.loading.hide()
}

const toggleTable = () => {
  isTableView.value = !isTableView.value

  if (isTableView.value) {
    moveScroll()
  }
}

const moveScroll = () => {
  nextTick(() => {
    chartTableContainer.value.$el.scrollIntoView({ behavior: 'smooth' })
  })
}
</script>

<style lang="scss">
.btn-table-view {
  position: absolute;
  top: 15px;
  right: 15px;
  z-index: 1;
}
.chart-table-container {
  position: absolute;
  top: 100%;
  width: 100%;
}
</style>
