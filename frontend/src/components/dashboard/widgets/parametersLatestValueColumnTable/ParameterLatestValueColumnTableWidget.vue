<template>
  <div class="bg-white">
    <q-markup-table :separator="'cell'" flat bordered>
      <tbody class="table-body">
        <tr style="padding: 0 0">
          <td
            v-for="(item, index) in items"
            :key="index"
            :style="{
              color: parseColor(index),
            }"
          >
            <span class="value">
              {{ formatValue(item) }}
            </span>
            <span class="time">{{ formatting(item) }}</span>
          </td>
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
.bg-white {
  height: 65px !important;
}

.table-body td {
  width: 30px;
  height: 75px !important;
  table-layout: auto;
  padding-left: 4px;
  padding-right: 4px;
  position: relative;
}

.table-body td > .value {
  text-align: center;
  font-size: 30px;
  width: 100%;
  display: inline-block;
}

.time {
  position: absolute;
  bottom: 3px;
  right: 5px;
  font-size: 9px;
  color: gray;
}
</style>
