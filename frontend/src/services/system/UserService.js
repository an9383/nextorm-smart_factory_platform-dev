import http from 'src/common/http.js'

const unWrapData = (res) => res.data

export default {
  /**
   * 로그인한 사용자 정보 조회
   * @returns
   */
  getLoginUser() {
    return http.get(`/api/users/me`).then(unWrapData)
  },
  getUsers() {
    return http.get('/api/users').then(unWrapData)
  },
  getUser(userId) {
    return http.get(`/api/users/${userId}`).then(unWrapData)
  },
  getUserImage(userId) {
    return http.get(`/api/users/${userId}/image`).then(unWrapData)
  },
  createUser(user) {
    return http.post('/api/users', user).then(unWrapData)
  },
  modifyUser(userId, user) {
    return http.put(`/api/users/${userId}`, user).then(unWrapData)
  },
  deleteUser(userId) {
    return http.delete(`/api/users/${userId}`).then(unWrapData)
  },
  deleteUsers(userIds) {
    return http.post(`/api/users/bulk-delete`, userIds).then(unWrapData)
  },
  changePassword(userChangePassword) {
    return http.put('/api/users/change-password', userChangePassword).then(unWrapData)
  },
  changeLocale(locale) {
    return http.put('/api/users/change-locale', { locale }).then(unWrapData)
  },
  createOrUpdateUserSetting(setting) {
    return http.post('/api/users/create-update-setting', setting).then(unWrapData)
  },
  updateUserToken(userId) {
    return http.post(`/api/users/${userId}/token`).then(unWrapData)
  },
}
