import http from 'src/common/http.js'

const unWrapData = (res) => res.data

export default {
  getHeatMapHealthData(parameterIds, fromDate, toDate) {
    return http.get(`/api/health-data/heatmap`, { params: { parameterIds, fromDate, toDate } }).then(unWrapData)
  },

  getMonthlyForecastHealthData(parameterIds, fromDate, toDate) {
    return http
      .get('/api/health-data/monthly-forecast', {
        params: { parameterIds, fromDate, toDate },
      })
      .then(unWrapData)
  },

  getMonthlyHeatMapHealthData(parameterIds, fromDate, toDate) {
    return http.get('/api/health-data/monthly-heatmap', { params: { parameterIds, fromDate, toDate } }).then(unWrapData)
  },
}
