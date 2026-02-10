<template>
  <q-item-section>
    <DateTime v-if="fromDate" v-model="fromDate" :label="fromLabel" />
  </q-item-section>
  <q-item-section>
    <DateTime v-if="toDate" v-model="toDate" :label="toLabel" />
  </q-item-section>
</template>

<script setup>
import { useDateTimeRange } from 'src/common/module/validation'
import { t } from 'src/plugins/i18n'
import DateTime from 'components/form/dateTime/DateTime.vue'
import useUI from 'src/common/module/ui'
import { date } from 'quasar'

defineProps({
  fromLabel: {
    type: String,
    required: false,
    default: () => t('조회 시작일시'),
  },
  toLabel: {
    type: String,
    required: false,
    default: () => t('조회 종료일시'),
  },
})

const fromDate = defineModel('fromDate', {
  type: Date,
  required: false,
  default: () => date.subtractFromDate(Date.now(), { minutes: 10 }),
})

const toDate = defineModel('toDate', {
  type: Date,
  required: false,
  default: () => new Date(),
})

const ui = useUI()

if (!fromDate.value) {
  fromDate.value = date.subtractFromDate(Date.now(), { minutes: 10 })
}

if (!toDate.value) {
  toDate.value = new Date()
}

useDateTimeRange(fromDate, toDate).onInvalid(() => {
  ui.notify.warning(t('조회기간 시작일시는 종료일시보다 작아야 합니다.'))
})
</script>

<style scoped></style>
