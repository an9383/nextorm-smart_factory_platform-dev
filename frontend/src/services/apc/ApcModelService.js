import http from 'src/common/http.js'

const unWrapData = (res) => res.data

export default {
  getApcModels() {
    return http.get(`/api/apc/models`).then(unWrapData)
  },
  getModelVersionsByModelId(apcModelId) {
    return http.get(`/api/apc/models/${apcModelId}/versions`).then(unWrapData)
  },
  getApcModelVersion(apcModelVersionId) {
    return http.get(`/api/apc/models/versions/${apcModelVersionId}`).then(unWrapData)
  },

  createApcModel(body) {
    return http.post(`/api/apc/models`, body).then(unWrapData)
  },
  createApcModelVersion(apcModelId, body) {
    return http.post(`/api/apc/models/${apcModelId}/versions`, body).then(unWrapData)
  },

  modifyApcModel(apcModelId, body) {
    return http.put(`/api/apc/models/${apcModelId}`, body).then(unWrapData)
  },
  deleteApcModel(apcModelId) {
    return http.delete(`/api/apc/models/${apcModelId}`).then(unWrapData)
  },
}
