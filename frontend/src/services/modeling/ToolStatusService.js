import http from 'src/common/http.js'

const unWrapData = (res) => res.data

export default {
  /**
   * @param params {{locationId?}}
   */
  getToolStatus(params = {}) {
    return http.get(`/api/tool-status`, { params }).then(unWrapData)
  },
}
