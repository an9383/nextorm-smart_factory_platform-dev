import http from 'src/common/http.js'

const unWrapData = (res) => res.data

export default {
  getParameterDataTrend(parameterId, from, to) {
    return http
      .get('/api/parameter-data/trend', {
        params: { parameterId, from, to },
      })
      .then(unWrapData)
  },
  getMultiParameterDataTrend(parameterIds, from, to) {
    return http
      .get('/api/parameter-data/trend/multi', {
        params: { parameterIds, from, to },
      })
      .then(unWrapData)
  },
  getParameterDataNormalDistribution(parameterIds, from, to) {
    return http
      .get('/api/parameter-data/distribution', {
        params: { parameterIds, from, to },
      })
      .then(unWrapData)
  },
  getImageParameterData(toolId, dateTime) {
    return http
      .get('/api/parameter-data/image', {
        params: { toolId, dateTime },
      })
      .then(unWrapData)
  },
  getParameterDataSpecOutCount(parameterIds, from, to) {
    return http
      .get('/api/parameter-data/spec-out/count', {
        params: { parameterIds, from, to },
      })
      .then(unWrapData)
  },
  getParameterStatistics(parameterId, operator, timeCriteria, from = null, to = null) {
    let params = {
      operator,
      timeCriteria,
    }

    if (from && to) {
      params = {
        ...params,
        from,
        to,
      }
    }
    return http.get(`/api/parameter-data/statistics/${parameterId}`, { params }).then(unWrapData)
  },
  getRecentData(parameterId, lastDataId, limit) {
    const params = {
      parameterId,
      lastDataId,
      limit,
    }
    return http.get(`/api/parameter-data/recent`, { params }).then(unWrapData)
  },
  getEcoParameters(parameterId, fromDate, toDate) {
    return http
      .get(`/api/parameter-data/eco-parameter-pivot`, { params: { parameterId, fromDate, toDate } })
      .then(unWrapData)
  },

  getUnderWaterTerrainData(toolId, fromDate, toDate) {
    return http.get(`/api/parameter-data/underwater-terrain`, { params: { toolId, fromDate, toDate } }).then(unWrapData)
  },

  getRecipeNames(recipeParameterId, from, to) {
    const params = { parameterId: recipeParameterId, from, to }
    return http.get(`/api/parameter-data/distinct`, { params }).then(unWrapData)
  },

  getRecipeTrendData(toolId, parameterId, recipeParameterId, recipeName, from, to) {
    const params = { toolId: toolId, parameterId, recipeParameterId, recipeName, from, to }
    return http.get(`/api/parameter-data/recipe-trend`, { params }).then(unWrapData)
  },

  getRecipeDtwTrendData(toolId, parameterId, recipeParameterId, recipeName, from, to) {
    const params = { toolId: toolId, parameterId, recipeParameterId, recipeName, from, to }
    return http.get(`/api/parameter-data/recipe-dtw-trend`, { params }).then(unWrapData)
  },

  getParameterDatasByPageable(parameterId, params) {
    return http.get(`/api/parameter-data/${parameterId}/pagination`, { params }).then(unWrapData)
  },

  getLatestDataWithinPeriod(parameterIds, dataInterval) {
    const params = { parameterIds: parameterIds, period: dataInterval }
    return http.get(`/api/parameter-data/latest-within-period`, { params }).then(unWrapData)
  },
}
