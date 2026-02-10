<template>
  <q-item-section>
    <filterable-select
      ref="selectBox"
      outlined
      v-model="selectedParameters"
      :options="options"
      option-value="id"
      :option-label="(item) => $pt(item.name)"
      :label="label"
      stack-label
      use-chips
      v-bind="$attrs"
    >
      <template v-slot:prepend>
        <q-icon name="dataset" />
      </template>
      <template v-slot:no-option>
        <q-item>
          <q-item-section class="text-grey"> {{ noOptionMessage }}</q-item-section>
        </q-item>
      </template>

      <template v-for="(_, name) in $slots" v-slot:[name]="slotProps">
        <slot key="name" :name="name" v-bind="slotProps" />
      </template>
    </filterable-select>
  </q-item-section>
</template>

<script setup>
import { t } from 'src/plugins/i18n'
import { computed, onMounted, ref, watch } from 'vue'
import ParameterService from 'src/services/modeling/ParameterService'
import { PARAMETER_DATA_TYPE, PARAMETER_TYPE } from 'src/common/constant/parameter'

const props = defineProps({
  toolId: {
    type: Number,
    required: false,
    default: null,
  },
  label: {
    type: String,
    required: false,
    default: () => t('파라미터'),
  },
  type: {
    type: Array,
    required: false,
    validator: (value) => {
      if (!value) {
        return true
      }
      const typeSet = new Set(Object.keys(PARAMETER_TYPE))
      return value.every((type) => typeSet.has(type))
    },
  },
  dataType: {
    type: Array,
    required: false,
    validator: (value) => {
      if (!value) {
        return true
      }
      const typeSet = new Set(Object.keys(PARAMETER_DATA_TYPE))
      return value.every((type) => typeSet.has(type))
    },
  },
  isVirtual: {
    type: Boolean,
    required: false,
    default: null,
  },
})

const selectedParameters = defineModel({
  type: Array,
  required: false,
  default: [],
})

const options = ref([])

const noOptionMessage = computed(() => {
  return !props.toolId ? t('설비 정보가 필요합니다.') : t('선택 가능한 파라미터가 없습니다.')
})

const fetchAndSetOptions = async (params) => {
  options.value = await ParameterService.getParameters(params)
}

const getApiOptions = (toolId) => {
  const dataTypes =
    props.dataType &&
    props.dataType.reduce((acc, cur) => {
      if (cur === PARAMETER_DATA_TYPE.NUMERIC_TYPES) {
        return acc.concat(Object.keys(PARAMETER_DATA_TYPE.NUMERIC_TYPES))
      } else {
        return acc.concat(cur)
      }
    }, [])

  return {
    toolId,
    isVirtual: props.isVirtual,
    type: props.type,
    dataTypes: dataTypes,
  }
}

watch(
  () => props.toolId,
  async (newToolId) => {
    if (newToolId) {
      await fetchAndSetOptions(getApiOptions(newToolId))
    }
  },
)

onMounted(async () => {
  const { toolId } = props
  if (toolId) {
    await fetchAndSetOptions(getApiOptions(toolId))
  }
})
</script>

<style scoped></style>
