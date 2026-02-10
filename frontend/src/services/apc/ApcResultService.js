import http from 'src/common/http.js'

const unWrapData = (res) => res.data

export default {
  getApcRequestStatusList(condition, from, to, isIncludeSimulation) {
    const params = {
      condition,
      from,
      to,
      isIncludeSimulation,
    }
    return http.get('/api/apc/results', { params }).then(unWrapData)
  },
  getApcResultData(requestId) {
    return http.get(`/api/apc/results/${requestId}`).then(unWrapData)
  },
  getResultParameterTrendData(versionId, from, to, isIncludeSimulation) {
    const params = {
      versionId,
      from,
      to,
      isIncludeSimulation,
    }
    return http.get(`/api/apc/results/trend`, { params }).then(unWrapData)
  },
}
