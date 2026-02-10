<template>
  <q-page class="q-pa-sm">
    <q-card class="cust_subPage">
      <q-card-section class="row search_section_wrap">
        <q-item class="col-11 search_left grid-0303-full">
          <q-item-section>
            <q-input filled v-model="toYear" readonly stack-label :label="$t('조회년도')">
              <template v-slot:prepend>
                <q-icon name="event" class="cursor-pointer">
                  <q-popup-proxy cover transition-show="scale" transition-hide="scale">
                    <q-date
                      v-model="toYear"
                      @update:model-value="updateYear"
                      :key="yearPickerKey"
                      default-view="Years"
                      mask="YYYY"
                      emit-immediately
                      :navigation-max-year-month="new Date().getFullYear() + '/12'"
                      minimal
                    ></q-date>
                  </q-popup-proxy>
                </q-icon>
              </template>
            </q-input>
          </q-item-section>
          <q-item-section>
            <filterable-select
              outlined
              v-model="selectedTool"
              :options="toolOptions"
              option-value="id"
              option-label="name"
              @update:model-value="loadParametersByTool"
              :label="$t('설비')"
            >
              <template v-slot:prepend>
                <q-icon name="construction" />
              </template>
            </filterable-select>
          </q-item-section>
          <q-item-section>
            <filterable-select
              outlined
              v-model="selectedParameters"
              :options="parameterOptions"
              option-value="id"
              :option-label="(item) => $pt(item.name)"
              :label="$t('파라미터')"
              stack-label
              multiple
              use-chips
            >
              <template v-slot:prepend>
                <q-icon name="dataset" />
              </template>
              <template v-slot:no-option>
                <q-item>
                  <q-item-section class="text-grey"> {{ $t('선택 가능한 파라미터가 없습니다.') }}</q-item-section>
                </q-item>
              </template>
            </filterable-select>
          </q-item-section>
        </q-item>
        <!-- <q-item class="col-8">
        </q-item> -->
        <q-item class="col-1 search_right">
          <q-item-section>
            <q-btn class="search_btn with_icon_btn sBtn" @click="onSearch">{{ $t('조회') }}</q-btn>
          </q-item-section>
        </q-item>
      </q-card-section>
      <!-- <q-separator /> -->
      <q-card-section class="column col">
        <Echarts id="echart" :option="options" resizable autoresize></Echarts>
      </q-card-section>
    </q-card>
  </q-page>
</template>

<script setup>
import Echarts from 'vue-echarts'
import { ref } from 'vue'
import ToolService from 'src/services/modeling/ToolService'
import ParameterService from 'src/services/modeling/ParameterService'
import { pt, t } from 'src/plugins/i18n'
import useUI from 'src/common/module/ui'
import HealthDataService from 'src/services/parameterData/HealthDataService'
import { use } from 'echarts/core'
import { BarChart } from 'echarts/charts'
import { CanvasRenderer } from 'echarts/renderers'
import {
  DataZoomComponent,
  GridComponent,
  LegendComponent,
  ToolboxComponent,
  TooltipComponent,
} from 'echarts/components'

use([BarChart, DataZoomComponent, GridComponent, LegendComponent, ToolboxComponent, TooltipComponent, CanvasRenderer])

const ui = useUI()
const toYear = ref(new Date().getFullYear())
const yearPickerKey = ref(Date.now())
const toolOptions = ref([])
const parameterOptions = ref([])
const selectedTool = ref(null)
const selectedParameters = ref([])
const options = ref()
const healthParameterType = 'HEALTH'

const updateYear = () => {
  yearPickerKey.value = Date.now()
}
const loadTools = async () => {
  toolOptions.value = await ToolService.getTools()
  if (toolOptions.value.length > 0) {
    selectedTool.value = toolOptions.value[0]
    await loadParametersByTool()
  }
}
const loadParametersByTool = async () => {
  parameterOptions.value = await ParameterService.getParameters({
    toolId: selectedTool.value.id,
    isVirtual: true,
    type: healthParameterType,
  })
  selectedParameters.value = []
}

const onSearch = async () => {
  if (selectedParameters.value.length === 0) {
    ui.notify.warning(t('파라미터가 선택되지 않았습니다.'))
    return
  }
  const parameterIds = selectedParameters.value.map((value) => value.id)
  const to = new Date(toYear.value, 11, 31, 23, 59, 59, 999)
  const from = new Date(toYear.value, 0, 1, 0, 0, 0, 0)
  const monthlyParameterData = await HealthDataService.getMonthlyForecastHealthData(parameterIds, from, to)

  createChart(monthlyParameterData)
}

const createChart = (monthlyParameterData) => {
  const months = [
    t('1월'),
    t('2월'),
    t('3월'),
    t('4월'),
    t('5월'),
    t('6월'),
    t('7월'),
    t('8월'),
    t('9월'),
    t('10월'),
    t('11월'),
    t('12월'),
  ]

  const labelOption = {
    show: true,
    rotate: 90,
    align: 'left',
    verticalAlign: 'middle',
    position: 'insideBottom',
    distance: 15,
    formatter: '{c}  {name|{a}}',
    fontSize: 16,
    rich: { name: {} },
  }

  const xAxisData = months.slice(0, 12)
  const seriesData = { value: {} }
  let maxMonth = 0

  if (monthlyParameterData.length !== 0) {
    monthlyParameterData.forEach((item) => {
      item.parameters.forEach((nameData) => {
        if (!seriesData.value[nameData.name]) {
          seriesData.value[nameData.name] = Array(months.length).fill(null)
        }
        seriesData.value[nameData.name][item.month - 1] = nameData.score
      })
    })

    maxMonth = Math.max(...monthlyParameterData.filter((months) => !months.forecast).map((item) => item.month))
  } else {
    ui.notify.warning(t('조회된 데이터가 없습니다.'))
  }

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
    },
    legend: {
      data: Object.keys(seriesData.value).map((name) => name.toString()),
    },
    toolbox: {
      show: true,
      orient: 'vertical',
      left: 'right',
      top: 'center',
    },
    xAxis: [
      {
        type: 'category',
        axisTick: { show: false },
        data: xAxisData,
      },
    ],
    yAxis: [
      {
        type: 'value',
        max: 100,
      },
    ],
    series: Object.keys(seriesData.value).map((name) => ({
      name: pt(name),
      type: 'bar',
      label: labelOption,
      emphasis: { focus: 'series' },
      data: seriesData.value[name],
      markArea: {
        silent: true,
        data: [[{ name: t('예측 구간'), xAxis: months[maxMonth] }, { xAxis: 'max' }]],
        itemStyle: { color: 'rgba(250, 207, 207)' },
      },
    })),
  }

  options.value = option
}

loadTools()
</script>
