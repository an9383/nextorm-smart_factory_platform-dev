<template>
  <q-select
    v-bind="filteredAttrs.restAttrs"
    v-model="modelValue"
    :options="filteredOptions"
    use-input
    ref="selectRef"
    @input="filteringOptions"
  >
    <template v-for="(_, slot) in $slots" #[slot]="slotProps">
      <slot :name="slot" v-bind="slotProps || {}" />
    </template>

    <template v-slot:no-option>
      <q-item>
        <q-item-section class="text-grey">
          {{ t('선택 가능한 항목이 없습니다.') }}
        </q-item-section>
      </q-item>
    </template>
  </q-select>
</template>

<script setup>
import { computed, defineOptions, ref, useAttrs, watch } from 'vue'
import { t } from 'src/plugins/i18n'
import _ from 'lodash'

defineOptions({ inheritAttrs: false })

const attrs = useAttrs()

const selectRef = ref()
const modelValue = ref(attrs.modelValue)
const filteredOptions = ref(Array.isArray(attrs.options) ? [...attrs.options] : [])

const filteringOptions = _.debounce(async (e) => {
  const value = e.target.value
  if (!value) {
    filteredOptions.value = Array.isArray(attrs.options) ? [...attrs.options] : []
    return
  }

  const upperValue = value.toLowerCase()
  const labelProp = attrs.optionLabel || attrs['option-label']
  filteredOptions.value = attrs.options.filter((item) => {
    // 그룹 헤더는 항상 표시
    if (item.isHeader) {
      return true
    }
    const label = (typeof labelProp === 'function' ? labelProp(item) : item?.[labelProp]) ?? item
    return String(label).toLowerCase().includes(upperValue)
  })

  selectRef.value.updateInputValue(value)
}, 200)

const isMultiple = computed(() => {
  return attrs.multiple === '' || attrs.multiple === true
})

const filteredAttrs = computed(() => {
  const rest = { ...attrs }
  delete rest.modelValue
  delete rest.options
  delete rest.filter

  if (!isMultiple.value) {
    if (!('hide-selected' in rest)) {
      rest['hide-selected'] = true
    }

    if (!('fill-input' in rest)) {
      rest['fill-input'] = true
    }
  }

  return { restAttrs: rest }
})

watch(
  () => attrs.modelValue,
  (val) => {
    modelValue.value = val
  },
)

watch(
  () => attrs.options,
  (val) => {
    filteredOptions.value = Array.isArray(val) ? [...val] : []
  },
)

defineExpose({
  clearFilter: () => {
    filteredOptions.value = Array.isArray(attrs.options) ? [...attrs.options] : []
  },
  $selectRef: selectRef,
})
</script>
