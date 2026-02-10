<!--
   Fullscreen(전체화면) On/Off 버튼이 있는 컨테이너 공통 컴포넌트
-->
<template>
  <div ref="containerDiv" class="text-right bg-white col relative-position">
    <q-btn
      color="secondary"
      @click="toggleFullscreen"
      :icon="isFullscreen ? 'fullscreen_exit' : 'fullscreen'"
      flat
      rounded
      class="btn-absolute"
    >
      <q-tooltip>
        {{ isFullscreen ? $t('닫기') : $t('전체화면') }}
      </q-tooltip>
    </q-btn>
    <slot />
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useQuasar } from 'quasar'
const $q = useQuasar()
const isFullscreen = ref(false)
const containerDiv = ref()

watch(
  () => $q.fullscreen.isActive,
  (val) => {
    if (val === false && isFullscreen.value === true) {
      isFullscreen.value = false
    }
  },
)

// 버튼 클릭 시 전체화면 토글
const toggleFullscreen = async () => {
  await $q.fullscreen.toggle(containerDiv.value)
  isFullscreen.value = !isFullscreen.value
}
</script>
<style scoped lang="scss">
.btn-absolute {
  position: absolute;
  z-index: 100;
  right: 5px;
  top: 5px;
}
</style>
