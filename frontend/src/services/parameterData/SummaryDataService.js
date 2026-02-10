import http from 'src/common/http.js'

const unWrapData = (res) => res.data

export default {
  getTrendData(from, to, parameterIds, chartWidth, dataType) {
    const params = {
      from: from.toISOString(),
      to: to.toISOString(),
      parameterIds,
      chartWidth,
      dataType,
    }
    return http.get('/api/summary/trend', { params }).then(unWrapData)
  },
  getFastTrendData(from, to, parameterIds, chartWidth) {
    const params = {
      from: from.toISOString(),
      to: to.toISOString(),
      parameterIds,
      chartWidth,
    }
    return http.get('/api/summary/fast-trend', { params }).then(unWrapData)
  },
  getMonthlySummaryData(parameterId, fromYear, toYear) {
    return http
      .get(`/api/summary/monthly-data-report`, {
        params: { parameterId, fromYear, toYear },
      })
      .then(unWrapData)
  },
  getSumBaseAtSummaryDataByPeriodType(parameterIds, fromDate, toDate, periodType) {
    return http
      .get('/api/summary/parameter-report', {
        params: {
          parameterIds,
          fromDate,
          toDate,
          periodType,
        },
      })
      .then(unWrapData)
  },
  getParameterReportPdf(parameterIds, fromDate, toDate, periodType, note) {
    return http
      .get('api/summary/parameter-report-pdf', {
        params: {
          parameterIds,
          fromDate,
          toDate,
          periodType,
          note,
        },
      })
      .then(unWrapData)
  },
}
