<template>
  <q-page class="q-pa-sm parameterReportpage">
    <div class="row">
      <q-card class="col-4">
        <q-card-section class="col search_section_wrap_col">
          <q-form ref="form">
            <q-item class="col-4">
              <DateTimeRangeSection v-model:from-date="fromDateTime" v-model:to-date="toDateTime" />
            </q-item>
            <q-item class="col-4">
              <q-item-section>
                <filterable-select
                  outlined
                  v-model="selectedType"
                  :options="typeOptions"
                  option-value="id"
                  option-label="name"
                  :rules="[$rules.required]"
                  :label="$t('통계기준')"
                >
                  <template v-slot:prepend>
                    <q-icon name="dataset" />
                  </template>
                </filterable-select>
              </q-item-section>
              <q-item-section>
                <filterable-select
                  outlined
                  v-model="selectedTool"
                  :options="toolOptions"
                  option-value="id"
                  option-label="name"
                  @update:model-value="loadParametersByTool"
                  :rules="[$rules.required]"
                  :label="$t('설비')"
                >
                  <template v-slot:prepend>
                    <q-icon name="construction" />
                  </template>
                  <template v-slot:no-option>
                    <q-item>
                      <q-item-section class="text-grey"> {{ $t('선택 가능한 설비가 없습니다.') }}</q-item-section>
                    </q-item>
                  </template>
                </filterable-select>
              </q-item-section>
            </q-item>

            <q-item class="row-3">
              <q-item-section>
                <q-table
                  bordered
                  :rows="rows"
                  :columns="columns"
                  row-key="name"
                  selection="multiple"
                  virtual-scroll
                  style="height: 566px"
                  :rows-per-page-options="[0]"
                  v-model:selected="selectedParameter"
                >
                  <template v-slot:header="props">
                    <q-tr :props="props">
                      <q-th v-for="col in props.cols" :key="col.name" :props="props">
                        {{ col.label }}
                      </q-th>
                    </q-tr>
                  </template>
                  <template v-slot:body="props">
                    <q-tr class="cursor-pointer" :props="props" @click="props.selected = !props.selected">
                      <q-td v-for="col in props.cols" :key="col.name" :props="props">
                        {{ col.value }}
                      </q-td>
                    </q-tr>
                  </template>
                </q-table>
                <q-btn @click="onSearch" class="search_btn with_icon_btn q-mt-md">{{ $t('조회') }}</q-btn>
              </q-item-section>
            </q-item>
          </q-form>
        </q-card-section>
      </q-card>
      <q-card class="col-8 chart-body">
        <q-card-section class="q-pb-none">
          <q-chip
            v-for="(value, name) in legendData"
            :key="name"
            clickable
            @click="toggleCustomLegend(name)"
            :color="value.isChecked ? 'primary' : 'dark'"
            text-color="white"
            >{{ $pt(name) }}
          </q-chip>
        </q-card-section>
        <q-card-section>
          <ParameterReportChartBody
            :parameters="parameters"
            :from-date="fromDate"
            :to-date="toDate"
            :period-type="periodType"
            :checked-parameter-ids="checkedParameterIds"
          />
        </q-card-section>
      </q-card>
    </div>
  </q-page>
</template>

<script setup>
import ToolService from 'src/services/modeling/ToolService'
import { pt, t } from 'src/plugins/i18n'
import { onMounted, ref, watch } from 'vue'
import { date } from 'quasar'
import useUI from 'src/common/module/ui'
import ParameterService from 'src/services/modeling/ParameterService'
import ParameterReportChartBody from 'pages/waterQuality/parameterReport/ParameterReportChartBody.vue'
import { PARAMETER_DATA_TYPE, PARAMETER_TYPE } from 'src/common/constant/parameter'
import DateTimeRangeSection from 'components/form/dateTime/DateTimeRangeSection.vue'

const ui = useUI()

const now = new Date()
const initFromDateTime = date.subtractFromDate(Date.now(), { day: 60 })
const initToDateTime = now

const fromDateTime = ref(initFromDateTime)
const toDateTime = ref(initToDateTime)

const toolOptions = ref()
const selectedTool = ref()
const selectedType = ref()
const selectedParameter = ref([])

const legendData = ref()
const checkedParameterIds = ref()
const fromDate = ref(initFromDateTime)
const toDate = ref(initToDateTime)
const parameters = ref([])
const periodType = ref()

const form = ref(null)
const periodTypeJson = { daily: 'DAILY', hourly: 'HOURLY' }

const typeOptions = ref([
  { id: periodTypeJson.daily, name: t('일별') },
  { id: periodTypeJson.hourly, name: t('시간별') },
])

const columns = ref([
  {
    name: 'name',
    label: t('파라미터명'),
    align: 'left',
    field: 'name',
    sortable: true,
    format: (val) => pt(val),
  },
  {
    name: 'dataType',
    label: t('데이터 타입'),
    align: 'left',
    field: 'dataType',
    sortable: true,
  },
])
const rows = ref([])

onMounted(() => {
  loadTools()
})

const loadTools = async () => {
  selectedType.value = typeOptions.value[0]
  toolOptions.value = await ToolService.getTools()
  if (toolOptions.value.length > 0) {
    selectedTool.value = toolOptions.value[0]
    await loadParametersByTool()
  }
}
const loadParametersByTool = async () => {
  selectedParameter.value = []
  const parameters = await ParameterService.getParameters({
    toolId: selectedTool.value.id,
    type: PARAMETER_TYPE.TRACE,
    dataTypes: PARAMETER_DATA_TYPE.NUMERIC_TYPES,
  })
  rows.value = parameters.filter((parameter) => parameter.dataType !== 'STRING')
}

const onSearch = async () => {
  const success = await form.value.validate()
  if (!success || selectedParameter.value.length === 0) {
    ui.notify.warning(t('필수값이 선택되지 않았습니다.'))
    return
  }

  parameters.value = selectedParameter.value.map((parameter) => {
    return { id: parameter.id, name: parameter.name }
  })
  fromDate.value = fromDateTime.value
  toDate.value = toDateTime.value
  periodType.value = selectedType.value.id

  legendData.value = parameters.value.reduce((acc, parameter) => {
    acc[parameter.name] = { isChecked: true, id: parameter.id }
    return acc
  }, {})
}
watch(
  () => legendData.value,
  () => {
    checkedParameterIds.value = Object.keys(legendData.value).map((k) => {
      if (legendData.value[k].isChecked === true) return legendData.value[k].id
    })
  },
  { deep: true },
)

const toggleCustomLegend = (name) => {
  legendData.value[name].isChecked = !legendData.value[name].isChecked
}
</script>
<style scoped>
.parameterReportpage .row {
  gap: 10px;
}

.parameterReportpage .row label.q-field {
  height: 100%;
}

.parameterReportpage .row .col-8 {
  width: calc(66.6667% - 10px);
}
</style>
