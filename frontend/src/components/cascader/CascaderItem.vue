<template>
  <div>
    <div v-if="title" class="text-subtitle2 q-ma-md text-grey-7">{{ title }}</div>
    <div>
      <q-list>
        <q-item
          v-for="item in items"
          :key="item.id"
          :model-value="item"
          clickable
          v-ripple
          :active="selectedItem ? item.id === selectedItem.id : false"
          active-class="selected"
          @click="handleSelectItem(item)"
        >
          <q-item-section>
            <q-item-label>{{ item[itemText] }}</q-item-label>
          </q-item-section>
          <q-item-section side>
            <q-icon v-if="item[childrenKey] && item[childrenKey].length > 0" name="chevron_right" />
          </q-item-section>
        </q-item>
      </q-list>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, defineProps, defineEmits } from 'vue'

const props = defineProps({
  dense: Boolean,
  multiple: Boolean,
  items: Array,
  title: String,
  childrenKey: { type: String, default: 'children' },
  itemText: { type: String, default: 'text' },
  itemValue: { type: String, default: 'value' },
  depth: { type: Number, default: 0 },
  modelValue: [Object, String, Number, Array], // 현재 선택된 항목의 값
})

const emits = defineEmits(['select'])

const selectedItem = ref(null)

watch(
  () => props.modelValue,
  (newVal) => {
    selectedItem.value = newVal
  },
  { immediate: true },
)

function handleSelectItem(item) {
  emits('select', { item: item, depth: props.depth })
}
</script>

<style scoped>
.selected {
  background-color: rgba(214, 255, 255, 0.8);
}
</style>
