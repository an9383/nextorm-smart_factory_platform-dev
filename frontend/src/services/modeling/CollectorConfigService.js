import http from '/src/common/http.js'

const unWrapData = (res) => res.data

export default {
  getCollectorConfigs() {
    return http.get(`/api/collector-configs`).then(unWrapData)
  },
  createCollectorConfig(collectorConfig) {
    return http.post(`/api/collector-configs`, collectorConfig).then(unWrapData)
  },
  modifyCollectorConfig(collectorConfigId, body) {
    return http.put(`/api/collector-configs/${collectorConfigId}`, body).then(unWrapData)
  },
  deleteCollectorConfig(id) {
    return http.delete(`/api/collector-configs/${id}`).then(unWrapData)
  },
}
