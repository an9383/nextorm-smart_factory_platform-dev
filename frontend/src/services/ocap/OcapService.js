import http from 'src/common/http.js'

const unWrapData = (res) => res.data

export default {
  getOcapById(ocapAlarmId) {
    return http.get(`/api/ocap/${ocapAlarmId}`).then(unWrapData)
  },

  getAllOcap() {
    return http.get('/api/ocap').then(unWrapData)
  },

  /**
   * @param body {{name, isAlarmControlSpecOver, isAlarmSpecOver, alarmIntervalCodeId, toolId, parameterId, recipients: Array<{userId: number, notificationTypes: string[]}>}}
   */
  createOcap(body) {
    return http.post('/api/ocap', body).then(unWrapData)
  },

  /**
   * @param body {{name, isAlarmControlSpecOver, isAlarmSpecOver, alarmIntervalCodeId, toolId, parameterId, recipients: Array<{userId: number, notificationTypes: string[]}>}}
   */
  modifyOcap(ocapAlarmId, body) {
    return http.put(`/api/ocap/${ocapAlarmId}`, body).then(unWrapData)
  },

  deleteOcapById(ocapAlarmId) {
    return http.delete(`/api/ocap/${ocapAlarmId}`).then(unWrapData)
  },

  getOcapAlarmHistories(fromDate, toDate) {
    return http
      .get(`/api/ocap/histories`, {
        params: {
          fromDate,
          toDate,
        },
      })
      .then(unWrapData)
  },
}
