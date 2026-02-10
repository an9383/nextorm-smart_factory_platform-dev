import http from 'src/common/http.js'

const unWrapData = (res) => res.data

export default {
  saveImportData(datas) {
    return http.post(`/api/data-mgt/data`, datas).then(unWrapData)
  },

  migration(formData) {
    return http
      .post('/api/migration', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      })
      .then(unWrapData)
  },
}
