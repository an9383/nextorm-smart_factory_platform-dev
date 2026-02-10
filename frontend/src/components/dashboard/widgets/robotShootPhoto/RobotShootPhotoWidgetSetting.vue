<template>
  <card-option-select
    v-model="config.toolId"
    :options="tools"
    :title="t('설비')"
    :sub-title="t('설비를 선택해 주세요.')"
    :placeholder="t('설비')"
    :required="true"
  />
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useWidgetSettingConfig, widgetSettingProps } from '/src/common/module/widgetCommon'

import ToolService from 'src/services/modeling/ToolService'
import CardOptionSelect from 'components/dashboard/widgets/CardOptionSelect.vue'

defineProps(widgetSettingProps)

const { t } = useI18n()
const config = useWidgetSettingConfig({
  period: 30,
})
const tools = ref([])

const loadTools = async () => {
  tools.value = await ToolService.getTools()
  if (tools.value.length !== 0 && !config.value.toolId) {
    config.value.toolId = tools.value[0].id
  }
}

onMounted(() => {
  loadTools()
})
</script>
