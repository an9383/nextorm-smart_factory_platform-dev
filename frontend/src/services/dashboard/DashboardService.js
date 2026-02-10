import http from 'src/common/http.js'

const unWrapData = (res) => res.data

export default {
  /**
   * @param params {{isHide?: boolean}}
   */
  getDashboards(params = {}) {
    return http
      .get('/api/dashboards', {
        params,
      })
      .then(unWrapData)
  },
  getDashboardById(id) {
    return http.get(`/api/dashboards/${id}`).then(unWrapData)
  },
  createDashboard(dashboard) {
    return http.post('/api/dashboards', dashboard).then(unWrapData)
  },
  modifyDashboard(dashboardId, body) {
    return http.put(`/api/dashboards/${dashboardId}`, body).then(unWrapData)
  },
  deleteDashboard(id) {
    return http.delete(`/api/dashboards/${id}`).then(unWrapData)
  },
  updateDashboards(dashboards) {
    return http.put('/api/dashboards', dashboards)
  },
  modifyWidget(widgetId, config) {
    return http.put(`/api/dashboards/widgets/${widgetId}`, {
      config: JSON.stringify(config),
    })
  },
}
