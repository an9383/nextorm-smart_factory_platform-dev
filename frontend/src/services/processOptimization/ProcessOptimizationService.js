import http from 'src/common/http.js'

const unWrapData = (res) => res.data

export default {
  getOptimizations() {
    return http.get('/api/process-optimization').then(unWrapData)
  },
  getOptimization(id) {
    return http.get(`/api/process-optimization/${id}`).then(unWrapData)
  },
  saveProcess(optimization) {
    return http.post('/api/process-optimization', optimization).then(unWrapData)
  },
  getOptimalYValueList(id) {
    return http.get(`/api/process-optimization/optimal-y-values/${id}`).then(unWrapData)
  },
  deleteOptimizations(ids) {
    return http.delete(`/api/process-optimization/${ids}`).then(unWrapData)
  },
  getAiModelTree() {
    return http.get('/api/process-optimization/tree').then(unWrapData)
  },
}
