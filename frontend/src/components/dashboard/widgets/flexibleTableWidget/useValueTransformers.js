import { ref } from 'vue'
import EqmsResourceService from 'src/services/EqmsResourceService'

/**
 * 모든 변환기가 허용되는 환경 변수 이름 목록
 * .env 파일 내의 환경 이름이 일치해야 한다.
 */
const ALL_PERMIT_ENV_NAMES_SET = new Set(['로컬', '개발서버', '데모서버'])

/**
 * 값 변환 함수를 제공하는 Composable
 * FlexibleTableWidget에서 셀 값을 표시하기 전에 변환하는 기능 제공
 */
export function useValueTransformers(envName) {
  // 제품 데이터 캐시
  const productMapCache = ref(null)

  /**
   * 제품 ID를 제품명으로 변환하는 데이터셋을 로드
   * @returns {Promise<Object>} 제품 ID를 키로 하고 제품명을 값으로 하는 객체
   */
  const loadProductIdToNameMap = async () => {
    try {
      const products = await EqmsResourceService.getMoldProducts()
      const map = {}

      if (Array.isArray(products)) {
        products.forEach((product) => {
          if (product.id && product.name) {
            map[product.id] = product.name
          }
        })
      }

      return map
    } catch (error) {
      console.error('Failed to load product map:', error)
      return {}
    }
  }

  /**
   * 제품 데이터 초기화 (위젯 로드 시 호출)
   * @returns {Promise<Object>}
   */
  const initializeProductMap = async () => {
    if (!productMapCache.value) {
      productMapCache.value = await loadProductIdToNameMap()
    }
    return productMapCache.value
  }

  /**
   * 제품 ID를 제품명으로 변환하는 데이터셋을 반환 (동기)
   * @returns {Object} 제품 ID를 키로 하고 제품명을 값으로 하는 객체
   */
  const getProductIdToNameMap = () => {
    return productMapCache.value || {}
  }

  /**
   * 변환 함수 적용
   * @param {*} value - 변환할 값
   * @param {string} transformerName - 변환 함수 이름
   * @returns {*} 변환된 값 또는 원본 값
   */
  const applyTransformer = (value, transformerName) => {
    if (!transformerName) {
      return value
    }

    try {
      switch (transformerName) {
        case 'productIdToName': {
          const map = getProductIdToNameMap()
          // 제품 ID로 제품명 조회
          const productId = typeof value === 'string' ? value : String(value)
          return map[productId] !== undefined ? map[productId] : value
        }
        default:
          return value
      }
    } catch (error) {
      console.error('Value transformation error:', error)
      return value
    }
  }

  const transformers = [
    {
      value: null,
      label: '변환 없음',
      permitEnvs: [],
      description: '원본 값을 그대로 표시합니다.',
    },
    {
      value: 'productIdToName',
      label: '제품 ID → 제품명',
      permitEnvs: ['건창스치로폴'],
      description: '제품 ID를 제품명으로 변환합니다. (EQMS 금형 제품)',
    },
  ]

  /**
   * 사용 가능한 변환 함수 목록
   * UI에서 선택할 수 있는 변환 함수들의 메타데이터
   */
  const availableTransformers = transformers.filter((transformer) => {
    const { permitEnvs } = transformer
    if (permitEnvs.length === 0) {
      return true
    }
    if (ALL_PERMIT_ENV_NAMES_SET.has(envName)) {
      return true
    }
    return permitEnvs.includes(envName)
  })

  return {
    initializeProductMap,
    applyTransformer,
    availableTransformers,
  }
}
