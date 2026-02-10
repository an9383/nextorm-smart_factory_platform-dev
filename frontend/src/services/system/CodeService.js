import http from 'src/common/http.js'

const unWrapData = (res) => res.data

export default {
  getCode(id) {
    return http.get(`/api/codes/${id}`).then(unWrapData)
  },
  getCodesByCategory(category) {
    return http.get(`/api/codes`, { params: { category } }).then(unWrapData)
  },
  getCategory(id) {
    return http.get(`/api/codes/category/${id}`).then(unWrapData)
  },
  getCodeTree() {
    return http.get(`/api/codes/tree`).then(unWrapData)
  },
  createCategory(category) {
    return http.post(`/api/codes/category`, category).then(unWrapData)
  },
  modifyCategory(categoryId, body) {
    return http.put(`/api/codes/category/${categoryId}`, body).then(unWrapData)
  },
  deleteCategory(id) {
    return http.delete(`/api/codes/category/${id}`).then(unWrapData)
  },
  sortCodes(categoryId, codeIds) {
    return http.put('/api/codes/sort', { categoryId, codeIds }).then(unWrapData)
  },
  createCode(code) {
    return http.post(`/api/codes`, code).then(unWrapData)
  },
  modifyCode(codeId, body) {
    return http.put(`/api/codes/${codeId}`, body).then(unWrapData)
  },
  deleteCodes(ids) {
    return http.post(`/api/codes/bulk-delete`, ids).then(unWrapData)
  },
}
