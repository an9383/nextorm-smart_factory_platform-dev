import http from 'src/common/http.js'

const unWrapData = (res) => res.data

export default {
  getSummaryDataKinds() {
    return http.get(`/api/meta/summary-data-kinds`).then(unWrapData)
  },
  getCollectorTypes() {
    return http.get(`/api/meta/collector-types`).then(unWrapData)
  },
  getI18nLanguages() {
    return http.get('/api/meta/i18n/languages').then(unWrapData)
  },
  getI18nMessagesByLanguage(language) {
    const params = {
      lang: language,
    }
    return http.get('/api/meta/i18n/messages', { params }).then(unWrapData)
  },
  getSystemInfo() {
    return http.get('/api/meta/system-info').then(unWrapData)
  },
  modifySystemInfo(systemInfoMap) {
    return http.put(`/api/meta/system-info`, systemInfoMap).then(unWrapData)
  },
}
