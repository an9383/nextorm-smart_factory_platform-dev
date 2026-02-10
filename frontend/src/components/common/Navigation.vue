<template>
  <q-list class="eco-sideBar-list">
    <template v-for="item of props.items" :key="item.to">
      <nav-item :item="item" :level="0" :class="'eco-sideBar-item-wrap'"></nav-item>
    </template>
  </q-list>
</template>

<script setup>
import NavItem from '/src/components/common/NavItem.vue'

const props = defineProps({ items: Array })
</script>

<style lang="scss" scoped></style>
