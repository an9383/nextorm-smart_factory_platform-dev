import http from 'src/common/http.js'

const unWrapData = (res) => res.data

export default {
  /**
   * @param params {{id?, toolId?, name?, isVirtual?, type?, dataTypes?}}
   */
  getParameters(params) {
    return http.get(`/api/parameters`, { params }).then(unWrapData)
  },
  getParameterById(parameterId) {
    return http.get(`/api/parameters/${parameterId}`).then(unWrapData)
  },
  createParameter(body) {
    return http.post(`/api/parameters`, body).then(unWrapData)
  },
  copyParametersByToolIds(body) {
    return http.post('/api/parameters/copy-parameters', body).then(unWrapData)
  },
  modifyParameter(parameterId, body) {
    return http.put(`/api/parameters/${parameterId}`, body).then(unWrapData)
  },
  deleteParameter(parameterId) {
    return http.delete(`/api/parameters/${parameterId}`).then(unWrapData)
  },
  getDcpAssignedParametersByToolId(toolId) {
    const params = {
      toolId,
    }
    return http.get('/api/parameters/dcp-assigned', { params }).then(unWrapData)
  },

  createVirtualParameter(body) {
    return http.post('/api/parameters/virtual', body).then(unWrapData)
  },
  getVirtualParameterById(parameterId) {
    return http.get(`/api/parameters/virtual/${parameterId}`).then(unWrapData)
  },
  modifyVirtualParameter(parameterId, body) {
    return http.put(`/api/parameters/virtual/${parameterId}`, body).then(unWrapData)
  },
  /**
   * @param notIncludedParameterIds Array<Number>
   *
   * @returns Array<[{parameterId: Number, extraData: object}]>
   */
  getExtraDataByIds(notIncludedParameterIds) {
    const params = {
      ids: notIncludedParameterIds,
    }
    return http.get('/api/parameters/extra-datas', { params }).then(unWrapData)
  },
  getParameterToolTree() {
    return http.get('/api/parameters/tool-tree').then(unWrapData)
  },
  getToolsByParameters(parameterIds) {
    const params = {
      ids: parameterIds,
    }
    return http.get('/api/parameters/tools-by-parameters', { params }).then(unWrapData)
  },

  /**
   * 메타데이터 값 업데이트
   */
  modifyMetaDataParameter(parameterId, body) {
    return http.put(`/api/parameters/${parameterId}/meta-value`, body).then(unWrapData)
  },

  /**
   * 메타데이터 값 일괄 업데이트
   *
   * body:
   * [
   *     {
   *         id: Number,
   *         value: string
   *     },
   *     ...
   * ]
   *
   */
  modifyMetaDataParameters(body) {
    return http.put(`/api/parameters/meta-value/bulk`, body).then(unWrapData)
  },
}
