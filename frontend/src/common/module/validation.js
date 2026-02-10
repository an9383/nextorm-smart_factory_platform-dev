import { watch } from 'vue'

export function useDateTimeRange(fromRef, toRef) {
  let invalidCallback = undefined

  const validateRange = () => {
    const fromDate = new Date(fromRef.value)
    const toDate = new Date(toRef.value)
    if (fromDate > toDate) {
      const temp = fromRef.value
      fromRef.value = toRef.value
      toRef.value = temp
      if (typeof invalidCallback === 'function') {
        invalidCallback()
      }
    }
  }

  watch(fromRef, validateRange)
  watch(toRef, validateRange)

  return {
    onInvalid: (callback) => {
      invalidCallback = callback
    },
  }
}
