<template>
  <q-card-section>
    <div class="flex justify-between q-mb-sm q-pr-sm">
      <q-chip square icon="stacked_line_chart" class="sBtn secondary">
        <span class="q-pl-sm">{{ $t('RAW 차트') }}</span>
      </q-chip>
      <q-btn dense @click="toggleTable" icon="table_chart" class="sBtn width">
        <span class="q-pl-sm">{{ isTableView === true ? $t('테이블 닫기') : $t('테이블 보기') }}</span>
      </q-btn>
    </div>
  </q-card-section>
  <q-card-section>
    <q-slide-transition>
      <div id="echarts">
        <TrendChart v-if="isChart" :datas="filteredTrendChartData" :legend-clickable="false" />
      </div>
    </q-slide-transition>
    <q-slide-transition>
      <div ref="trendTable">
        <simple-table
          v-if="isTableView"
          :rows="filteredRows"
          :columns="columns"
          class="full-width"
          :pagination="{ rowsPerPage: 5 }"
        />
      </div>
    </q-slide-transition>
  </q-card-section>
</template>
<script setup>
import { computed, defineProps, nextTick, onMounted, ref, watch } from 'vue'
import ParameterDataService from 'src/services/parameterData/ParameterDataService'
import { pt, t } from 'src/plugins/i18n'
import { date } from 'quasar'
import { DATE_TIME_FORMAT } from 'src/common/constant/format'
import TrendChart from 'components/chart/TrendChart.vue'
import useUI from 'src/common/module/ui'
import { formatDateTime } from 'src/common/utils'

const ui = useUI()
const isTableView = ref(false)
const isChart = ref(false)
const trendTable = ref(null)
const trendDatas = ref()

const props = defineProps({
  lineDate: {
    type: Date,
    required: true,
  },
  parameterIds: {
    type: Array,
    required: true,
  },
  checkedParameterIds: {
    type: Array,
    required: false,
  },
})

const columns = [
  {
    name: 'traceAt',
    label: t('시간'),
    align: 'left',
    field: 'traceAt',
    format: (val) => date.formatDate(val, DATE_TIME_FORMAT),
    sortable: true,
  },
  {
    name: 'parameterName',
    label: t('파라미터명'),
    align: 'left',
    field: 'parameterName',
    sortable: true,
    format: (val) => pt(val),
  },
  {
    name: 'parameterType',
    label: t('데이터타입'),
    align: 'left',
    field: 'parameterType',
    sortable: true,
  },
  {
    name: 'value',
    label: t('값'),
    align: 'left',
    field: 'value',
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
  loadParameterTrendData()
})
watch(
  () => props.lineDate,
  () => {
    loadParameterTrendData()
  },
  { deep: true },
)

const toggleTable = () => {
  isTableView.value = !isTableView.value
  if (isTableView.value) {
    moveScroll()
  }
}

const moveScroll = () => {
  nextTick(() => {
    trendTable.value.scrollIntoView({ behavior: 'smooth' })
  })
}

const loadParameterTrendData = async () => {
  const fromDate = new Date(props.lineDate)
  const toDate = new Date(new Date(props.lineDate).setMinutes(59, 59, 999))
  ui.loading.show()
  trendDatas.value = await ParameterDataService.getMultiParameterDataTrend(props.parameterIds, fromDate, toDate)
  ui.loading.hide()

  isChart.value = true
}

const filteredRows = computed(() => {
  return trendDatas.value
    .filter((v) => props.checkedParameterIds.includes(v.parameterId))
    .reduce((acc, v) => {
      return acc.concat(
        v.rawDatas.map((raw) => {
          const specLimit = (raw.lsl ? raw.lsl : '') + '~' + (raw.usl ? raw.usl : '')
          const controlLimit = (raw.lcl ? raw.lcl : '') + '~' + (raw.ucl ? raw.ucl : '')
          return {
            parameterName: v.parameterName,
            specLimit: specLimit === '~' ? '' : specLimit,
            controlLimit: controlLimit === '~' ? '' : controlLimit,
            value: raw.value,
            traceAt: raw.traceAt,
            parameterType: v.parameterType,
          }
        }),
      )
    }, [])
    .sort((a, b) => new Date(a.traceAt) - new Date(b.traceAt))
})

const filteredTrendChartData = computed(() => {
  return trendDatas.value
    .filter((v) => props.checkedParameterIds.includes(v.parameterId))
    .map((v) => ({
      name: pt(v.parameterName),
      trendData: v.rawDatas.map((k) => ({
        x: formatDateTime(new Date(k.traceAt)),
        y: k.value,
      })),
    }))
})
</script>

<style scoped>
#echarts {
  height: 400px;
}
</style>
