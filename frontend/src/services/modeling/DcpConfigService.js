import http from 'src/common/http.js'

const unWrapData = (res) => res.data

export default {
  /**
   *
   * @param toolId
   * @returns {Promise<DcpConfig[]>}
   */
  getDcpConfigsByTool(toolId) {
    const params = {
      toolId,
    }
    return http.get(`/api/dcp-configs`, { params }).then(unWrapData)
  },
  getDcpConfigById(id) {
    return http.get(`/api/dcp-configs/${id}`).then(unWrapData)
  },
  createDcpConfig(body) {
    return http.post(`/api/dcp-configs`, body).then(unWrapData)
  },
  modifyDcpConfig(dcpConfig, id) {
    return http.put(`/api/dcp-configs/${id}`, dcpConfig).then(unWrapData)
  },
  deleteDcpConfig(id) {
    return http.delete(`/api/dcp-configs/${id}`).then(unWrapData)
  },
}
