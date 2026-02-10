<template>
  <q-card-section class="row search_section_wrap">
    <q-item class="search_left">
      <date-time-range-section v-model:from-date="fromDateTime" v-model:to-date="toDateTime" />
    </q-item>
    <q-item class="col-4 search_right">
      <q-item-section>
        <q-btn @click="onSearch" class="search_btn with_icon_btn sBtn">
          {{ $t('조회') }}
        </q-btn>
      </q-item-section>
    </q-item>
  </q-card-section>
  <NoDataNotice v-if="!noDataNotice.isSearch.value" :title="noDataNotice.title" :message="noDataNotice.message" />
  <div v-else>
    <q-card-section class="" style="height: 300px">
      <div class="row full-height full-width">
        <div class="full-height full-width">
          <fast-trend-chart
            :parameter-ids="fastChartParameterIds"
            :from-date="captureSearchCondition.fromDateTime"
            :to-date="captureSearchCondition.toDateTime"
            @changeChartData="handleChartDataChange"
          />
        </div>
      </div>
    </q-card-section>
    <q-card-section class="row q-col-gutter-x-md" style="height: 350px">
      <div class="col-6">
        <correlation-scatter-plot :correlation-chart-data="correlationChartData" :y-parameter="correlationYParameter" />
      </div>
      <div class="col-6">
        <correlation-rank
          :x-parameter-ids="correlationXParameterIds"
          :y-parameter="correlationYParameter"
          :from-date="captureSearchCondition.fromDateTime"
          :to-date="captureSearchCondition.toDateTime"
          @update:selected="handleCorrelationRankSelection"
        />
      </div>
    </q-card-section>
  </div>
</template>

<script setup>
import useUI from 'src/common/module/ui'
import { computed, ref, watch } from 'vue'
import { date } from 'quasar'
import DateTimeRangeSection from 'components/form/dateTime/DateTimeRangeSection.vue'
import { useI18n } from 'vue-i18n'
import FastTrendChart from 'pages/ai/modeling/parameterSelectStep/FastTrendChart.vue'
import NoDataNotice from 'components/common/NoDataNotice.vue'
import CorrelationScatterPlot from 'pages/ai/modeling/parameterSelectStep/CorrelationScatterPlot.vue'
import CorrelationRank from 'pages/ai/modeling/parameterSelectStep/CorrelationRank.vue'

// 부모 컴포넌트의 변경사항을 감지하기 위한 모델
const yParameter = defineModel('yParameter', {
  type: Object,
  required: false,
  default: null,
})

// 부모 컴포넌트의 변경사항을 감지하기 위한 모델
const xParameters = defineModel('xParameters', {
  type: Array,
  required: false,
  default: () => [],
})

const ui = useUI()
const { t } = useI18n()

//const now = '2024-03-07 12:10:00'
const fromDateTime = ref('2024-03-07 18:03:00')
const toDateTime = ref(date.subtractFromDate('2024-03-07 18:15:00', { minutes: 10 }))
// const now = new Date()
// const fromDateTime = ref(date.subtractFromDate(Date.now(), { minutes: 10 }))
// const toDateTime = ref(now)

const selectedRankRows = ref([])
const correlationChartData = ref([])

// 조회 버튼을 누른 시점의 검색조건을 저장
const captureSearchCondition = ref({
  xParameters: [],
  yParameter: null,
  fromDateTime: null,
  toDateTime: null,
})
const noDataNotice = {
  title: t('상관관계 분석'),
  message: t('조회조건을 설정한 후 조회 버튼을 클릭하여 조회해 주세요.'),
  isSearch: ref(false),
}

const fastChartParameterIds = computed(() => {
  if (!captureSearchCondition.value.yParameter) {
    return []
  }

  if (captureSearchCondition.value.xParameters.length === 0) {
    return []
  }
  return [captureSearchCondition.value.yParameter.id, ...captureSearchCondition.value.xParameters.map((v) => v.id)]
})

const correlationXParameterIds = computed(() => {
  if (!captureSearchCondition.value.yParameter) {
    return []
  }

  if (captureSearchCondition.value.xParameters.length === 0) {
    return []
  }
  return captureSearchCondition.value.xParameters.map((v) => v.id)
})

const correlationYParameter = computed(() => {
  if (!captureSearchCondition.value.yParameter) {
    return []
  }

  if (captureSearchCondition.value.xParameters.length === 0) {
    return []
  }
  return captureSearchCondition.value.yParameter
})

const cloneDeep = (obj) => JSON.parse(JSON.stringify(obj))

const onSearch = async () => {
  if (xParameters.value.length === 0) {
    ui.notify.warning(t('파라미터를 선택해주세요.'))
    return
  }

  if (!yParameter.value) {
    ui.notify.warning(t('Y값을 선택해주세요.'))
    return
  }
  ui.loading.show()
  noDataNotice.isSearch.value = true
  captureSearchCondition.value = {
    xParameters: cloneDeep(xParameters.value),
    yParameter: cloneDeep(yParameter.value),
    fromDateTime: new Date(fromDateTime.value),
    toDateTime: new Date(toDateTime.value),
  }
  ui.loading.hide()
}

const handleChartDataChange = (data) => {
  correlationChartData.value = data
}

const handleCorrelationRankSelection = (selected) => {
  selectedRankRows.value = selected
}

defineExpose({
  getData: () => {
    const { yParameter, fromDateTime, toDateTime } = captureSearchCondition.value
    if (!captureSearchCondition.value.yParameter) {
      return null
    }

    if (selectedRankRows.value.length === 0) {
      return null
    }
    return {
      selectedYParameterId: yParameter.id,
      //min max 값을 찾기 위한 id 와 name 을 같이 넘겨줌
      selectedXParameter: selectedRankRows.value.map((row) => ({
        id: row.id,
        name: row.name,
      })),
      fromDateTime,
      toDateTime,
    }
  },
})
watch(fastChartParameterIds, (newValue) => {
  if (newValue.length > 0) {
    noDataNotice.isSearch.value = true
  }
})
</script>
<style scoped>
.corr-table {
  overflow-y: scroll;
  height: 230px;
}
</style>
