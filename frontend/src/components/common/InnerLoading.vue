<template>
  <q-inner-loading :showing="modelValue" class="inner-loading">
    <q-spinner-dots :size="size" :color="color" />
  </q-inner-loading>
</template>

<script setup>
import { defineProps } from 'vue'
const { modelValue, size } = defineProps({
  modelValue: {
    type: Boolean,
    default: false,
  },
  size: {
    type: [String, Number],
    default: 70,
  },
  color: {
    type: String,
    default: 'primary',
  },
})
</script>

<style lang="scss" scoped>
.inner-loading {
  background: rgba(0, 0, 0, 0.55); /* 배경색과 투명도 조정 */
}
</style>
