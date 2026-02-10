<template>
  <q-card flat bordered class="q-mt-md">
    <q-card-section class="bg-primary text-white">
      <div :class="requiredClass" class="text-subtitle2">
        <q-icon name="construction" color="white" class="q-mr-xs" size="20px" />
        {{ title }}
      </div>
      <div class="text-caption">{{ localSubTitle }}</div>
    </q-card-section>
    <q-card-section class="q-ma-sm">
      <q-select
        v-model="localModelValue"
        :placeholder="placeholder"
        :options="options"
        option-value="id"
        option-label="name"
        emit-value
        map-options
        filled
        dense
        lazy-rules
        multiple
        :rules="rules"
        @update:modelValue="updateValue"
      />
    </q-card-section>
  </q-card>
</template>

<script setup>
import { computed, defineEmits, defineProps, getCurrentInstance, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const props = defineProps({
  modelValue: {
    required: true,
  },
  options: {
    required: true,
    type: Array,
  },
  title: {
    type: String,
    required: true,
  },
  subTitle: {
    type: String,
    required: true,
  },
  placeholder: {
    type: String,
    required: true,
  },
  required: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['update:modelValue'])
const instance = getCurrentInstance()

const rules = props.required ? [instance.appContext.config.globalProperties.$rules.required] : []
const requiredClass = computed(() => (props.required ? 'field-required' : ''))
const localModelValue = ref(props.modelValue)

const localSubTitle = computed(() => `${props.subTitle} (` + t('옵션') + `: ${props.options.length}` + t(`개`) + `)`)

watch(
  () => props.modelValue,
  (newValue) => {
    localModelValue.value = newValue
  },
)

const updateValue = (value) => {
  localModelValue.value = value
  emit('update:modelValue', value)
}
</script>

<style scoped></style>
