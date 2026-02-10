import http from 'src/common/http.js'

const unWrapData = (res) => res.data

export default {
  getReservoirCapacities(startDt, endDt) {
    return http
      .get('/api/reservoir-capacity', {
        params: { startDt, endDt },
      })
      .then(unWrapData)
  },
  getReservoirCapacity(reservoirCapacityId) {
    return http.get(`/api/reservoir-capacity/${reservoirCapacityId}`).then(unWrapData)
  },
  createReservoirCapacity(reservoirCapacity) {
    return http.post(`/api/reservoir-capacity`, reservoirCapacity).then(unWrapData)
  },
  modifyReservoirCapacity(reservoirCapacityId, reservoirCapacity) {
    return http.put(`/api/reservoir-capacity/${reservoirCapacityId}`, reservoirCapacity).then(unWrapData)
  },
  deleteReservoirCapacity(reservoirCapacityId) {
    return http.delete(`/api/reservoir-capacity/${reservoirCapacityId}`).then(unWrapData)
  },
  deleteReservoirCapacities(ids) {
    const data = {
      ids,
    }
    return http.post(`/api/reservoir-capacity/bulk-delete`, data).then(unWrapData)
  },
  getReservoirCapacityTrend(type, startDt, endDt, locationId) {
    return http
      .get('/api/reservoir-capacity/trend', {
        params: { type, startDt, endDt, locationId },
      })
      .then(unWrapData)
  },
}
