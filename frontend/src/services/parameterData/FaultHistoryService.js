import http from 'src/common/http.js'

const unWrapData = (res) => res.data

export default {
  /**
   *
   * @param parameterId : number
   * @param from : Date
   * @param to : Date
   */
  getFaultHistories(parameterId, from, to) {
    const params = {
      parameterId,
      from: from.toISOString(),
      to: to.toISOString(),
    }
    return http.get('/api/fault-histories', { params }).then(unWrapData)
  },

  /**
   *
   * @param toolId : number
   * @param from : Date
   * @param to : Date
   */
  getToolParametersFaultCount(toolId, from, to) {
    const params = {
      toolId,
      from: from.toISOString(),
      to: to.toISOString(),
    }
    return http.get('/api/fault-histories/count', { params }).then(unWrapData)
  },
}
