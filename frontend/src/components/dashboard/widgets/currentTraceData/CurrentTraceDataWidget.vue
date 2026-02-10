<template>
  <div class="bg-white">
    <q-card v-for="(item, index) in items" :key="item" flat bordered class="q-mb-sm">
      <q-card-section class="row items-center no-wrap q-pt-sm q-pb-sm">
        <div class="icon-box q-ml-md">
          <q-icon :name="getIcon(index)" size="90px" :color="getColor(item)" />
        </div>
        <div class="column justify-center q-ml-xl">
          <div class="text-caption text-grey-7 row items-center q-mt-xs q-mb-xs">
            <q-icon name="mdi-tools" size="14px" class="q-mt-xm q-mr-xs" />
            {{ $pt(getLocationText(index)) }}
          </div>
          <div class="text-subtitle1 text-weight-bold">{{ $pt(getParameterName(index)) }}</div>
          <div class="text-subtitle1" :class="getColor(item, true)">
            {{ formatValue(item) }}
          </div>
          <q-card-section class="q-pt-none q-pb-none q-pl-none q-pr-none">
            <div class="text-caption text-grey text-right">
              {{ formatting(item) }}
            </div>
          </q-card-section>
        </div>
      </q-card-section>
    </q-card>
  </div>
</template>

<script setup>
import { defineProps, ref, watch } from 'vue'
import { useWidgetRefresh, widgetProps } from '/src/common/module/widgetCommon'
import ParameterDataService from 'src/services/parameterData/ParameterDataService'
import ParameterService from 'src/services/modeling/ParameterService'

const props = defineProps(widgetProps)

const items = ref([])
const FRACTION_DIGITS = 5 // 표시할 소수점 자리수

const parameterWithTool = ref([])

const loadParameterInfo = async () => {
  //파라미터 정보 + 툴 + 위치 정보 조회
  if (!props.config) return
  const parameterIds = props.config.parameterSets?.filter((set) => set.id != null).map((set) => set.id) || []
  if (parameterIds.length === 0) {
    parameterWithTool.value = []
    return
  }
  parameterWithTool.value = await ParameterService.getToolsByParameters(parameterIds)
}

const formatLocation = (toolName, location) => {
  const names = []
  let current = location
  while (current) {
    if (current.name) names.unshift(current.name)
    current = current.parent
  }
  if (toolName) names.push(toolName)
  return names.join(' > ')
}

const getLocationText = (index) => {
  const parameterId = props.config.parameterSets[index]?.id
  const dto = parameterWithTool.value.find((d) => d.id === parameterId)
  return dto ? formatLocation(dto.toolName, dto.location) : '-'
}

const getIcon = (index) => {
  return props.config.parameterSets[index]?.icon || 'mdi-help'
}

const getParameterName = (index) => {
  const parameterId = props.config.parameterSets[index]?.id
  const dto = parameterWithTool.value.find((d) => d.id === parameterId)
  return dto?.name || '-'
}

const getColor = (item, isText = false) => {
  const base = item?.isSpecLimitOver === true ? 'red' : item?.isSpecLimitOver === false ? 'green' : 'grey'
  return isText ? `text-${base}` : base
}

const formatValue = (item) => {
  const value = item?.value
  if (value == null || value === '' || isNaN(Number(value))) return '-'
  return Number.isInteger(value) ? value : Number(value).toFixed(FRACTION_DIGITS)
}

const formatting = (item) => {
  const rawDate = item?.traceAt
  if (!rawDate) return ''

  const date = new Date(rawDate)
  if (isNaN(date.getTime())) return '-'

  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const day = date.getDate()
  const hours = date.getHours()
  const minutes = date.getMinutes()
  const seconds = date.getSeconds()

  return `${year}-${month < 10 ? '0' + month : month}-${day < 10 ? '0' + day : day} ${hours < 10 ? '0' + hours : hours}:${minutes < 10 ? '0' + minutes : minutes}:${seconds < 10 ? '0' + seconds : seconds}`
}

const refresh = async (config) => {
  const { dataInterval, parameterSets } = config
  const parameterIds = parameterSets.filter((set) => set.id != null).map((set) => set.id)

  if (parameterIds.length === 0) {
    items.value = []
    return
  }
  const recentData = await ParameterDataService.getLatestDataWithinPeriod(parameterIds, dataInterval)
  items.value = recentData
}

watch(
  () => props.config,
  () => {
    if (!props.config || !props.config.parameterSets?.length) return
    loadParameterInfo()
  },
  { deep: true, immediate: true },
)

useWidgetRefresh(refresh)
</script>

<style lang="scss" scoped>
.item {
  margin: 5px 0;
  padding: 10px;
  box-sizing: border-box;
  border-bottom: 1px solid #eee;
}

.icon-box {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
