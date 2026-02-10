<template>
  <widget-container :widget-id="WIDGET_ID" :title="TITLE" :config="widgetConfig" />
</template>

<script setup>
import WidgetContainer from 'components/dashboard/WidgetContainer.vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const WIDGET_ID = 'parameterDataTrend'
const TITLE = t('배터리 전압/충전량 트렌드')

const props = defineProps({
  parameterIds: Array,
})

const widgetConfig = {
  interval: 'SECS',
  intervalSecs: 2,
  period: 30,
  parameterIds: props.parameterIds,
}
</script>

<style scoped></style>
