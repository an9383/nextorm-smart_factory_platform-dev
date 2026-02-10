<template>
  <div class="full-width full-height q-py-xs realtime_parameter_wrap">
    <q-list separator>
      <transition-group name="slide" tag="div">
        <q-item v-for="(item, index) in items" :key="item.id" :class="`item ${index === 0 ? 'mainBgcolor' : ''}`">
          <q-item-section>
            <div class="text-subtitle1 text-bold">{{ $pt(item.name) }}</div>
            <div class="text-body1">{{ item.value }}</div>
            <div class="text-caption">{{ formatting(item.time) }}</div>
          </q-item-section>
        </q-item>
      </transition-group>
    </q-list>
  </div>
</template>

<script setup>
import { defineProps, ref } from 'vue'
import { useWidgetRefresh, widgetProps } from '/src/common/module/widgetCommon'
import ParameterDataService from 'src/services/parameterData/ParameterDataService'

// 표시할 소수점 자리수
const FRACTION_DIGITS = 5

defineProps(widgetProps)

const lastDataId = ref(null)
const items = ref([])

const formatting = (date) => {
  let year = date.getFullYear()
  let month = date.getMonth() + 1 // getMonth()는 0부터 시작하기 때문에 1을 더해줍니다.
  let day = date.getDate()

  let hours = date.getHours()
  let minutes = date.getMinutes()
  let seconds = date.getSeconds()

  return `${year}-${month < 10 ? '0' + month : month}-${day < 10 ? '0' + day : day} ${hours < 10 ? '0' + hours : hours}:${minutes < 10 ? '0' + minutes : minutes}:${seconds < 10 ? '0' + seconds : seconds}`
}

const refresh = async (config) => {
  const { parameterId, viewCount } = config
  const recentData = await ParameterDataService.getRecentData(parameterId, lastDataId.value, viewCount)
  items.value = recentData.map((item) => {
    return {
      id: item.id,
      name: item.parameterName,
      value: Number.isInteger(item.value) ? item.value : item.value?.toFixed(FRACTION_DIGITS),
      time: new Date(item.traceAt),
    }
  })
}
useWidgetRefresh(refresh)
</script>

<style scoped>
.list-container {
  width: 300px;
  height: 400px;
  overflow: hidden;
  border: 1px solid #ccc;
  display: flex;
  flex-direction: column;
}

.item {
  margin: 5px 0;
  padding: 10px;
  box-sizing: border-box;
  border-bottom: 1px solid #eee;
}

.slide-enter-active,
.slide-move {
  transition:
    transform 0.5s ease,
    opacity 0.5s ease;
}

.slide-enter-from {
  transform: translateX(-100%);
  opacity: 0;
}

.slide-enter-to {
  transform: translateX(0);
  opacity: 1;
}
</style>
