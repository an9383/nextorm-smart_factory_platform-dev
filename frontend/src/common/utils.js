import { date } from 'quasar'
import { DATE_FORMAT, DATE_MINUTES_FORMAT, DATE_TIME_FORMAT, TIME_FORMAT } from 'src/common/constant/format'

const randomRangeInt = (min, max) => {
  // min and max included
  return Math.floor(Math.random() * (max - min + 1) + min)
}

/**
 * Tree 데이터 에서 find
 * @param {*} items 트리 데이터
 * @param {*} key 찾을 key
 * @param {*} value 찾을 값
 * @param {*} options childrenKey : 자식노드의 키
 * @returns 찾은 노드
 */
const findInTree = (items, key, value, options = { childrenKey: 'children' }) => {
  let foundItem = null
  for (const item of items) {
    if (item[key] === value) {
      foundItem = item
    } else if (item[options.childrenKey]) {
      foundItem = findInTree(item[options.childrenKey], key, value, options)
    }
    if (foundItem) {
      break
    }
  }

  return foundItem
}

/**
 * Tree 데이터 에서 데이터의 계층 구조를 찾는다
 * @param {*} items 트리 데이터
 * @param {*} key 찾을 key
 * @param {*} value 찾을 값
 * @param {*} options childrenKey : 자식노드의 키
 * @returns 찾은 트리 구조의 데이터
 */
const findHierarchyInTree = (items, key, value, options = { childrenKey: 'children' }) => {
  for (const item of items) {
    if (item[key] === value) {
      return item // 찾은 아이템을 반환
    } else if (item[options.childrenKey]) {
      const result = findHierarchyInTree(item[options.childrenKey], key, value, options)
      if (result) {
        // 찾은 결과가 있으면 현재 아이템을 부모로 설정하여 계층 구조로 반환
        return { ...item, [options.childrenKey]: [result] }
      }
    }
  }
  return null // 찾지 못한 경우
}

const formatDate = (inputDate) => {
  return date.formatDate(inputDate || Date.now(), DATE_FORMAT)
}

const formatDateTime = (inputDate) => {
  return date.formatDate(inputDate || Date.now(), DATE_TIME_FORMAT)
}

const formatDateMinutes = (inputDate) => {
  return date.formatDate(inputDate || Date.now(), DATE_MINUTES_FORMAT)
}

const formatTime = (inputDate) => {
  return date.formatDate(inputDate || Date.now(), TIME_FORMAT)
}
export const isDateTimeRangeOver = (fromRef, toRef, dateRange, dateType) => {
  const afterFromDate = date.addToDate(fromRef.value, { [dateType]: dateRange })
  const fromRefToSeconds = date.getDateDiff(afterFromDate, fromRef.value, 'seconds')
  const refRangeToSeconds = date.getDateDiff(toRef.value, fromRef.value, 'seconds')
  return refRangeToSeconds > fromRefToSeconds
}

export default {
  randomRangeInt,
  findInTree,
  findHierarchyInTree,
  formatDate,
  formatDateTime,
  formatDateMinutes,
  formatTime,
  isDateTimeRangeOver,
}

export { randomRangeInt, findInTree, findHierarchyInTree, formatDate, formatDateTime, formatDateMinutes, formatTime }
