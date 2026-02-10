import http from 'src/common/http.js'

const unWrapData = (res) => res.data

export default {
  getToolStatusWidgetData(toolId) {
    return http.get(`/api/widget-mgt/tool-status-data/${toolId}`).then(unWrapData)
  },
  getCollectStatusWidgetData(toolId) {
    return http.get(`/api/widget-mgt/collect-status-data/${toolId}`).then(unWrapData)
  },
}
