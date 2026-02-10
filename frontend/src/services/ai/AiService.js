import http from 'src/common/http.js'

const unWrapData = (res) => res.data

export default {
  getModels() {
    return http.get('/api/ai/models').then(unWrapData)
  },

  getModeById(id) {
    return http.get(`/api/ai/models/${id}`).then(unWrapData)
  },

  /**
   * @param body {{name, description, algorithm, from, to, yParameterId, xParameterIds}}
   */
  buildModel(body) {
    body.from = body.from.toISOString()
    body.to = body.to.toISOString()
    return http.post('/api/ai/models', body).then(unWrapData)
  },
  delete(id) {
    return http.delete(`/api/ai/models/${id}`).then(unWrapData)
  },

  /**
   *
   * @param params {{yParameterId, xParameterIds, from, to}}
   */
  getCorrelation(params) {
    params.from = params.from.toISOString()
    params.to = params.to.toISOString()
    return http.post('/api/ai/correlation', params).then(unWrapData)
  },

  getInferenceData(modelId, from, to) {
    const params = {
      from: from.toISOString(),
      to: to.toISOString(),
    }
    return http.get(`/api/ai/models/${modelId}/inference-data`, { params }).then(unWrapData)
  },
  getModelByToolId(toolId) {
    return http.get(`/api/ai/models/tool/${toolId}`).then(unWrapData)
  },
}
