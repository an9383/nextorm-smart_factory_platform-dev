<template>
  <widget-container :widget-id="WIDGET_ID" :title="TITLE" :config="widgetConfig" />
</template>

<script setup>
import WidgetContainer from 'components/dashboard/WidgetContainer.vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const WIDGET_ID = 'realtimeParameterValues'
const TITLE = t('실시간 배터리 충전량')

const props = defineProps({
  parameterId: Number,
})

const widgetConfig = {
  interval: 'SECS',
  intervalSecs: 1,
  period: 30,
  viewCount: 5,
  parameterId: props.parameterId,
}
</script>

<style scoped></style>
