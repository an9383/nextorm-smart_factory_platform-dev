import http from 'src/common/http.js'

const unWrapData = (res) => res.data

export default {
  getReservoirLayout(toolIds) {
    return http.get(`/api/reservoir-layouts`, toolIds).then(unWrapData)
  },

  createReservoirLayout(reservoirLayout) {
    return http.post(`/api/reservoir-layouts`, reservoirLayout).then(unWrapData)
  },

  modifyReservoirLayout(reservoirLayoutId, reservoirLayout) {
    return http.put(`/api/reservoir-layouts/${reservoirLayoutId}`, reservoirLayout).then(unWrapData)
  },

  deleteReservoirLayout(reservoirLayoutId) {
    return http.delete(`/api/reservoir-layouts/${reservoirLayoutId}`).then(unWrapData)
  },
}
