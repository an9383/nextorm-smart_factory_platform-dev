<template>
  <card-option-select
    v-model="config.toolId"
    :options="tools"
    :title="$t('설비')"
    :sub-title="$t('설비를 선택해 주세요.')"
    :placeholder="$t('설비')"
    :required="true"
  />
  <card-option-select
    v-model="config.parameterId"
    :options="parameters"
    :title="$t('파라미터')"
    :sub-title="$t('파라미터를 선택해 주세요.')"
    :placeholder="$t('파라미터')"
    :required="true"
  />
  <q-card flat bordered>
    <q-card-section class="bg-secondary text-white">
      <div class="text-subtitle2 field-required">
        <q-icon name="construction" color="white" class="q-mr-xs" size="20px" />
        {{ $t('최대 데이터 조회수') }}
      </div>
      <div class="text-caption">{{ $t('표시할 최대 데이터 개수를 선택해주세요 (1 ~ 100)') }}</div>
    </q-card-section>
    <q-card-section class="q-ma-sm hasCustom-input">
      <q-input
        v-model.number="config.viewCount"
        type="number"
        :placeholder="$t('데이터 조회 수를 입력해주세요')"
        dense
        standout="bg-grey-8 text-white"
        :rules="[$rules.required, $rules.min(1), $rules.max(100)]"
        class="unUnderline inputNum"
      />
    </q-card-section>
  </q-card>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useWidgetSettingConfig, widgetSettingProps } from '/src/common/module/widgetCommon'
import { pt } from '/src/plugins/i18n'

import ToolService from 'src/services/modeling/ToolService'
import ParameterService from 'src/services/modeling/ParameterService'
import CardOptionSelect from 'components/dashboard/widgets/CardOptionSelect.vue'

defineProps(widgetSettingProps)

const config = useWidgetSettingConfig({
  period: 30,
  viewCount: 10,
})
const tools = ref([])
const parameters = ref([])

const loadTools = async () => {
  tools.value = await ToolService.getTools()
  if (tools.value.length !== 0 && !config.value.toolId) {
    config.value.toolId = tools.value[0].id
  }
}

const loadParameters = async (toolId) => {
  const parameterList = await ParameterService.getParameters({ toolId })
  parameters.value = parameterList.map((it) => ({ ...it, name: pt(it.name) }))
  if (parameters.value.length !== 0 && !config.value.parameterId) {
    config.value.parameterId = parameters.value[0].id
  }
  if (parameters.value.length === 0) {
    config.value.parameterId = null
  }
}

watch(
  () => config.value.toolId,
  (newValue) => {
    //설비 변경 시 설비 하위의 파라미터 목록 조회
    config.value.parameterId = []
    parameters.value = []
    loadParameters(newValue)
  },
)

onMounted(() => {
  loadTools()
  if (config.value.toolId) {
    loadParameters(config.value.toolId)
  }
})
</script>
