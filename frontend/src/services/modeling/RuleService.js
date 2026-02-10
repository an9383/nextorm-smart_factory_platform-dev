import http from 'src/common/http.js'

const unWrapData = (res) => res.data

export default {
  getRules() {
    return http.get('/api/rules').then(unWrapData)
  },
  createRule(rule) {
    return http.post(`/api/rules`, rule).then(unWrapData)
  },
  modifyRule(rule, ruleId) {
    return http.put(`/api/rules/${ruleId}`, rule).then(unWrapData)
  },
  deleteRule(id) {
    return http.delete(`/api/rules/${id}`).then(unWrapData)
  },
}
