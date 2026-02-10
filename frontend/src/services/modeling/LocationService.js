import http from 'src/common/http.js'

const unWrapData = (res) => res.data

export default {
  getLocationsAndToolsTree() {
    return http.get('/api/locations/tools/tree').then(unWrapData)
  },

  getLocationUnderTools(locationId) {
    return http.get(`/api/locations/${locationId}/tools`).then(unWrapData)
  },

  getLocationsTree() {
    return http.get('/api/locations/tree').then(unWrapData)
  },

  getLocationsTreeTypeUntil(typeUntils) {
    return http
      .get('/api/locations/tree', {
        params: {
          typeUntils: typeUntils,
        },
      })
      .then(unWrapData)
  },
  getLocation(id) {
    return http.get(`/api/locations/${id}`).then(unWrapData)
  },
  getLocationChildren(id) {
    return http.get(`/api/locations/${id}/children`).then(unWrapData)
  },
  createLocation(body) {
    return http.post('/api/locations', body).then(unWrapData)
  },
  modifyLocation(locationId, body) {
    return http.put(`/api/locations/${locationId}`, body).then(unWrapData)
  },
  deleteLocation(id) {
    return http.delete(`/api/locations/${id}`).then(unWrapData)
  },
  bulkDelete(ids) {
    return http.post(`/api/locations/bulk-delete`, { ids }).then(unWrapData)
  },
  getLineTypeLocationByToolId(toolId) {
    return http.get(`/api/locations/line/${toolId}`).then(unWrapData)
  },
}
