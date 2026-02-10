<template>
  <q-card class="column col full-height" bordered flat>
    <q-card-section class="col flex items-center justify-center text-black">
      <div class="text-h4 text-weight-bold text-center">{{ formattedTime }}</div>
    </q-card-section>
  </q-card>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useWidgetRefresh, widgetProps } from 'src/common/module/widgetCommon'

const props = defineProps(widgetProps)

const currentTime = ref(new Date())
let intervalId = null

const refresh = async () => {
  // 시간 형식이 변경되면 화면 갱신
  currentTime.value = new Date()
}

const formattedTime = computed(() => {
  const timeFormat = props.config?.timeFormat || 'full'
  const now = currentTime.value

  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const hour = String(now.getHours()).padStart(2, '0')
  const minute = String(now.getMinutes()).padStart(2, '0')
  const second = String(now.getSeconds()).padStart(2, '0')

  if (timeFormat === 'full') {
    return `${year}.${month}.${day} ${hour}:${minute}:${second}`
  } else if (timeFormat === 'monthToSecond') {
    return `${month}.${day} ${hour}:${minute}:${second}`
  } else if (timeFormat === 'dayToSecond') {
    return `${day}일 ${hour}:${minute}:${second}`
  }

  return `${year}.${month}.${day} ${hour}:${minute}:${second}`
})

onMounted(() => {
  // 1초마다 시간 갱신
  intervalId = setInterval(() => {
    currentTime.value = new Date()
  }, 1000)
})

onUnmounted(() => {
  if (intervalId) {
    clearInterval(intervalId)
  }
})

useWidgetRefresh(refresh)
</script>
