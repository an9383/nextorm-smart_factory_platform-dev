<template>
  <q-tooltip class="formula-tooltip" anchor="center right">
    <div class="text-caption text-white desc-tooltip q-pa-sm">
      {{ description ? props.description : $t('설명 없음') }}
    </div>
  </q-tooltip>
</template>

<script setup>
const props = defineProps({
  description: {
    type: String,
    required: false,
    default: () => null,
  },
})
</script>

<style scoped>
.desc-tooltip {
  white-space: pre-line;
}
</style>
