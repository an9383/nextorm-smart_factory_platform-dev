<template>
  <q-page class="q-pa-sm column">
    <q-card class="cust_subPage">
      <q-card-section class="row search_section_wrap">
        <q-item class="col-8 search_left">
          <date-time-range-section v-model:to-date="toDateTime" v-model:from-date="fromDateTime" />
          <q-item-section>
            <filterable-select
              outlinedq-select
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
            <q-btn @click="onSearch" class="search_btn with_icon_btn sBtn">
              {{ $t('조회') }}
            </q-btn>
          </q-item-section>
        </q-item>
      </q-card-section>
      <q-card-section class="q-pa-none">
        <simple-table
          ref="tableRef"
          v-model:pagination="pagination"
          :columns="columns"
          :rows="rows"
          row-key="traceAt"
          flat
          @request="onRequest"
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
import useUI from 'src/common/module/ui'
import { pt, t } from '/src/plugins/i18n'
import { isDateTimeRangeOver } from 'src/common/utils'
import { DATE_TIME_FORMAT } from 'src/common/constant/format'
import { useDateTimeRange } from 'src/common/module/validation'
import { PARAMETER_DATA_TYPE } from 'src/common/constant/parameter'
import ParameterDataService from 'src/services/parameterData/ParameterDataService'
import DateTimeRangeSection from 'components/form/dateTime/DateTimeRangeSection.vue'

const ui = useUI()

const now = new Date()

const tableRef = ref()

const fromDateTime = ref(date.subtractFromDate(Date.now(), { minutes: 10 }))
const toDateTime = ref(now)
const toolOptions = ref([])
const parameterOptions = ref([])
const selectedTool = ref(null)
const selectedParameter = ref([])
const rows = ref([])
const pagination = ref({ page: 1, rowsPerPage: 10, sortBy: null, descending: false })

const columns = [
  {
    name: 'traceAt',
    align: 'left',
    label: t('일시'),
    field: 'traceAt',
    format: (value) => date.formatDate(value, DATE_TIME_FORMAT),
    sortable: true,
  },
  { name: 'value', align: 'left', label: t('값'), field: 'value', sortable: true },
  { name: 'ucl', align: 'left', label: t('UCL'), field: 'ucl' },
  { name: 'lcl', align: 'left', label: t('LCL'), field: 'lcl' },
  { name: 'usl', align: 'left', label: t('USL'), field: 'usl' },
  { name: 'lsl', align: 'left', label: t('LSL'), field: 'lsl' },
]

useDateTimeRange(fromDateTime, toDateTime).onInvalid(() => {
  ui.notify.warning(t('조회기간 시작일시는 종료일시보다 작아야 합니다.'))
})

onMounted(() => {
  loadTools()
})

const loadTools = async () => {
  toolOptions.value = await ToolService.getTools()
  if (toolOptions.value.length > 0) {
    selectedTool.value = toolOptions.value[0]
    loadParametersByTool()
  }
}

const onRequest = (props) => {
  const { rowsPerPage, page, rowNumber, sortBy, descending } = props.pagination
  pagination.value.rowsPerPage = rowsPerPage
  pagination.value.page = page
  pagination.value.rowNumber = rowNumber
  pagination.value.sortBy = sortBy
  pagination.value.descending = descending

  loadParameterDatas()
}

const loadParametersByTool = async () => {
  selectedParameter.value = []
  const parameters = await ParameterService.getParameters({
    toolId: selectedTool.value.id,
    dataTypes: PARAMETER_DATA_TYPE.ALPHANUMERIC_TYPES,
  })
  parameterOptions.value = parameters.map((parameter) => {
    return {
      ...parameter,
      name: pt(parameter.name),
    }
  })

  if (parameterOptions.value.length > 0) {
    selectedParameter.value = parameterOptions.value[0]
  }
}

const onSearch = () => {
  pagination.value.page = 1 // 페이지 초기화
  pagination.value.rowsPerPage = 10
  pagination.value.sortBy = null

  tableRef.value.$tableRef.requestServerInteraction()
}

const loadParameterDatas = async () => {
  if (selectedParameter.value == null) {
    ui.notify.warning(t('파라미터 값은 필수입니다!'))
    return
  }
  const dateRange = 12
  //quasar date 의 DateUnitOptions
  const dateType = 'months'
  // from, to 의 dateRange와 dateType 기준으로 초단위 초과 여부 체크
  if (isDateTimeRangeOver(fromDateTime, toDateTime, dateRange, dateType)) {
    const translateDateType = t(dateType)
    ui.notify.warning(
      t('조회 범위는 {dateRange} {translateDateType} 를(을) 초과 할 수 없습니다.', {
        dateRange,
        translateDateType,
      }),
    )
    return
  }
  const sortBy = getSortBy(pagination.value.sortBy)
  const descending = pagination.value.descending ? 'desc' : 'asc'

  const params = {
    from: new Date(fromDateTime.value),
    to: new Date(toDateTime.value),
    page: pagination.value.page - 1,
    size: pagination.value.rowsPerPage < 1 ? pagination.value.rowNumber : pagination.value.rowsPerPage,
    sort: `${sortBy},${descending}`,
  }
  ui.loading.show()
  const parameterDatas = await ParameterDataService.getParameterDatasByPageable(selectedParameter.value.id, params)
  pagination.value.rowsNumber = parameterDatas.totalElements
  rows.value = parameterDatas.content
  ui.loading.hide()
}

const getSortBy = (sortBy) => {
  if (sortBy === 'value') {
    if (selectedParameter.value.dataType === 'INTEGER') {
      return 'iValue'
    } else if (selectedParameter.value.dataType === 'DOUBLE') {
      return 'dValue'
    } else {
      return 'sValue'
    }
  } else {
    return sortBy === null ? 'traceAt' : sortBy
  }
}
</script>

<style lang="scss"></style>
