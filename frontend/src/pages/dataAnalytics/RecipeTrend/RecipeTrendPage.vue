<template>
  <q-page class="q-pa-sm column">
    <q-card class="cust_subPage column col">
      <q-card-section class="row search_section_wrap">
        <q-list class="search-list col">
          <q-item class="col search_left">
            <DateTimeRangeSection v-model:from-date="fromDateTime" v-model:to-date="toDateTime" />
            <q-item-section>
              <ToolSelectBox v-model="selectedTool" @update:modelValue="handleToolChange" />
            </q-item-section>
            <q-item-section>
              <filterable-select
                :label="t('Recipe 파라미터')"
                outlined
                v-model="selectedRecipeParameter"
                :options="recipeParameterOptions"
                option-value="id"
                :option-label="(item) => $pt(item.name)"
              >
                <template v-slot:prepend>
                  <q-icon name="dataset" />
                </template>
                <template v-slot:no-option>
                  <q-item>
                    <q-item-section class="text-grey"> {{ $t('선택 가능한 파라미터가 없습니다.') }}</q-item-section>
                  </q-item>
                </template>

                <template v-for="(_, name) in $slots" v-slot:[name]="slotProps">
                  <slot key="name" :name="name" v-bind="slotProps" />
                </template>
              </filterable-select>
            </q-item-section>
            <q-item-section>
              <q-btn class="edit_btn with_icon_btn sBtn secondary" @click="handleRecipeSearchClick"
                >{{ $t('RECIPE 조회') }}
              </q-btn>
            </q-item-section>
            <q-item-section>
              <filterable-select :label="t('Recipe')" outlined v-model="selectedRecipeName" :options="recipeOptions">
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
          <q-item class="col search_left">
            <q-item-section class="col-2">
              <filterable-select
                :label="t('파라미터')"
                outlined
                v-model="selectedParameter"
                :options="parameterOptions"
                option-value="id"
                :option-label="(item) => $pt(item.name)"
              >
                <template v-slot:prepend>
                  <q-icon name="dataset" />
                </template>
                <template v-slot:no-option>
                  <q-item>
                    <q-item-section class="text-grey"> {{ $t('선택 가능한 파라미터가 없습니다.') }}</q-item-section>
                  </q-item>
                </template>

                <template v-for="(_, name) in $slots" v-slot:[name]="slotProps">
                  <slot key="name" :name="name" v-bind="slotProps" />
                </template>
              </filterable-select>
            </q-item-section>
            <q-item-section class="col-1">
              <q-btn @click="handleTrendSearchClick" class="search_btn with_icon_btn sBtn">
                {{ $t('조회') }}
              </q-btn>
            </q-item-section>
          </q-item>
        </q-list>
      </q-card-section>
      <q-card-section class="column col q-pa-none">
        <div class="q-ma-md column col">
          <RecipeTrendChartArea
            v-if="chartDataSearchCondition"
            :chart-data-search-condition="chartDataSearchCondition"
          />
        </div>
      </q-card-section>
    </q-card>
  </q-page>
</template>

<script setup>
import { ref, watch } from 'vue'
import { date } from 'quasar'
import { t } from 'src/plugins/i18n'
import DateTimeRangeSection from 'components/form/dateTime/DateTimeRangeSection.vue'
import ToolSelectBox from 'components/form/ToolSelectBox.vue'
import ParameterService from 'src/services/modeling/ParameterService'
import { PARAMETER_DATA_TYPE } from 'src/common/constant/parameter'
import ParameterDataService from 'src/services/parameterData/ParameterDataService'
import useUI from 'src/common/module/ui'
import RecipeTrendChartArea from 'pages/dataAnalytics/RecipeTrend/RecipeTrendChartArea.vue'

const ui = useUI()

const now = Date.now()
const fromDateTime = ref(date.subtractFromDate(now, { days: 1 }))
const toDateTime = ref(new Date(now))
const selectedTool = ref(null)

const selectedRecipeParameter = ref(null)
const recipeParameterOptions = ref([])

const selectedRecipeName = ref(null)
const recipeOptions = ref([])

const selectedParameter = ref(null)
const parameterOptions = ref([])

const chartDataSearchCondition = ref(null)

watch([selectedTool, fromDateTime, toDateTime], () => {
  selectedRecipeName.value = null
  recipeOptions.value = []
})

const withLoading = async (callback) => {
  ui.loading.show()
  try {
    await callback()
  } finally {
    ui.loading.hide()
  }
}

const handleToolChange = async (tool) => {
  await withLoading(async () => {
    selectedRecipeParameter.value = null
    selectedParameter.value = null
    selectedRecipeName.value = null

    recipeParameterOptions.value = await ParameterService.getParameters({
      toolId: tool.id,
      dataTypes: [PARAMETER_DATA_TYPE.STRING],
    })
    if (recipeParameterOptions.value.length > 0) {
      selectedRecipeParameter.value = recipeParameterOptions.value[0]
    }

    parameterOptions.value = await ParameterService.getParameters({
      toolId: tool.id,
      dataTypes: PARAMETER_DATA_TYPE.NUMERIC_TYPES,
    })
    if (parameterOptions.value.length > 0) {
      selectedParameter.value = parameterOptions.value[0]
    }
  })
}

const validateRecipeSearchCondition = () => {
  if (!selectedTool.value?.id) {
    ui.notify.warning(t('설비를 선택해 주세요.'))
    return false
  }
  if (!selectedRecipeParameter.value?.id) {
    ui.notify.warning(t('Recipe 파라미터를 선택해 주세요.'))
    return false
  }
  return true
}

const handleRecipeSearchClick = async () => {
  if (!validateRecipeSearchCondition()) {
    return false
  }

  await withLoading(async () => {
    selectedRecipeName.value = null
    recipeOptions.value = await ParameterDataService.getRecipeNames(
      selectedRecipeParameter.value.id,
      fromDateTime.value,
      toDateTime.value,
    )
    if (recipeOptions.value.length > 0) {
      selectedRecipeName.value = recipeOptions.value[0]
    }
  })
}

const validateTrendSearchCondition = () => {
  if (!validateRecipeSearchCondition()) {
    return false
  }

  if (!selectedRecipeName.value) {
    ui.notify.warning(t('Recipe를 선택해 주세요.'))
    return false
  }

  if (!selectedParameter.value?.id) {
    ui.notify.warning(t('파라미터를 선택해 주세요.'))
    return false
  }

  return true
}

const handleTrendSearchClick = async () => {
  if (!validateTrendSearchCondition()) {
    return false
  }

  chartDataSearchCondition.value = {
    toolId: selectedTool.value.id,
    parameterId: selectedParameter.value.id,
    recipeParameterId: selectedRecipeParameter.value.id,
    recipeName: selectedRecipeName.value,
    from: fromDateTime.value,
    to: toDateTime.value,
  }
}
</script>

<style lang="scss" scoped>
.search_section_wrap {
  height: 134px !important;
  padding: 0 !important;
}

.search-list {
  padding-top: 19px;

  .search_left:nth-child(2) {
    margin-top: 15px;
  }
}

.search_left {
  padding-left: 10px;
}
</style>
