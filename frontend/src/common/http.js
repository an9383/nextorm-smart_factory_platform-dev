import axios from 'axios'
import qs from 'qs'
import { useAuthStore } from '/src/stores/auth'
import { showSessionOutAlert } from '/src/common/module/ui'
import { t } from '/src/plugins/i18n'
import { Notify } from 'quasar'

const is4xxStatus = (statusCode) => {
  if (statusCode === 401 || statusCode === 403) {
    return false
  }
  return statusCode >= 400 && statusCode < 500
}

const http = axios.create({
  baseURL: process.env.baseURL,
  headers: { 'content-type': 'application/json' },
  paramsSerializer: (params) => qs.stringify(params, { arrayFormat: 'repeat' }),
})

http.downloadFile = (url, isShowNewTab = false, config) =>
  http.get(url, { responseType: 'blob', ...config }).then((response) => {
    const url = window.URL.createObjectURL(new Blob([response.data], { type: response.data.type }))
    const a = document.createElement('a')
    a.href = url
    if (isShowNewTab) {
      a.target = '_blank'
    } else {
      let fileName = 'download_file'
      if (config && config.fileName) {
        fileName = config.fileName
      } else {
        const disposition = response.headers['content-disposition']
        if (disposition && disposition.indexOf('attachment') > -1) {
          var filenameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/
          var matches = filenameRegex.exec(disposition)
          if (matches != null && matches[1]) {
            fileName = decodeURIComponent(matches[1].replace(/['"]/g, ''))
          }
        }
      }
      a.download = fileName
    }
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(url)
  })

http.interceptors.request.use((config) => {
  const { accessToken } = useAuthStore()

  config.headers.setAuthorization(accessToken)
  return config
})

// 상태 변수
let isTokenRefreshing = false
let refreshSubscribers = []

http.interceptors.response.use(
  (response) => {
    return response
  },
  async (error) => {
    const originalRequest = error.config
    if (error.response.status === 401 && error.response?.data?.status !== 'ID_PASSWORD_INVALID') {
      if (error.response?.data?.status === 'ACCESS_TOKEN_EXPIRED') {
        if (!originalRequest._retry) {
          originalRequest._retry = true

          if (isTokenRefreshing) {
            // 토큰 재발급이 진행 중이면 대기
            return new Promise((resolve) => {
              //토큰 재발급 완료 후 원래 요청 재시도를 위해 subscriber 추가
              refreshSubscribers.push((token) => {
                // 재발급 완료되면 토큰을 변경하여 원래 요청 재시도
                originalRequest.headers.Authorization = 'Bearer ' + token
                resolve(http(originalRequest))
              })
            })
          }

          // 토큰 재발급 시작
          isTokenRefreshing = true
          const { reissueAccessToken } = useAuthStore()
          try {
            const newToken = await reissueAccessToken()
            isTokenRefreshing = false
            refreshSubscribers.map((callback) => callback(newToken)) // 대기 중인 요청들에 새로운 토큰 전달
            refreshSubscribers = [] // 대기 목록 초기화
            // 원래 요청 재시도
            originalRequest.headers.Authorization = 'Bearer ' + newToken
            return http(originalRequest)
          } catch (e) {
            isTokenRefreshing = false
            return Promise.reject(error)
          }
        } else {
          showSessionOutAlert()
        }
      } else {
        showSessionOutAlert()
      }
    } else if (is4xxStatus(error.response.status)) {
      const { code, message: defaultMessage, extraData } = error.response.data

      Notify.create({
        type: 'negative',
        message: `${t(code, extraData, defaultMessage)}`,
        html: true,
        timeout: 1500,
      })
    } else if (error.response.status === 500) {
      Notify.create({
        type: 'negative',
        message: `${t('서버와의 통신중 에러가 발생했습니다.')}<br/>${t('잠시 후 다시 시도해 주시기 바랍니다.')}`,
        html: true,
        timeout: 1500,
      })
    }
    return Promise.reject(error)
  },
)

http.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded'
export default http
