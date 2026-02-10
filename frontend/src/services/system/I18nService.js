import http from 'src/common/http.js'

const unWrapData = (res) => res.data

export default {
  getMessages() {
    return http.get('/api/i18n').then(unWrapData)
  },

  updateMessages(newMessages, updateMessages, removeKeys) {
    return http.post('/api/i18n', { newMessages, updateMessages, removeKeys }).then(unWrapData)
  },

  translation(translationLanguage, translationData) {
    const params = {
      text: translationData,
      targetLang: translationLanguage,
    }
    return http.get('/api/i18n/translation', { params }).then(unWrapData)
  },
}
