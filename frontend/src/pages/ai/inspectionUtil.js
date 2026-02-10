const BRIX_PARAMETERS = [
  { id: 1001, name: 'Brix', isVirtual: false, type: 'TRACE', dataType: 'DOUBLE' },
  { id: 1002, name: 'Brix', isVirtual: false, type: 'TRACE', dataType: 'DOUBLE' },
]

const findParameterByToolName = (toolName) => {
  const number = parseInt(toolName.replace('살균기', ''), 10)
  if (number === 1) {
    return BRIX_PARAMETERS[0]
  } else {
    return BRIX_PARAMETERS[1]
  }
}

const findParameterById = (parameterId) => {
  return BRIX_PARAMETERS.find((parameter) => {
    return parameter.id === parameterId
  })
}

function createRandomGenerator(seed) {
  const m = 2 ** 35 - 31
  const a = 185852
  let s = seed % m

  return {
    // 0~1 사이의 랜덤값
    random() {
      return (s = (s * a) % m) / m
    },

    // min~max 사이의 정수
    range(min, max) {
      return Math.floor(this.random() * (max - min + 1)) + min
    },

    // min~max 사이의 소수점 포함 실수
    floatRange(min, max, decimals = 1) {
      const value = this.random() * (max - min) + min
      return Number(value.toFixed(decimals))
    },

    // 배열에서 랜덤 요소 선택
    pick(array) {
      return array[Math.floor(this.random() * array.length)]
    },
  }
}

const generateRandomValue = (seed, min = 5.5, max = 6) => {
  return createRandomGenerator(seed).floatRange(min, max)
}

const generateRandomValueTest = (min = 5.5, max = 6, decimals = 1) => {
  const value = Math.random() * (max - min) + min
  return Number(value.toFixed(decimals))
}

const includeParameterId = (parameterIds) => {
  return BRIX_PARAMETERS.some((parameter) => parameterIds.includes(parameter.id))
}

export { findParameterByToolName, findParameterById, generateRandomValue, includeParameterId, generateRandomValueTest }
