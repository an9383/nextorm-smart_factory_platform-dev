<template>
  <Suspense>
    <template #default>
      <meta-data-loader>
        <router-view />
      </meta-data-loader>
    </template>
    <template #fallback>
      <meta-loading />
    </template>
  </Suspense>
</template>

<script setup>
import MetaLoading from 'components/meta/MetaLoading.vue'
import MetaDataLoader from 'components/meta/MetaDataLoader.vue'
</script>

<style>
.blocklyDropDownDiv {
  z-index: 6000;
}
</style>
