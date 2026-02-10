import http from 'src/common/http.js'

const unWrapData = (res) => res.data

export default {
  getMaps() {
    return http.get(`/api/map-mgt/maps`).then(unWrapData)
  },
  getInfraByLocation(lat, lng, radius) {
    return http.get('/api/map-mgt/infra-location', { params: { lat, lng, radius } }).then((value) => value.data)
  },
  // createTool(tool) {
  //   return http.post(`/tool-mgt/tool`, tool);
  // },
  // modifyTool(tool) {
  //   return http.put(`/tool-mgt/tool`, tool);
  // },
  // deleteTool(id) {
  //   return http.delete(`/tool-mgt/tool/${id}`);
  // },
}
