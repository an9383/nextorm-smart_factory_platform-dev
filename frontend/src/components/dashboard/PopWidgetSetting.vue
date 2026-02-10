<template>
  <q-dialog :model-value="modelValue" persistent maximized>
    <q-card class="pop-widget-setting">
      <!-- 헤더 영역 -->
      <q-card-section class="row bg-secondary text-white" style="flex-shrink: 0">
        <div class="text-h6">{{ props.widgetName }} {{ $t('위젯 설정') }}</div>
        <q-space />
        <q-btn icon="close" flat round dense @click="close" />
      </q-card-section>

      <!-- 바디 영역 -->
      <q-card-section class="setting-list overflow-auto" style="flex: 1">
        <q-form ref="form">
          <!-- 위젯 공통 설정(새로고침 주기) -->
          <q-card flat bordered>
            <q-card-section class="bg-primary text-white">
              <div class="text-subtitle2">
                <q-icon name="refresh" color="white" class="q-mr-xs" size="20px" />
                {{ $t('새로고침 주기') }}
              </div>
              <div class="text-caption">{{ $t('위젯 데이터가 설정한 주기마다 새로고침 됩니다.') }}</div>
            </q-card-section>
            <q-card-section class="row items-center q-my-sm">
              <q-radio v-model="widgetConfig.interval" :val="1800" :label="$t('30분')" />
              <q-radio v-model="widgetConfig.interval" :val="600" :label="$t('10분')" />
              <q-radio v-model="widgetConfig.interval" :val="300" :label="$t('5분')" />
              <q-radio v-model="widgetConfig.interval" :val="180" :label="$t('3분')" />
              <q-radio v-model="widgetConfig.interval" :val="60" :label="$t('1분')" />
              <q-radio v-model="widgetConfig.interval" :val="'SECS'" />
              <filterable-select
                v-if="widgetConfig.interval === 'SECS'"
                filled
                v-model="widgetConfig.intervalSecs"
                :options="Array.from({ length: 59 }, (_, index) => index + 1)"
                :placeholder="$t('초')"
                dense
                class="widthUnset"
              />
              <div class="column justify-center q-ml-xs">{{ $t('초') }}</div>
            </q-card-section>
          </q-card>
          <!-- 각 위젯의 설정 -->
          <component :is="settingComponent" v-model="widgetConfig" />
        </q-form>
      </q-card-section>

      <!-- 푸터 영역 -->
      <q-card-actions align="right" class="q-pa-md" style="flex-shrink: 0">
        <q-btn flat :label="$t('취소')" color="negative" @click="close" />
        <q-btn flat type="submit" :label="$t('저장')" color="primary" @click="confirm" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup>
import { defineEmits, defineProps, ref, toRaw, watch } from 'vue'
import useUI from 'src/common/module/ui'
import { t } from '/src/plugins/i18n'

const props = defineProps({
  modelValue: Boolean,
  widgetName: String,
  settingComponent: Object,
  config: Object,
})

const ui = useUI()
const form = ref(null) //for validation

const widgetConfig = ref({
  interval: 1800,
  intervalSecs: null,
  ...props.config,
})

watch(
  () => widgetConfig.value.interval,
  (newValue) => {
    if (newValue === 'seconds') {
      widgetConfig.value.intervalSecs = 59
    } else {
      widgetConfig.value.intervalSecs = null
    }
  },
)

const emit = defineEmits(['update:config'])

//위젯 설정 팝업 close
const close = () => {
  emit('update:model-value', false)
}

//위젯 설정 팝업 확인
const confirm = async () => {
  if (await form.value.validate()) {
    emit('update:config', toRaw(widgetConfig.value))
    close()
  } else {
    ui.notify.warning(t('유효하지 않은 값이 있습니다.'))
  }
}
</script>
<style lang="scss" scoped>
.pop-widget-setting {
  width: 800px;
  height: 80vh;
  display: flex;
  flex-direction: column;

  .setting-list {
    max-height: none; // flex로 관리하므로 제거
  }
}
</style>
