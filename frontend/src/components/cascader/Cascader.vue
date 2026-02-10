<template>
  <q-input
    :label="label"
    :placeholder="placeholder"
    :model-value="inputValue"
    readonly
    :dense="dense"
    :clearable="clearable"
    v-bind="$attrs"
    :disable="disabled"
    :rules="(props.rules || []).concat(rules.depthRule)"
    class="cascader-input"
  >
    <template #append>
      <q-icon name="close" @click.stop.prevent="handleClickClearable" class="cursor-pointer" v-if="clearable" />
      <q-icon name="mdi-chevron-down" />
    </template>
    <q-menu v-model="showMenu" offset-y tile>
      <q-card flat>
        <q-card-section class="bg-primary">
          <q-input
            v-model="search"
            standout="bg-blue-grey-5"
            :placeholder="$t('검색')"
            color="white"
            dense
            clearable
            clear-icon="close"
            class="col"
            input-class="text-white"
          />
        </q-card-section>
        <q-card-section class="list-container row q-pa-none">
          <template v-for="(children, dep) in filteredChildrens" :key="dep + 1">
            <cascader-item
              :items="children"
              :depth="dep"
              :dense="dense"
              :title="dep > titles.length - 1 ? undefined : titles[dep]"
              v-model="selectedItems[dep]"
              :multiple="multiple"
              :item-text="itemText"
              :item-value="itemValue"
              :item-tooltip="itemTooltip"
              @select="handleItemSelect"
            />
          </template>
        </q-card-section>
      </q-card>
    </q-menu>
  </q-input>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import CascaderItem from './CascaderItem.vue'
import { QCard, QCardSection, QInput, QMenu } from 'quasar'
import { findPath } from 'n-ary-tree'
import { expandTreeData } from '/src/common/treeUtil'
import _ from 'lodash'
import { useI18n } from 'vue-i18n'

const props = defineProps({
  items: Array,
  label: String,
  placeholder: String,
  name: String,
  dense: Boolean,
  outlined: Boolean,
  multiple: Boolean,
  clearable: Boolean,
  returnObject: Boolean,
  validDepth: Number,
  validDepthMsg: String,
  disabled: Boolean,
  rules: Array,
  titles: {
    type: Array,
    default: () => [],
  },
  childrenKey: {
    type: String,
    default: 'children',
  },
  separator: {
    type: String,
    default: ' / ',
  },
  itemText: {
    type: String,
    default: 'text',
  },
  itemValue: {
    type: String,
    default: 'value',
  },
  itemTooltip: {
    type: String,
    default: undefined,
  },
  modelValue: {
    type: [Array, Number, String],
    default: () => [],
  },
})
const { t } = useI18n()
const emit = defineEmits(['update:modelValue', 'change'])

const showMenu = ref(false)
const search = ref('')
const selectedItems = ref([])
const childrens = ref([props.items])
const rules = {
  depthRule() {
    return (
      !props.validDepth ||
      selectedItems.value.length === 0 ||
      selectedItems.value.length === props.validDepth ||
      props.validDepthMsg ||
      props.validDepth + t('댑스까지 선택해야 합니다.')
    )
  },
}

const filteredChildrens = computed(() => {
  if (search.value) {
    return childrens.value.map((group) => {
      return group.filter(
        (item) =>
          expandTreeData([item]).some((child) => child[props.itemText].includes(search.value)) ||
          findPath(item, search.value, {
            value: props.itemValue,
            children: props.childrenKey,
          }).length > 0 ||
          selectedItems.value.includes(item),
      )
    })
  }
  return childrens.value
})

watch(
  () => props.modelValue,
  (newVal) => {
    if (newVal) {
      initValue(newVal)
    }
  },
  { deep: true, immediate: true },
)

watch(
  () => props.items,
  () => {
    childrens.value = [props.items]
    initValue(props.modelValue)
  },
)

function initValue(val) {
  let path
  if (!_.isObject(val)) {
    path = findPathWithId(val)
  } else {
    path = findPathWithId(val.id)
  }
  if (path) {
    selectedItems.value = path
    path.forEach((item, index) => {
      const children = item[props.childrenKey]
      if (children) {
        childrens.value[index + 1] = children
      }
    })
  } else {
    selectedItems.value = []
  }
}

const inputValue = ref(
  selectedItems.value
    .filter((item) => item !== null)
    .map((item) => item[props.itemText])
    .join(props.separator),
)
watch(selectedItems, (newVal) => {
  inputValue.value = newVal
    .filter((item) => item !== null)
    .map((item) => item[props.itemText])
    .join(props.separator)
})

function findPathWithId(id) {
  return id
    ? props.items
        .map((item) => {
          return findPath(item, id, { value: props.itemValue, children: props.childrenKey })
        })
        .filter((item) => item.length > 0)
        .pop()
    : []
}

function handleItemSelect({ item, depth }) {
  const childrensKey = item[props.childrenKey]
  selectedItems.value[depth] = item
  childrens.value[depth + 1] = childrensKey

  // Reset all grandchildren's children
  for (let i = 0; i < childrens.value.length; i++) {
    if (i > depth + 1) {
      childrens.value[i] = []
    }
  }
  childrens.value = childrens.value.filter((item) => item && item.length > 0)
  for (let i = 0; i < selectedItems.value.length; i++) {
    if (i > depth) {
      selectedItems.value[i] = null
    }
  }
  // Remove nulls from selectedItems
  selectedItems.value = selectedItems.value.filter((item) => item !== null)
  emit('update:modelValue', computeValue())
  emit('change', computeValue())

  if (props.validDepth && props.validDepth === depth + 1) {
    showMenu.value = false
  }
}

function computeValue() {
  if (props.multiple) {
    return selectedItems.value.map((item) => item[props.itemValue])
  }
  return selectedItems.value.at(-1)?.[props.itemValue]
}

function handleClickClearable() {
  inputValue.value = ''
  selectedItems.value = []
  emit('update:modelValue', null)
  emit('change', null)
}
</script>
<style scoped lang="scss">
.list-container {
  max-height: 380px;
  overflow-y: auto;

  > :not(:last-child) {
    border-right: 0.5px dashed lightgray;
  }
}

.cascader-input {
  cursor: pointer;

  :deep(input) {
    cursor: pointer !important;
  }
}
</style>
