<template>
  <q-page class="q-pa-sm column">
    <q-card class="cust_subPage column col">
      <q-card-section class="row search_section_wrap">
        <q-item class="col-8 search_left">
          <DateTimeRangeSection v-model:from-date="fromDateTime" v-model:to-date="toDateTime" />
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
              v-model="selectedParameter"
              :options="parameterOptions"
              option-value="id"
              option-label="name"
              @update:model-value="autoSearchCheck"
              :label="$t('파라미터')"
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
      <q-card-section class="column col q-pa-none">
        <ParameterTrendChart
          v-if="searchCondition.parameterId"
          :parameter-id="searchCondition.parameterId"
          :from-date-time="searchCondition.fromDateTime"
          :to-date-time="searchCondition.toDateTime"
          :table-viewable="true"
        />
      </q-card-section>
    </q-card>
  </q-page>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import ToolService from 'src/services/modeling/ToolService.js'
import ParameterService from 'src/services/modeling/ParameterService.js'
import { date } from 'quasar'
import ParameterTrendChart from './ParameterTrendChart.vue'
import useUI from 'src/common/module/ui'
import { t, pt } from 'src/plugins/i18n'
import { formatDateMinutes } from 'src/common/utils'
import { PARAMETER_DATA_TYPE } from 'src/common/constant/parameter'
import DateTimeRangeSection from 'components/form/dateTime/DateTimeRangeSection.vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const ui = useUI()

const toDay = formatDateMinutes()
const fromDay = formatDateMinutes(date.subtractFromDate(Date.now(), { minutes: 10 }))

const isSearch = ref(route.query.isSearch)
const fromDateTime = ref(route.query.fromDateTime || fromDay)
const toDateTime = ref(route.query.toDateTime || toDay)
const toolOptions = ref([])
const parameterOptions = ref([])
const selectedTool = ref(null)
const selectedParameter = ref(null)
const autoSearch = ref(true)

const routeToolId = ref(route.query.toolId)
const routeParameterId = ref(route.query.parameterId)

//조회 조건
const searchCondition = ref({
  parameterId: undefined,
  fromDateTime: fromDateTime.value,
  toDateTime: toDateTime.value,
})

onMounted(async () => {
  await loadTools()
  if (isSearch.value) {
    await onSearch()
  }
})

const loadTools = async () => {
  toolOptions.value = await ToolService.getTools()
  if (toolOptions.value.length > 0) {
    const selected = toolOptions.value.find((v) => v.id === Number(routeToolId.value))
    selectedTool.value = selected || (selectedTool.value = toolOptions.value[0])
    await loadParametersByTool()
  }
}

const loadParametersByTool = async () => {
  selectedParameter.value = []
  const parameters = await ParameterService.getParameters({
    toolId: selectedTool.value.id,
    dataTypes: PARAMETER_DATA_TYPE.NUMERIC_TYPES,
  })
  parameterOptions.value = parameters.map((parameter) => {
    return {
      ...parameter,
      name: pt(parameter.name),
    }
  })

  if (parameterOptions.value.length > 0) {
    const selected = parameterOptions.value.find((v) => v.id === Number(routeParameterId.value))
    selectedParameter.value = selected || parameterOptions.value[0]
  }
}

const autoSearchCheck = () => {
  if (autoSearch.value) {
    onSearch()
  }
}
const onSearch = async () => {
  if (selectedParameter.value == null) {
    ui.notify.warning(t('파라미터 값은 필수입니다!'))
    return
  }
  const from = new Date(fromDateTime.value)
  const to = new Date(toDateTime.value)

  searchCondition.value = {
    parameterId: selectedParameter.value.id,
    fromDateTime: from,
    toDateTime: to,
  }
}
</script>

<style lang="scss"></style>
