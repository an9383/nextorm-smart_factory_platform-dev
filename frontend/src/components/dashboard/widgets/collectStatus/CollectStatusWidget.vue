<template>
  <div class="full-width full-height chart_container no-padding column flex">
    <div class="col chartBorder rounded-borders">
      <VChart id="echart" ref="myChart" :option="options" :resizable="true"></VChart>
    </div>
    <div class="col q-pt-xs">
      <q-list bordered separator>
        <q-item v-ripple>
          <q-item-section class="text-subtitle2 text-bold text-right q-pr-md titleWidth">
            {{ $t('DCP 수집 상태') }}
          </q-item-section>
          <q-item-section
            v-if="collectData.dcpGoodCnt !== null && collectData.dcpGoodCnt !== ''"
            class="value_container"
          >
            <div class="status-container">
              <q-chip outline dense color="blue" text-color="white" class="q-mr-sm">{{
                $t('양호') + ': ' + collectData.dcpGoodCnt
              }}</q-chip>
              <q-chip outline dense :color="getDcpBadCnt" text-color="white">{{
                $t('문제') + ': ' + collectData.dcpBadCnt
              }}</q-chip>
            </div>
          </q-item-section>
          <div v-else class="value_container col">{{ $t('no data') }}</div>
        </q-item>
        <q-item v-ripple>
          <q-item-section class="text-subtitle2 text-bold text-right q-pr-md titleWidth">
            {{ $t('파라미터 수집 상태') }}
          </q-item-section>
          <q-item-section
            v-if="collectData.parameterGoodCnt !== null && collectData.parameterGoodCnt !== ''"
            class="value_container"
          >
            <div class="status-container">
              <q-chip outline dense color="blue" text-color="white" class="q-mr-sm">{{
                $t('양호') + ': ' + collectData.parameterGoodCnt
              }}</q-chip>
              <q-chip outline dense :color="getParameterBadCnt" text-color="white">{{
                $t('문제') + ': ' + collectData.parameterBadCnt
              }}</q-chip>
            </div>
          </q-item-section>
          <div v-else class="value_container col">{{ $t('no data') }}</div>
        </q-item>
        <q-item v-ripple>
          <q-item-section class="text-subtitle2 text-bold text-right q-pr-md titleWidth">
            {{ $t('최근 수집 시간') }}
          </q-item-section>
          <q-item-section
            v-if="collectData.lastCollectedAtList !== null && collectData.lastCollectedAtList !== ''"
            class="value_container"
          >
            <div v-for="(lastCollectedAt, index) in collectData.lastCollectedAtList" :key="lastCollectedAt">
              <q-chip color="primary" text-color="white" size="sm">{{
                $t('DCP ID') + ': ' + collectData.dcpIds[index]
              }}</q-chip>
              <span class="text-bold">
                {{ formatDateTime(lastCollectedAt) }}
              </span>
            </div>
          </q-item-section>
          <div v-else class="value_container col">{{ $t('no data') }}</div>
        </q-item>
      </q-list>
    </div>
  </div>
</template>

<script setup>
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { PieChart } from 'echarts/charts'
import { LegendComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { formatDateTime } from 'src/common/utils'
import 'echarts-gl'
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useWidgetRefresh, widgetProps } from '/src/common/module/widgetCommon'
import WidgetService from 'src/services/widget/WidgetService.js'
import { t } from '/src/plugins/i18n'

use([PieChart, LegendComponent, TooltipComponent, CanvasRenderer])

defineProps(widgetProps)
const myChart = ref()
// const optionData = ref([])
const options = ref()

const collectData = ref({
  toolId: '',
  toolName: '',
  dcpIds: [],
  dcpGoodCnt: '',
  dcpBadCnt: '',
  parameterGoodCnt: '',
  parameterBadCnt: '',
  lastCollectedAtList: [],
  goodCollectedParameterIds: [],
  badCollectedParameterIds: [],
  goodCollectedParameterNames: [],
  badCollectedParameterNames: [],
})

onMounted(() => {
  if (myChart.value) {
    myChart.value.resize()
  }
})

onBeforeUnmount(() => {
  if (myChart.value) {
    myChart.value.dispose()
    myChart.value = null
  }
})

const getDcpBadCnt = computed(() => {
  if (collectData.value.dcpBadCnt === '' || collectData.value.dcpBadCnt === null) {
    return 'grey'
  }
  return collectData.value.dcpBadCnt === 0 ? 'grey' : 'red'
})

const getParameterBadCnt = computed(() => {
  if (collectData.value.parameterBadCnt === '' || collectData.value.parameterBadCnt === null) {
    return 'grey'
  }
  return collectData.value.parameterBadCnt === 0 ? 'grey' : 'red'
})

const refresh = async (config) => {
  const { toolId } = config

  try {
    const widget = await WidgetService.getCollectStatusWidgetData(toolId)
    collectData.value.toolId = widget.toolId
    collectData.value.toolName = widget.toolName
    collectData.value.dcpIds = widget.dcpIds
    collectData.value.dcpGoodCnt = widget.dcpGoodCnt
    collectData.value.dcpBadCnt = widget.dcpBadCnt
    collectData.value.parameterGoodCnt = widget.parameterGoodCnt
    collectData.value.parameterBadCnt = widget.parameterBadCnt
    collectData.value.lastCollectedAtList = widget.lastCollectedAtList
    collectData.value.goodCollectedParameterIds = widget.goodCollectedParameterIds
    collectData.value.badCollectedParameterIds = widget.badCollectedParameterIds
    collectData.value.goodCollectedParameterNames = widget.goodCollectedParameterNames
    collectData.value.badCollectedParameterNames = widget.badCollectedParameterNames
    setOptions(widget)
    if (myChart.value) {
      myChart.value.resize()
    }
  } catch (error) {
    collectData.value.toolId = ''
    collectData.value.toolName = ''
    collectData.value.dcpIds = []
    collectData.value.dcpGoodCnt = ''
    collectData.value.dcpBadCnt = ''
    collectData.value.parameterGoodCnt = ''
    collectData.value.parameterBadCnt = ''
    collectData.value.lastCollectedAtList = []
    collectData.value.goodCollectedParameterIds = []
    collectData.value.badCollectedParameterIds = []
    collectData.value.goodCollectedParameterNames = []
    collectData.value.badCollectedParameterNames = []
  }
}

const setOptions = (result) => {
  const good = result.parameterGoodCnt
  const bad = result.parameterBadCnt

  options.value = {
    legend: {
      orient: 'horizontal',
      left: 10,
      top: 10,
      itemGap: 10,
      textStyle: {
        color: '#353c4d',
      },
    },
    tooltip: {
      trigger: 'item',
      formatter: (params) => {
        return `${params.name}<br/>${params.value} (${params.percent}%)`
      },
    },
    series: [
      {
        type: 'pie',
        radius: '65%',
        center: ['50%', '50%'],
        data: [
          {
            name: t('양호'),
            value: good,
            itemStyle: {
              color: '#3ad33a',
            },
          },
          {
            name: t('문제'),
            value: bad,
            itemStyle: {
              color: '#FF0000',
            },
          },
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)',
          },
        },
        label: {
          show: true,
          formatter: '{b}: {c} ({d}%)',
        },
      },
    ],
  }
}
useWidgetRefresh(refresh)
</script>
<style lang="scss" scoped>
.chart_container {
  width: calc(100% - 10px);
  height: calc(100% - 20px);
  overflow-y: hidden;
  overflow-x: hidden;
  flex-direction: column;
}
.label {
  font-weight: bold;
  background: #f5f5f5;
  padding: 8px;
  border-right: 1px solid #000;
}
.status-container {
  display: flex;
  background: white;
  border-left: none;
}
.q-chip {
  font-weight: bold;
  font-size: 0.875em;
  padding: 6px 12px;
}
.titleWidth {
  max-width: 140px;
}
.value_container {
  display: flow;
  align-content: center;
}
.chartBorder {
  border: 1px solid black;
}
</style>
