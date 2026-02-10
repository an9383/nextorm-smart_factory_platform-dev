/**
 * EQMS의 리소스에 접근하기 위한 api 서비스
 * SFP의 포탈을 통해 EQMS의 리소스에 접근한다
 */
import http from 'src/common/http.js'

const unWrapData = (res) => res.data

export default {
  /**
   * 종속 사이트: 건창 스치로폴
   *
   * 금형으로 생상하는 제품 정보 조회
   */
  getMoldProducts() {
    return http.get(`/api/eqms/mold-products`).then(unWrapData)
  },
}
