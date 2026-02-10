import http from 'src/common/http.js'

const unWrapData = (res) => res.data

export default {
  getRoles() {
    return http.get('/api/roles').then(unWrapData)
  },
  getRole(id) {
    return http.get(`/api/roles/${id}`).then(unWrapData)
  },
  createRole(role) {
    return http.post(`/api/roles`, role).then(unWrapData)
  },
  modifyRole(roleId, body) {
    return http.put(`/api/roles/${roleId}`, body).then(unWrapData)
  },
  deleteRole(id) {
    return http.delete(`/api/roles/${id}`).then(unWrapData)
  },
  deleteRoles(ids) {
    return http.post(`/api/roles/bulk-delete`, ids).then(unWrapData)
  },
}
