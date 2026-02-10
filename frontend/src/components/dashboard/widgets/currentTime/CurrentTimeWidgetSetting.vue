<template>
  <q-card flat bordered>
    <q-card-section class="bg-primary text-white">
      <div class="text-subtitle2">
        <q-icon name="schedule" color="white" class="q-mr-xs" size="20px" />
        {{ $t('시간 형식') }}
      </div>
      <div class="text-caption">{{ $t('시간 형식을 선택해 주세요.') }}</div>
    </q-card-section>
    <q-card-section class="q-ma-sm">
      <q-option-group v-model="config.timeFormat" :options="timeFormatOptions" color="primary" type="radio" />
    </q-card-section>
  </q-card>
</template>

<script setup>
import { ref } from 'vue'
import { useWidgetSettingConfig, widgetSettingProps } from 'src/common/module/widgetCommon'

defineProps(widgetSettingProps)

const config = useWidgetSettingConfig({
  timeFormat: 'full',
})

const timeFormatOptions = ref([
  { label: '연/월/일/시/분/초 (YYYY.MM.DD HH:mm:ss)', value: 'full' },
  { label: '월/일/시/분/초 (MM.DD HH:mm:ss)', value: 'monthToSecond' },
  { label: '일/시/분/초 (DD HH:mm:ss)', value: 'dayToSecond' },
])
</script>
