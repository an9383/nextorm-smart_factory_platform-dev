import http from 'src/common/http.js'

const unWrapData = (res) => res.data

export default {
  getSimulationList(condition, from, to) {
    const params = {
      condition,
      from,
      to,
    }
    return http.get('/api/apc/simulations', { params }).then(unWrapData)
  },
  getApcSimulation(apcSimulationId) {
    return http.get(`/api/apc/simulations/${apcSimulationId}`).then(unWrapData)
  },
  getSimulationTargetData(from, to) {
    const params = {
      from,
      to,
    }
    return http
      .get('/api/apc/simulations/target-data', {
        params,
      })
      .then(unWrapData)
  },
  runApcSimulation(apcModelVersionId, apcTargetDatas) {
    return http.post(`/api/apc/simulations/${apcModelVersionId}/run`, apcTargetDatas).then(unWrapData)
  },
  getApcSimulationDataRequestResult(apcSimulationDataId) {
    return http.get(`/api/apc/simulations/data/${apcSimulationDataId}/result`).then(unWrapData)
  },
  cancelApcSimulation(apcSimulationId) {
    return http.put(`/api/apc/simulations/${apcSimulationId}/cancel`).then(unWrapData)
  },
}
