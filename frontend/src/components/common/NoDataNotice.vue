<template>
  <div class="no-data-guide flex flex-center column">
    <q-icon name="mdi-alert-circle" color="blue-grey-5" size="64px" />
    <div class="text-h6 text-blue-grey-8 q-mt-md">{{ $t(props.title) }}</div>
    <div class="text-subtitle1 text-blue-grey-6 q-mt-sm">{{ $t(props.message) }}</div>
  </div>
</template>

<script setup>
const props = defineProps({
  title: {
    type: String,
    required: true,
  },
  message: {
    type: String,
    required: true,
  },
})
</script>

<style scoped>
.no-data-guide {
  height: 100%;
  width: 100%;
  text-align: center;
  padding: 20px;
}
</style>
