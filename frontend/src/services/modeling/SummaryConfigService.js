import http from '/src/common/http.js'

const unWrapData = (res) => res.data

export default {
  getSummaryConfigs() {
    return http.get(`/api/summary-configs`).then(unWrapData)
  },
  createSummaryConfig(summaryConfig) {
    return http.post(`/api/summary-configs`, summaryConfig).then(unWrapData)
  },
  modifySummaryConfig(summaryConfigId, body) {
    return http.put(`/api/summary-configs/${summaryConfigId}`, body).then(unWrapData)
  },
  deleteSummaryConfig(id) {
    return http.delete(`/api/summary-configs/${id}`).then(unWrapData)
  },
}
