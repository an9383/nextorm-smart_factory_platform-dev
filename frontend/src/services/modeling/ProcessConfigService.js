import http from '/src/common/http.js'

const unWrapData = (res) => res.data

export default {
  getProcessConfigs() {
    return http.get(`/api/process-configs`).then(unWrapData)
  },
  createProcessConfig(processConfig) {
    return http.post(`/api/process-configs`, processConfig).then(unWrapData)
  },
  modifyProcessConfig(processConfigId, body) {
    return http.put(`/api/process-configs/${processConfigId}`, body).then(unWrapData)
  },
  deleteProcessConfig(id) {
    return http.delete(`/api/process-configs/${id}`).then(unWrapData)
  },
}
