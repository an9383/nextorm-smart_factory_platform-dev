<template>
  <q-page class="q-pa-sm">
    <q-card class="cust_subPage">
      <q-card-section class="row search_section_wrap">
        <q-item class="col-11 search_left grid-0404-full">
          <DateTimeRangeSection v-model:from-date="fromDateTime" v-model:to-date="toDateTime" />
          <q-item-section class="parameter-select">
            <filterable-select
              outlined
              v-model="selectedParameters"
              :options="parameterOptions"
              option-value="id"
              option-label="name"
              :label="$t('파라미터')"
              max-values="10"
              multiple
              stack-label
              use-chips
            >
              <template v-slot:option="scope">
                <q-item-label v-if="scope.opt.isHeader" header class="text-weight-bold">{{
                  $t(scope.opt.label)
                }}</q-item-label>
                <q-item v-else v-bind="scope.itemProps">
                  <q-item-section>
                    <span class="parameter-option q-ml-md">
                      <span class="parameter-name">{{ scope.opt.name }}</span>
                    </span>
                  </q-item-section>
                </q-item>
              </template>
              <template v-slot:selected-item="scope">
                <q-chip
                  removable
                  dense
                  @remove="scope.removeAtIndex(scope.index)"
                  class="modern-parameter-chip"
                  color="white"
                  text-color="primary"
                  :outline="false"
                >
                  <q-chip class="tool-chip-glass" dense>
                    {{ $t(scope.opt.toolName) }}
                  </q-chip>
                  <span class="separator-modern">•</span>
                  <span class="parameter-name-modern">{{ $t(scope.opt.name) }}</span>

                  <q-tooltip class="bg-dark text-white" anchor="top middle" self="bottom middle">
                    {{ $t(scope.opt.name) }}
                  </q-tooltip>
                </q-chip>
              </template>
              <template v-slot:prepend>
                <q-icon name="dataset" />
              </template>
              <template v-slot:no-option>
                <q-item>
                  <q-item-section class="text-grey"> {{ $t('선택 가능한 파라미터가 없습니다.') }}</q-item-section>
                </q-item>
              </template>

              <!-- 파라미터 필드 전체 호버 툴팁 -->
              <q-tooltip
                v-if="selectedParameters && selectedParameters.length > 0"
                class="bg-blue-4 text-white parameter-list-tooltip"
                anchor="bottom right"
                self="top middle"
                :offset="[0, 10]"
              >
                <div class="tooltip-header">선택된 파라미터 ({{ selectedParameters.length }}개)</div>
                <div class="tooltip-divider"></div>
                <div v-for="param in selectedParameters" :key="param.id" class="tooltip-item">
                  <span class="tooltip-tool-name">{{ $t(param.toolName) }}</span>
                  <span class="tooltip-separator">•</span>
                  <span class="tooltip-param-name">{{ $t(param.name) }}</span>
                </div>
              </q-tooltip>
            </filterable-select>
          </q-item-section>
        </q-item>
        <q-item class="col-1 search_right">
          <q-item-section>
            <q-btn @click="onSearch" class="search_btn with_icon_btn sBtn">{{ $t('조회') }}</q-btn>
          </q-item-section>
        </q-item>
      </q-card-section>
      <q-card-section class="full-height">
        <div class="row full-height">
          <div class="col-9 full-height">
            <trend-chart :datas="trendData"></trend-chart>
          </div>
          <div class="col-3 column">
            <q-card class="col q-mb-xs" bordered flat>
              <q-card-section style="height: 88%">
                <div class="text-subtitle1 text-primary text-bold">{{ $t('정규분포') }}</div>
                <trend-chart :datas="distributionData"></trend-chart>
              </q-card-section>
            </q-card>
            <q-card class="col q-mb-xs" bordered flat>
              <q-card-section style="height: 88%">
                <div class="text-subtitle1 text-red text-bold">{{ $t('스펙 아웃') }}</div>
                <bar-chart :datas="specOutCntData"></bar-chart>
              </q-card-section>
            </q-card>
          </div>
        </div>
      </q-card-section>
    </q-card>
  </q-page>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import ParameterService from 'src/services/modeling/ParameterService.js'
import ParameterDataService from 'src/services/parameterData/ParameterDataService.js'
import { date } from 'quasar'
import TrendChart from 'src/components/chart/TrendChart.vue'
import BarChart from 'src/components/chart/BarChart.vue'
import useUI from 'src/common/module/ui'
import { pt, t } from 'src/plugins/i18n'
import { formatDateTime } from 'src/common/utils'
import { PARAMETER_DATA_TYPE } from 'src/common/constant/parameter'
import { getColor } from 'src/common/constant/chart'
import DateTimeRangeSection from 'components/form/dateTime/DateTimeRangeSection.vue'
import FilterableSelect from 'components/common/FilterableSelect.vue'

const ui = useUI()

const now = new Date()

const fromDateTime = ref(date.subtractFromDate(now, { minutes: 10 }))
const toDateTime = ref(now)
const parameterOptions = ref([])
const selectedParameters = ref(null)
const trendData = ref([])
const distributionData = ref([])
const specOutCntData = ref([])

onMounted(() => {
  loadAllParameters()
})

const loadAllParameters = async () => {
  selectedParameters.value = []
  const parameters = await ParameterService.getParameters({
    dataTypes: PARAMETER_DATA_TYPE.NUMERIC_TYPES,
  })

  const groupedParams = parameters.reduce((acc, parameter) => {
    const { toolName } = parameter
    if (!acc[toolName]) {
      acc[toolName] = []
    }
    acc[toolName].push({
      ...parameter,
      name: pt(parameter.name),
    })
    return acc
  }, {})

  const options = []
  for (const toolName in groupedParams) {
    options.push({ isHeader: true, label: toolName, disable: true })
    options.push(...groupedParams[toolName])
  }
  parameterOptions.value = options
}

const loadTrend = async (paramIds, from, to, chartColors) => {
  trendData.value = []
  ui.loading.show()

  const parametersTrendData = await ParameterDataService.getMultiParameterDataTrend(paramIds, from, to)
  if (parametersTrendData.length === 0 || !parametersTrendData.find((v) => v.rawDatas.length > 0)) {
    ui.notify.warning(t('조회조건에 맞는 데이터가 없습니다.'))
  } else {
    trendData.value = parametersTrendData.map((v) => {
      const selectedParam = selectedParameters.value.find((p) => p.id === v.parameterId)
      const name = selectedParam ? `${t(selectedParam.toolName)} / ${pt(v.parameterName)}` : pt(v.parameterName)
      return {
        name,
        trendData: v.rawDatas.map((k) => ({
          x: formatDateTime(new Date(k.traceAt)),
          y: k.value,
        })),
        color: chartColors[v.parameterId],
      }
    })
  }
  ui.loading.hide()
}

const loadNormalDistribution = async (paramIds, from, to, chartColors) => {
  const normalDistributionData = await ParameterDataService.getParameterDataNormalDistribution(paramIds, from, to)
  distributionData.value = normalDistributionData.map((v) => {
    const selectedParam = selectedParameters.value.find((p) => p.id === v.parameterId)
    const name = selectedParam ? `${t(selectedParam.toolName)} / ${pt(v.parameterName)}` : pt(v.parameterName)
    return {
      name,
      trendData: v.distributions?.map((k) => ({
        x: k.value,
        y: k.distributionValue,
      })),
      color: chartColors[v.parameterId],
    }
  })
}

const loadSpecOutCount = async (paramIds, from, to) => {
  const parameterSpecOutCounts = await ParameterDataService.getParameterDataSpecOutCount(paramIds, from, to)
  specOutCntData.value = parameterSpecOutCounts.map((v) => ({
    category: pt(v.parameterName),
    value: v.specOutCnt,
  }))
}

const generateChartColors = (paramIds) => {
  return paramIds.reduce((prev, curr, index) => {
    prev[curr] = getColor(index)
    return prev
  }, {})
}

const onSearch = async () => {
  if (!selectedParameters.value || selectedParameters.value.length === 0) {
    ui.notify.warning(t('파라미터 값은 필수입니다!'))
    return
  }

  const paramIds = selectedParameters.value.map((v) => v.id)
  const from = new Date(fromDateTime.value).toISOString()
  const to = new Date(toDateTime.value).toISOString()

  const chartColors = generateChartColors(paramIds)

  loadTrend(paramIds, from, to, chartColors)
  loadNormalDistribution(paramIds, from, to, chartColors)
  loadSpecOutCount(paramIds, from, to)
}
</script>

<style lang="scss">
.parameter-select {
  width: 500px;
}

/* 파라미터 목록 툴팁 스타일 */
.parameter-list-tooltip {
  max-width: 400px !important;

  .tooltip-header {
    font-weight: 600;
    font-size: 14px;
    margin-bottom: 8px;
    text-align: center;
  }

  .tooltip-divider {
    height: 1px;
    background: rgba(255, 255, 255, 0.3);
    margin: 8px 0;
  }

  .tooltip-item {
    display: flex;
    align-items: center;
    margin-bottom: 6px;
    padding: 4px 0;

    &:last-child {
      margin-bottom: 0;
    }
  }

  .tooltip-tool-name {
    background: rgba(255, 255, 255, 0.2);
    color: white;
    padding: 2px 6px;
    border-radius: 8px;
    font-size: 11px;
    margin-right: 8px;
    min-width: 60px;
    text-align: center;
  }

  .tooltip-separator {
    color: rgba(255, 255, 255, 0.7);
    margin: 0 6px;
    font-weight: 500;
  }

  .tooltip-param-name {
    color: white;
    font-size: 12px;
    font-weight: 500;
    flex: 1;
  }
}

/* 모던 카드 스타일 - 글래스모피즘 설비 칩 적용 */
.modern-parameter-chip {
  border: 2px solid #4d66d2 !important;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05) !important;
  transition: all 0.3s ease !important;
  margin: 2px !important;

  .separator-modern {
    margin-right: 10px;
    overflow: hidden;
    font-weight: 500;
    font-size: 15px;
  }

  .parameter-name-modern {
    font-weight: 600;
    color: var(--q-primary);
    font-size: 12px;
  }
}

.tool-chip-glass {
  background: #ff5370 !important;
  color: white !important;
  font-size: 12px !important;
  margin-bottom: 4px !important;
  min-height: 20px !important; /* 높이 제한 */
  max-width: 15px;
}
.q-chip--dense {
  min-height: 26px; /* 높이도 약간 축소 */
  max-width: 300px;
}
</style>
