<template>
  <div class="col column q-px-xl bg-white">
    <ECharts ref="chart" :option="options" class="q-mt-md col" resizable autoresize />
    <div class="q-mt-md row q-px-xl">
      <q-range
        v-model="selectRange"
        @change="onRangeChange"
        color="deep-orange"
        markers
        :min="rangeMin"
        :max="rangeMax"
        drag-range
        label-always
        :left-label-value="minLabel"
        :right-label-value="maxLabel"
      />
      <!-- <div class="row">
        <q-item class="">
          <q-item-section> {{ t('자동 시간 변경') }} </q-item-section>
        </q-item>
        <q-item class="col-lg-1">
          <q-item-section>
            <q-input v-model="selectRangeSeconds" dense outlined round label="sec"></q-input>
          </q-item-section>
        </q-item>
        <q-item class="col-lg-3">
          <q-item-section>
            <q-btn v-if="!isTimeProcess" :label="t('진행')" @click="onTimeProcess"></q-btn>
            <q-btn v-if="isTimeProcess" :label="t('정지')" @click="onTimeProcess"></q-btn>
          </q-item-section>
          <q-item-section>
            <span>{{ t('Current:') }}</span>
          </q-item-section>
          <q-item-section class="col-lg-6">
            <span>{{ date.formatDate(filterDateTo, 'YYYY-MM-DD HH:mm:ss') }}</span>
          </q-item-section>
        </q-item>
      </div> -->
    </div>
  </div>
</template>

<script setup>
import ECharts from 'vue-echarts'
import { computed, onMounted, ref, watch } from 'vue'
import { date } from 'quasar'
import { use } from 'echarts/core'
import { DataZoomComponent, ToolboxComponent, TooltipComponent, VisualMapComponent } from 'echarts/components'
import { LineChart, ScatterChart } from 'echarts/charts'
import { CanvasRenderer } from 'echarts/renderers'

use([
  LineChart,
  ScatterChart,
  VisualMapComponent,
  DataZoomComponent,
  ToolboxComponent,
  TooltipComponent,
  CanvasRenderer,
])
// import { t } from '/src/plugins/i18n'

const chart = ref()
// const containerId = ref(uid())
const props = defineProps(['datas', 'markers', 'dateRange'])

let xMin = null
let xMax = null
let yMin = null
let yMax = null
let vMin = null
let vMax = null
const dataFromDate = ref()
const dataToDate = ref()
watch(
  () => props.datas,
  (datas) => {
    // filterDateFrom.value = props.dateRange.fromDate
    // filterDateTo.value = props.dateRange.toDate

    if (datas.length == 0) return
    dataFromDate.value = new Date(datas[0][3])
    dataToDate.value = new Date(datas[datas.length - 1][3])

    filterDateFrom.value = dataFromDate.value
    filterDateTo.value = dataToDate.value

    datas.map((d) => {
      if (xMin == null) {
        xMin = d[0]
      } else if (xMin > d[0]) {
        xMin = d[0]
      }
      if (xMax == null) {
        xMax = d[0]
      } else if (xMax < d[0]) {
        xMax = d[0]
      }
      if (yMin == null) {
        yMin = d[1]
      } else if (yMin > d[1]) {
        yMin = d[1]
      }
      if (yMax == null) {
        yMax = d[1]
      } else if (yMax < d[1]) {
        yMax = d[1]
      }
      if (vMin == null) {
        vMin = d[2]
      } else if (vMin > d[2]) {
        vMin = d[2]
      }
      if (vMax == null) {
        vMax = d[2]
      } else if (vMax < d[2]) {
        vMax = d[2]
      }
    })
    filterData()
    // let dateDiff = date.getDateDiff(props.dateRange.toDate, props.dateRange.fromDate, 'minutes')
    let dateDiff = date.getDateDiff(dataToDate.value, dataFromDate.value, 'minutes')
    rangeMin.value = 0
    rangeMax.value = dateDiff
    fnMarkerLabel()
  },
)
const options = ref({
  tooltip: {
    trigger: 'item',
    axisPointer: {
      //   type: 'cross',
      animation: false,
      //   triggerTooltip: true,
      //   label: {
      //     backgroundColor: '#ccc',
      //     borderColor: '#aaa',
      //     borderWidth: 1,
      //     shadowBlur: 0,
      //     shadowOffsetX: 0,
      //     shadowOffsetY: 0,
      //     color: '#222',
      //   },
    },

    formatter: function (params) {
      return (
        // 'Name: ' +
        // params[0].name +
        // '<br />' +
        'Longitude: ' +
        // params[0].value[0] +
        params.data[0] +
        '<br />' +
        'Latitude: ' +
        params.data[1] +
        '<br />' +
        'Value: ' +
        params.data[2]
      )
    },
  },
  dataZoom: [
    {
      type: 'inside', // 차트 내부에 스크롤바 표시
      disabled: true,
    },
  ],
  toolbox: {
    left: 'center',
    itemSize: 25,
    top: 0,
    feature: {
      dataZoom: {
        yAxisIndex: 'none',
      },
      restore: {},
    },
  },
  xAxis: {
    type: 'value',
    // data: [],
    splitLine: {
      show: false,
      // lineStyle: {
      //   color: "white",
      // },
    },
    // axisLabel: {
    //   color: "#fff",
    // },
    min: (value) => value.min,
    max: (value) => value.max,
    boundaryGap: false,
    // inverse: true
  },
  yAxis: {
    type: 'value',
    splitLine: {
      show: false,
      // lineStyle: {
      //   color: "white",
      // },
    },
    // axisLabel: {
    //   color: "#fff",
    // },
    min: (value) => value.min,
    max: (value) => value.max,
  },
  series: [
    {
      name: 'data',
      data: [],
      type: 'scatter',
      showSymbol: true,
      symbolSize: 4,
      animation: false,
    },
    {
      name: 'place',
      data: [],
      type: 'line',
      lineStyle: {
        color: 'orange',
        width: 1,
      },
      showSymbol: false,
      animation: false,
    },
  ],
  grid: { top: 30, bottom: 40 },
})

function filterData() {
  options.value = {
    ...options.value,
    xAxis: {
      ...options.value.xAxis,
      // data: datas.map((v) => v[0]),
      // min: xMin,
      // max: xMax,
    },
    yaxis: {
      ...options.value.yAxis,
      // min: yMin,
      // max: yMax,
      // data: datas.map((v) => v[1]),
    },
    visualMap: {
      min: vMin,
      max: vMax,
      type: 'continuous',
      // Maps to the third value in each data point (index 2)
      dimension: 2,
      inRange: {
        color: ['#0A2FDB', '#DB0A2F'],
      },
      text: ['HIGH', 'LOW'],
      right: 10,
      top: 'center',
      calculable: true,
    },
    series: [
      {
        ...options.value.series[0],
        data: props.datas
          .filter((v) => {
            const date = new Date(v[3])
            return date >= filterDateFrom.value && date < filterDateTo.value ? true : false
          })
          .map((v) => [v[0], v[1], v[2]]),
      },
      {
        ...options.value.series[1],
        data: props.markers.map((v) => [v[1], v[0]]),
      },
    ],
  }
}

onMounted(() => {
  chart.value.chart.on('dataZoom', (e) => {
    // const dom = document.getElementById(containerId.value)
    // console.log(e, dom.clientWidth)

    var start = e.batch[0].startValue // x축 값
    var end = e.batch[0].endValue // y축 값
    console.log(`x축 시작: ${start}, x축 끝: ${end}`)
    // chart.value.chart.setOption({
    //   series: [{ ...options.value.series[0] }],
    // })
  })
  chart.value.chart.on('restore', () => {})
})

const selectRange = ref({ min: null, max: null })
const rangeMin = ref(0)
const rangeMax = ref(10)
const markerLabels = ref([])
const filterDateFrom = ref()
const filterDateTo = ref()
// const selectRangeSeconds = ref(60)

const fnMarkerLabel = () => {
  let datas = {}
  for (let i = 0; i <= rangeMax.value; i++) {
    // datas[i] = date.formatDate(date.addToDate(props.dateRange.fromDate, { hours: i }), 'HH:mm')
    datas[i] = date.formatDate(date.addToDate(dataFromDate.value, { hours: i }), 'HH:mm')
  }
  markerLabels.value = datas
  selectRange.value = { min: rangeMin.value, max: rangeMax.value }
}
const onRangeChange = (e) => {
  filterDateFrom.value = date.addToDate(dataFromDate.value, { minutes: e.min })
  filterDateTo.value = date.addToDate(dataFromDate.value, { minutes: e.max })
  console.log('from,to', filterDateFrom, filterDateTo)
  filterData()
}
// const isTimeProcess = ref(false)
// const autoTimeTo = ref(0)
// const onTimeProcess = () => {
//   if (isTimeProcess.value) {
//     isTimeProcess.value = false

//     return
//   }
//   isTimeProcess.value = true
//   autoTimeTo.value = selectRange.value.min
//   filterDateFrom.value = date.addToDate(dataFromDate.value, { milliseconds: selectRange.value.min })
//   filterDateTo.value = date.addToDate(dataFromDate.value, { seconds: autoTimeTo.value })
//   timeAnimation()
// }
// const timeAnimation = () => {
//   setTimeout(() => {
//     if (!isTimeProcess.value) return
//     const diff = selectRange.value.max - selectRange.value.min
//     if (autoTimeTo.value < diff * 60) {
//       autoTimeTo.value += selectRangeSeconds.value
//       filterDateTo.value = date.addToDate(props.dateRange.fromDate, { seconds: autoTimeTo.value })
//       filterData()
//       timeAnimation()
//     } else {
//       isTimeProcess.value = false
//     }
//   }, 1000)
// }

const minLabel = computed(() => {
  if (selectRange.value.min == null) {
    return 0
  }
  return date.formatDate(
    date.addToDate(props.dateRange.fromDate, { seconds: selectRange.value.min }),
    'YY-MM-DD HH:mm:ss',
  )
})
const maxLabel = computed(() => {
  if (selectRange.value.max == null) {
    return 0
  }
  return date.formatDate(
    date.addToDate(props.dateRange.fromDate, { minutes: selectRange.value.max }),
    'YY-MM-DD HH:mm:ss',
  )
})
console.log(minLabel, maxLabel)
</script>

<style lang="scss" scoped></style>
