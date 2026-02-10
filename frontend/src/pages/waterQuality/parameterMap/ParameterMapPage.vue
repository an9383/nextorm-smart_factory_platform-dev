<template>
  <q-page class="q-pa-sm column">
    <q-card class="cust_subPage column col">
      <q-card-section class="row search_section_wrap">
        <q-item class="col-8 search_left">
          <DateTimeRangeSection v-model:from-date="fromDateTime" v-model:to-date="toDateTime" />
          <q-item-section>
            <filterable-select
              v-model="selectTool"
              :options="toolOptions"
              option-value="id"
              option-label="name"
              @update:model-value="getParametersByTool"
              :label="$t('설비')"
            />
          </q-item-section>
          <q-item-section>
            <filterable-select
              v-model="selectParameter"
              :options="parameterOptions"
              option-value="id"
              :option-label="(item) => $pt(item.name)"
              @update:model-value="autoSearchCheck"
              :label="$t('파라미터')"
            />
          </q-item-section>
        </q-item>
        <q-item class="col-4 search_right">
          <q-item-section>
            <q-checkbox v-model="autoSearch" :label="$t('자동 조회')" />
          </q-item-section>
          <q-item-section>
            <q-btn @click="onSearch" class="search_btn with_icon_btn sBtn">
              {{ $t('조회') }}
            </q-btn>
          </q-item-section>
        </q-item>
      </q-card-section>
      <!-- <q-separator /> -->
      <q-card-section class="column col">
        <parameter-map-chart
          :tool-id="searchCondition.toolId"
          :parameter-id="searchCondition.parameterId"
          :from-date-time="searchCondition.fromDateTime"
          :to-date-time="searchCondition.toDateTime"
        ></parameter-map-chart>
      </q-card-section>
    </q-card>
  </q-page>
</template>

<script setup>
import { ref } from 'vue'
import ToolService from 'src/services/modeling/ToolService.js'
import ParameterService from 'src/services/modeling/ParameterService.js'
import { t } from '/src/plugins/i18n'
import { date } from 'quasar'
import ParameterMapChart from './ParameterMapChart.vue'
import useUI from 'src/common/module/ui'
import DateTimeRangeSection from 'components/form/dateTime/DateTimeRangeSection.vue'

const ui = useUI()

const toDay = date.formatDate(Date.now(), 'YYYY-MM-DD HH:mm')
const fromDay = date.formatDate(date.subtractFromDate(Date.now(), { days: 1 }), 'YYYY-MM-DD HH:mm')
// const dateRange = ref({ from: fromDay, to: toDay })

const fromDateTime = ref(fromDay)
const toDateTime = ref(toDay)

const toolOptions = ref([])
const parameterOptions = ref([])
const selectTool = ref(null)
const selectParameter = ref(null)
const autoSearch = ref(true)

//조회 조건
const searchCondition = ref({
  parameterId: undefined,
  toolId: undefined,
  fromDateTime: new Date(fromDay),
  toDateTime: new Date(toDay),
})

ToolService.getTools().then((tools) => {
  toolOptions.value = tools
  if (toolOptions.value.length > 0) {
    selectTool.value = toolOptions.value[0]
    getParametersByTool()
  }
})

const getParametersByTool = () => {
  ParameterService.getParameters({ toolId: selectTool.value.id }).then((parameters) => {
    parameterOptions.value = parameters
    if (parameterOptions.value.length > 0) {
      selectParameter.value = parameterOptions.value[0]
    }
  })
  selectParameter.value = []
}
const autoSearchCheck = () => {
  if (autoSearch.value) {
    onSearch()
  }
}

async function onSearch() {
  if (selectParameter.value == null) {
    ui.notify.warning(t('파라미터 값은 필수입니다!'))
    return
  }

  searchCondition.value = {
    toolId: selectTool.value?.id,
    parameterId: selectParameter.value?.id,
    fromDateTime: new Date(fromDateTime.value),
    toDateTime: new Date(toDateTime.value),
  }

  // ui.loading.show()

  // let layouts = await ReservoirLayoutService.getReservoirLayout({ params: { toolIds: selectTool.value.id } })
  // if (layouts.length === 0) {
  //   ui.notify.error(t('장소 GPS 정의를 먼저 하십시오'))
  //   ui.loading.hide()

  //   return
  // }
  // let data = JSON.parse(layouts[0].data)

  // markers.value = data.markers

  // const fromDate = new Date(fromDateTime.value).toISOString()
  // const toDate = new Date(toDateTime.value).toISOString()
  // ParameterDataService.getEcoParameters(selectParameter.value.id, fromDate, toDate)
  //   .then((data) => {
  //     datas.value = data.datas
  //       .filter((d) => d[1] != null && d[2] != null && d[3] != null)
  //       .map((d) => [d[3], d[2], d[1], d[0]])

  //     conditionDate.value = { fromDate: new Date(fromDate), toDate: new Date(toDate) }
  //   })
  //   .finally(() => {
  //     ui.loading.hide()
  //   })
}
</script>

<style lang="scss"></style>
