<template>
  <widget-container :widget-id="WIDGET_ID" :title="TITLE" :config="widgetConfig" />
</template>

<script setup>
import WidgetContainer from 'components/dashboard/WidgetContainer.vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const WIDGET_ID = 'toolStatus'
const TITLE = t('장비 상태')

const props = defineProps({
  toolId: Number,
})

const widgetConfig = {
  interval: 'SECS',
  intervalSecs: 5,
  period: 30,
  toolId: props.toolId,
}
</script>

<style scoped></style>
