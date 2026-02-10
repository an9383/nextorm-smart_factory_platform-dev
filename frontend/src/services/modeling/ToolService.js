import http from 'src/common/http.js'

const unWrapData = (res) => res.data

export default {
  /**
   * @param params {{locationId?}}
   */
  getTools(params = {}) {
    return http.get(`/api/tools`, { params }).then(unWrapData)
  },

  getToolsByLocationId(locationId) {
    return http.get(`/api/tools?locationId=${locationId}`).then(unWrapData)
  },

  getToolById(toolId) {
    return http.get(`/api/tools/${toolId}`).then(unWrapData)
  },
  createTool(body) {
    return http.post(`/api/tools`, body).then(unWrapData)
  },
  modifyTool(toolId, body) {
    return http.put(`/api/tools/${toolId}`, body).then(unWrapData)
  },
  deleteToolById(toolId) {
    return http.delete(`/api/tools/${toolId}`).then(unWrapData)
  },
  bulkDelete(ids) {
    const data = {
      ids,
    }
    return http.post(`/api/tools/bulk-delete`, data).then(unWrapData)
  },
  createToolKafka(toolId, bootstrapServer) {
    const body = {
      bootstrapServer,
    }
    return http.post(`/api/tools/${toolId}/kafka`, body).then(unWrapData)
  },
  getToolKafka(toolId) {
    return http.get(`/api/tools/${toolId}/kafka`).then(unWrapData)
  },
  modifyToolKafka(toolId, bootstrapServer) {
    const body = {
      bootstrapServer,
    }
    return http.put(`/api/tools/${toolId}/kafka`, body).then(unWrapData)
  },
  checkExistsToolKafka(toolId) {
    return http.get(`/api/tools/${toolId}/kafka/is-exists`).then(unWrapData)
  },
  getToolCollectStatus() {
    return http.get('/api/tools/tool-collect-status').then(unWrapData)
  },
}
