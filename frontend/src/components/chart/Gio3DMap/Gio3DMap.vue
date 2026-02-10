<template>
  <div class="map" ref="threejs"></div>
</template>

<script setup>
import { onUpdated, ref } from 'vue'
import Gio3DMap from './gio3DMap.js'

const props = defineProps(['data'])

const threejs = ref()
const gio3DMap = ref(null)

onUpdated(() => {
  if (!gio3DMap.value) {
    gio3DMap.value = new Gio3DMap(threejs.value)
  }
  if (props.data) {
    gio3DMap.value.setData(props.data.areaData, props.data.data)
  }
})
</script>

<style lang="scss" scoped>
.map {
  width: 100%;
  height: 100%;
}
//   }
// }
</style>
