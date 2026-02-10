<template>
  <filterable-select
    v-model="selectedTool"
    :options="options"
    :label="label"
    option-value="id"
    option-label="name"
    outlined
    v-bind="$attrs"
  >
    <template v-slot:prepend>
      <q-icon name="construction" />
    </template>

    <template v-for="(_, name) in $slots" v-slot:[name]="slotProps">
      <slot key="name" :name="name" v-bind="slotProps" />
    </template>
  </filterable-select>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { t } from 'src/plugins/i18n'
import ToolService from 'src/services/modeling/ToolService'

defineProps({
  label: {
    type: String,
    default: () => t('설비'),
    require: false,
  },
})

const selectedTool = defineModel({
  type: Object,
  required: false,
  default: () => null,
})

const options = ref([])

const loadTools = async () => {
  options.value = await ToolService.getTools()
  if (options.value.length > 0 && !selectedTool.value) {
    selectedTool.value = options.value[0]
  }
}

const refresh = async () => {
  await loadTools()
}

defineExpose({
  refresh,
})

onMounted(async () => {
  await loadTools()
})
</script>
