<template>
  <div class="bg-white">
    <q-markup-table :separator="'cell'" flat bordered>
      <thead>
        <tr>
          <th v-for="(column, colIndex) in configColumns" :key="colIndex" :style="{ textAlign: column.align }">
            {{ column.name }}
          </th>
        </tr>
      </thead>
      <tbody class="table-body">
        <tr v-for="(row, rowIndex) in props.config?.tableRows" :key="rowIndex">
          <td
            v-for="(cell, cellIndex) in row"
            :key="cellIndex"
            :style="{
              ...getCellStyle(cell, cellIndex),
              color: cell?.highlightColor || getCellStyle(cell, cellIndex).color || '',
              textAlign: configColumns[cellIndex]?.align || 'left',
            }"
          >
            <span class="value">
              {{ getCellValue(cell, cellIndex) }}
            </span>
            <span class="time" v-if="cell?.type === 'parameter' && cell?.showTimestamp">
              {{ getCellTime(cell) }}
            </span>
          </td>
        </tr>
      </tbody>
    </q-markup-table>
  </div>
</template>

<script setup>
import { defineProps, ref, watch, onMounted } from 'vue'
import { useWidgetRefresh, widgetProps } from '/src/common/module/widgetCommon'
import ParameterDataService from 'src/services/parameterData/ParameterDataService'
import ParameterService from 'src/services/modeling/ParameterService'
import { useValueTransformers } from './useValueTransformers'

const props = defineProps(widgetProps)

const ENV_NAME = import.meta.env?.VITE_ENV_NAME

// 값 변환 함수 사용
const { applyTransformer, initializeProductMap } = useValueTransformers(ENV_NAME)

const items = ref([])
const FRACTION_DIGITS = 1

const parameterWithTool = ref([])
const configColumns = ref([])

const loadParameterInfo = async () => {
  //파라미터 정보 + 툴 + 위치 정보 조회
  if (!props.config) {
    return
  }

  // tableRows에서 파라미터 타입인 셀들의 parameterId만 필터링
  const parameterIds = []
  if (props.config.tableRows && Array.isArray(props.config.tableRows)) {
    props.config.tableRows.forEach((row) => {
      if (Array.isArray(row)) {
        row.forEach((cell) => {
          if (cell?.type === 'parameter' && cell?.parameterId) {
            parameterIds.push(cell.parameterId)
          }
        })
      }
    })
  }

  // 중복 제거
  const uniqueParameterIds = [...new Set(parameterIds)]

  if (uniqueParameterIds.length === 0) {
    parameterWithTool.value = []
    return
  }
  parameterWithTool.value = await ParameterService.getToolsByParameters(uniqueParameterIds)
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

// 셀 값 가져오기 (변환 함수 적용 전 원본 값)
const getRawCellValue = (cell) => {
  if (!cell) return '-'

  if (cell.type === 'custom') {
    return cell.customValue || '-'
  } else if (cell.type === 'parameter') {
    if (!cell.parameterId) return '-'

    const parameterData = items.value[cell.parameterId]
    if (!parameterData) return '-'

    return formatValue(parameterData)
  }

  return '-'
}

// 셀 값 가져오기 (변환 함수 적용 후)
const getCellValue = (cell, cellIndex) => {
  const rawValue = getRawCellValue(cell)

  // 값이 없으면 그대로 반환
  if (rawValue === '-') return rawValue

  // 컬럼에 값 변환 함수가 설정되어 있으면 적용
  const column = configColumns.value[cellIndex]
  if (column && column.valueTransformer) {
    return applyTransformer(rawValue, column.valueTransformer)
  }

  return rawValue
}

// 셀 시간 가져오기
const getCellTime = (cell) => {
  if (!cell || cell.type !== 'parameter' || !cell.parameterId) return ''

  const parameterData = items.value[cell.parameterId]
  if (!parameterData) return ''

  return formatting(parameterData)
}

// 숫자 값 추출 (문자열에서 숫자만 추출)
const extractNumericValue = (value) => {
  if (typeof value === 'number') return value
  if (typeof value === 'string') {
    // 쉼표 제거 및 숫자 추출
    const cleaned = value.replace(/,/g, '')
    const num = parseFloat(cleaned)
    return isNaN(num) ? null : num
  }
  return null
}

// 조건 평가 함수
const evaluateCondition = (operator, cellValue, compareValue) => {
  // eq, neq는 문자열 비교도 지원
  if (operator === 'eq' || operator === 'neq') {
    // 먼저 숫자로 비교 시도
    const numericCellValue = extractNumericValue(cellValue)
    const numericCompareValue = extractNumericValue(compareValue)

    // 둘 다 숫자로 변환 가능하면 숫자로 비교
    if (numericCellValue !== null && numericCompareValue !== null) {
      if (operator === 'eq') {
        return numericCellValue === numericCompareValue
      } else {
        return numericCellValue !== numericCompareValue
      }
    }

    // 숫자 변환 실패 시 문자열로 비교
    const stringCellValue = String(cellValue).trim()
    const stringCompareValue = String(compareValue).trim()

    if (operator === 'eq') {
      return stringCellValue === stringCompareValue
    } else {
      return stringCellValue !== stringCompareValue
    }
  }

  // 나머지 연산자는 숫자만 비교
  const numericCellValue = extractNumericValue(cellValue)
  const numericCompareValue = extractNumericValue(compareValue)

  // 숫자로 변환할 수 없으면 false 반환
  if (numericCellValue === null || numericCompareValue === null) {
    return false
  }

  switch (operator) {
    case 'gt':
      return numericCellValue > numericCompareValue
    case 'gte':
      return numericCellValue >= numericCompareValue
    case 'lt':
      return numericCellValue < numericCompareValue
    case 'lte':
      return numericCellValue <= numericCompareValue
    default:
      return false
  }
}

// 셀 스타일 가져오기 (원본 값 기준으로 평가)
const getCellStyle = (cell, cellIndex) => {
  const column = configColumns.value[cellIndex]
  if (!column || !column.styleRules || column.styleRules.length === 0) {
    return {}
  }

  // 원본 셀 값 가져오기 (변환 전)
  const rawCellValue = getRawCellValue(cell)
  if (rawCellValue === '-') return {}

  // 스타일 규칙 순회하며 조건에 맞는 첫 번째 스타일 반환
  for (const rule of column.styleRules) {
    if (evaluateCondition(rule.operator, rawCellValue, rule.value)) {
      return {
        backgroundColor: rule.backgroundColor || '',
        color: rule.textColor || '',
        fontWeight: rule.fontWeight || 'normal',
      }
    }
  }

  return {}
}

const refresh = async (config) => {
  const { dataInterval, columns, tableRows } = config

  // columns를 ref에 저장하여 템플릿에서 사용
  configColumns.value = columns || []

  // tableRows에서 파라미터 타입인 셀들의 parameterId만 필터링
  const parameterIds = []
  if (tableRows && Array.isArray(tableRows)) {
    tableRows.forEach((row) => {
      if (Array.isArray(row)) {
        row.forEach((cell) => {
          if (cell?.type === 'parameter' && cell?.parameterId) {
            parameterIds.push(cell.parameterId)
          }
        })
      }
    })
  }

  // 중복 제거
  const uniqueParameterIds = [...new Set(parameterIds)]

  if (uniqueParameterIds.length === 0) {
    items.value = {}
    return
  }

  const recentDataArray = await ParameterDataService.getLatestDataWithinPeriod(uniqueParameterIds, dataInterval)

  // 배열을 parameterId를 key로 하는 객체로 변환
  const recentData = {}
  recentDataArray.forEach((data) => {
    if (data?.parameterId) {
      recentData[data.parameterId] = data
    }
  })

  items.value = recentData
}

watch(
  () => props.config,
  () => {
    if (!props.config || !props.config.tableRows?.length) return
    loadParameterInfo()
  },
  { deep: true, immediate: true },
)

// 제품 데이터 초기화
onMounted(async () => {
  const permitSite = new Set(['로컬', '개발서버', '데모서버', '건창스치로폴'])
  if (permitSite.has(ENV_NAME)) {
    await initializeProductMap()
  }
})

useWidgetRefresh(refresh)
</script>

<style lang="scss" scoped>
.bg-white {
  height: 65px !important;
}

.table-body td {
  table-layout: auto;
  padding-left: 4px;
  padding-right: 4px;
  position: relative;
}

.table-body td > .value {
  text-align: center;
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
