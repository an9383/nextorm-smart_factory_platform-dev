<template>
  <q-dialog :model-value="modelValue" persistent>
    <q-card class="pop-add-widget">
      <q-card-section class="row">
        <div class="text-h6">{{ $t('위젯 추가') }}</div>
        <q-space />
        <q-btn icon="close" flat round dense @click="close" />
      </q-card-section>
      <q-card-section class="row items-center">
        <!-- 위젯 목록 -->
        <q-list class="full-width" separator>
          <q-item
            v-for="widget in filteredWidgets"
            clickable
            v-ripple
            :key="widget.id"
            :active="selectedWidgets.has(widget.id)"
            @click="toggleWidgetItem(widget.id)"
            active-class="selected"
          >
            <q-item-section thumbnail>
              <img :src="widget.capture" />
            </q-item-section>
            <q-item-section>
              <q-item-label>{{ widget.name }}</q-item-label>
              <q-item-label caption>{{ widget.description }}</q-item-label>
            </q-item-section>
          </q-item>
        </q-list>
      </q-card-section>

      <q-card-actions align="right">
        <q-btn flat :label="$t('취소')" color="negative" @click="close" />
        <q-btn flat :label="$t('추가')" color="primary" @click="confirm" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup>
import { ref, defineProps, defineEmits, watch } from 'vue'
import allWidgets, { WIDGET_ALL_PERMIT_ENV_NAMES_SET } from '/src/common/constant/widgets'

const CURRENT_ENV_NAME = import.meta.env?.VITE_ENV_NAME

const widgets = ref(allWidgets)
const selectedWidgets = ref(new Set())

const props = defineProps({
  modelValue: Boolean,
})
const emit = defineEmits(['confirm'])

watch(
  () => props.modelValue,
  () => {
    selectedWidgets.value = new Set()
  },
)

const filteredWidgets = widgets.value.filter((widget) => {
  const permitEnvs = widget.permitEnvs
  // permitEnv 설정이 없으면 모두 허용
  if (!permitEnvs || !Array.isArray(permitEnvs) || permitEnvs.length === 0) {
    return true
  }

  // 모든 위젯을 허용하는 환경인 경우 허용
  if (WIDGET_ALL_PERMIT_ENV_NAMES_SET.has(CURRENT_ENV_NAME)) {
    return true
  }

  return permitEnvs.includes(CURRENT_ENV_NAME)
})

const toggleWidgetItem = (widgetId) => {
  if (selectedWidgets.value.has(widgetId)) {
    selectedWidgets.value.delete(widgetId)
  } else {
    selectedWidgets.value.add(widgetId)
  }
}

const close = () => {
  emit('update:model-value', false)
}
const confirm = () => {
  emit('confirm', Array.from(selectedWidgets.value))
  close()
}
</script>
<style lang="scss" scoped>
.pop-add-widget {
  width: 700px;
}
.q-item.selected {
  background: #0054ff33;
}
</style>
