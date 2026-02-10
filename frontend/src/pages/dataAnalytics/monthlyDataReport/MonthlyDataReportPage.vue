<template>
  <q-page class="q-pa-sm column">
    <q-card class="cust_subPage column col">
      <q-card-section class="row search_section_wrap">
        <q-item class="col-8 search_left">
          <q-item-section>
            <q-input filled v-model="fromYear" readonly stack-label :label="$t('조회시작년도')">
              <template v-slot:prepend>
                <q-icon name="event" class="cursor-pointer">
                  <q-popup-proxy cover transition-show="scale" transition-hide="scale">
                    <!-- 년도만 선택 가능하도록 하기 위해 key 추가 -->
                    <q-date
                      v-model="fromYear"
                      @update:model-value="updateYear"
                      :key="yearPickerKey"
                      default-view="Years"
                      mask="YYYY"
                      emit-immediately
                      minimal
                    ></q-date>
                  </q-popup-proxy>
                </q-icon>
              </template>
            </q-input>
          </q-item-section>
          <q-item-section>
            <q-input filled v-model="toYear" readonly stack-label :label="$t('조회종료년도')">
              <template v-slot:prepend>
                <q-icon name="event" class="cursor-pointer">
                  <q-popup-proxy cover transition-show="scale" transition-hide="scale">
                    <!-- 년도만 선택 가능하도록 하기 위해 key 추가 -->
                    <q-date
                      v-model="toYear"
                      @update:model-value="updateYear"
                      :key="yearPickerKey"
                      default-view="Years"
                      mask="YYYY"
                      emit-immediately
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
        <q-item class="col-4 search_right">
          <q-item-section>
            <q-btn class="search_btn with_icon_btn sBtn" @click="onSearch">{{ $t('조회') }}</q-btn>
          </q-item-section>
        </q-item>
      </q-card-section>
      <q-card-section class="column col q-pa-md">
        <MonthlyDataChart
          v-if="searchCondition.parameterId"
          :parameter-id="searchCondition.parameterId"
          :from-year="searchCondition.fromYear"
          :to-year="searchCondition.toYear"
        />
      </q-card-section>
    </q-card>
  </q-page>
</template>

<script setup>
import { ref } from 'vue'
import ToolService from 'src/services/modeling/ToolService'
import ParameterService from 'src/services/modeling/ParameterService'
import MonthlyDataChart from './MonthlyDataChart.vue'
import useUI from 'src/common/module/ui'
import { t } from 'src/plugins/i18n'
import { PARAMETER_TYPE, PARAMETER_DATA_TYPE } from 'src/common/constant/parameter'

const ui = useUI()

const toYear = ref(new Date().getFullYear())
const fromYear = ref(new Date().getFullYear() - 2)
const yearPickerKey = ref(Date.now())

const toolOptions = ref([])
const parameterOptions = ref([])
const selectedTool = ref(null)
const selectedParameters = ref(null)
//조회 조건
const searchCondition = ref({
  parameterId: undefined,
  fromYear: fromYear.value,
  toYear: toYear.value,
})

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
  selectedParameters.value = null
  parameterOptions.value = await ParameterService.getParameters({
    toolId: selectedTool.value.id,
    type: PARAMETER_TYPE.TRACE,
    dataTypes: PARAMETER_DATA_TYPE.NUMERIC_TYPES,
  })
}

const onSearch = async () => {
  if (selectedParameters.value === null) {
    ui.notify.warning(t('파라미터가 선택되지 않았습니다.'))
    return
  } else if (fromYear.value > toYear.value) {
    ui.notify.warning(t('조회기간 시작년도는 종료년도보다 작아야 합니다.'))
    return
  }

  searchCondition.value = {
    parameterId: selectedParameters.value.id,
    fromYear: fromYear.value,
    toYear: toYear.value,
  }
}
loadTools()
</script>

<style scoped></style>
