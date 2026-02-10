<template>
  <div class="bg-white">
    <q-markup-table :separator="'cell'" flat bordered>
      <thead>
        <tr>
          <th>위치</th>
          <th>파라미터</th>
          <th>값</th>
          <th>시간</th>
        </tr>
      </thead>
      <tbody class="table-body">
        <tr v-for="(item, index) in items" :key="item.id" style="padding: 0 0">
          <td>{{ getLocationText(index) }}</td>
          <td>{{ $pt(getParameterName(index)) }}</td>
          <td
            class="text-right"
            :style="{
              color: parseColor(index),
            }"
          >
            {{ formatValue(item) }}
          </td>
          <td>{{ formatting(item) }}</td>
        </tr>
      </tbody>
    </q-markup-table>
  </div>
</template>

<script setup>
import { defineProps, ref, watch } from 'vue'
import { useWidgetRefresh, widgetProps } from '/src/common/module/widgetCommon'
import ParameterDataService from 'src/services/parameterData/ParameterDataService'
import ParameterService from 'src/services/modeling/ParameterService'

const props = defineProps(widgetProps)

const items = ref([])
const FRACTION_DIGITS = 1

const parameterWithTool = ref([])

const loadParameterInfo = async () => {
  //파라미터 정보 + 툴 + 위치 정보 조회
  if (!props.config) return
  const parameterIds = props.config.parameters?.filter((parameter) => parameter.id != null).map((set) => set.id) || []
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
  const parameterId = props.config.parameters[index]?.id
  const dto = parameterWithTool.value.find((d) => d.id === parameterId)
  return dto ? formatLocation(dto.toolName, dto.location) : '-'
}

const getParameterName = (index) => {
  const parameterId = props.config.parameters[index]?.id
  const dto = parameterWithTool.value.find((d) => d.id === parameterId)
  return dto?.name || '-'
}

const formatValue = (item) => {
  const value = item?.value
  const dataType = item?.dataType
  // 숫자형 데이터가 아니라면 바로 반환
  if (dataType !== 'INTEGER' && dataType !== 'DOUBLE') {
    return value
  }

  if (!value == null || value === '' || isNaN(Number(value))) {
    return '-'
  }

  let numValue = Number(value)
  if (dataType === 'DOUBLE') {
    numValue = Number(Number(value).toFixed(FRACTION_DIGITS))
  }
  if (dataType === 'INTEGER') {
    numValue = Math.floor(numValue)
  }
  return numValue.toLocaleString() + (item.unit || '')
}

const parseColor = (index) => {
  const config = props.config.parameters[index]
  return config?.valueHighlightColor || ''
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
  const { dataInterval, parameters } = config
  const parameterIds = parameters.filter((set) => set.id != null).map((set) => set.id)

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
    if (!props.config || !props.config.parameters?.length) return
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

.table-body td {
  height: 44px !important;
}
</style>
