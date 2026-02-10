import { useAPCStore } from '/src/stores/apc'
import { storeToRefs } from 'pinia'

const getApcConditions = async () => {
  const apcStore = useAPCStore()
  const { apcConditions } = storeToRefs(apcStore)
  if (!apcConditions?.value?.length) {
    await apcStore.loadConditions()
  }
  return apcConditions.value
}
export const DELIMITER = '|;'

/**
 * Delimiter로 조합된 단일 String을 Object로 변환한다.
 * @param {*} conditionsWithDelimiter (ex: toolA|;rec*...)
 * @returns
 */
export const apcConditionDelimiterToObject = async (conditionsWithDelimiter) => {
  const apcConditions = await getApcConditions()
  const conditions = conditionsWithDelimiter.split(DELIMITER)
  return apcConditions.reduce((prev, curr, index) => {
    prev[curr.key] = conditions[index]
    return prev
  }, {})
}

/**
 * condition + input data 객체를 condition과 inputData로 분리하여 리턴한다.
 * @param {*} data condition + input data (ex: {tool: 'toolA', recipe: 'recipe1', temperature: 30})
 * @returns {
 *    condition : {}
 *    inputData: {}
 * }
 */
export const splitConditionAndInputData = async (data) => {
  const result = { condition: {}, inputData: {} }
  if (!data) {
    return result
  }

  const apcConditions = await getApcConditions()

  const conditions = apcConditions.map((it) => it.key)
  return Object.keys(data).reduce((acc, key) => {
    if (conditions.includes(key)) {
      acc.condition[key] = data[key]
    } else {
      acc.inputData[key] = data[key]
    }
    return acc
  }, result)
}
