<template>
  <div class="fullscreen bg-blue text-white text-center q-pa-md flex flex-center">
    <div>
      <div style="font-size: 30vh">403</div>

      <div class="text-h2" style="opacity: 0.4">{{ $t('권한이 없습니다.') }}</div>

      <q-btn class="q-mt-xl" color="white" text-color="blue" unelevated to="/" :label="$t('Go Home')" no-caps />
    </div>
  </div>
</template>

<script>
import { defineComponent } from 'vue'

export default defineComponent({
  name: 'Error403',
})
</script>
