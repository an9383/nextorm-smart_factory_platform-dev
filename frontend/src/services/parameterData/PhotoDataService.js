import http from 'src/common/http.js'

const unWrapData = (res) => res.data

export default {
  getRobotPhotoData(toolId, fromDate, toDate) {
    return http.get('/api/photo-data/robot-shoot-photo', { params: { toolId, fromDate, toDate } }).then(unWrapData)
  },
}
