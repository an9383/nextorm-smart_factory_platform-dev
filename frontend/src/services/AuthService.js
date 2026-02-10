import http from 'src/common/http.js'

export default {
  /**
   * 로그인 처리
   * @param {*} loginId 유저ID
   * @param {*} password 패스워드
   * @returns
   */
  login(loginId, password) {
    const body = {
      loginId,
      password,
    }

    return http.post(`/api/auth/login`, body, {
      withCredentials: true,
    })
  },
  /**
   * 로그인 처리
   * @param {*} loginId 유저ID
   * @param {*} token 유저 토큰
   * @returns
   */
  loginByToken(loginId, token) {
    const body = {
      loginId,
      token,
    }

    return http.post(`/api/auth/login-by-token`, body, {
      withCredentials: true,
    })
  },

  /**
   * 로그아웃 처리
   * @returns
   */
  logout() {
    return http.post('/api/auth/logout', undefined, { withCredentials: true })
  },
  /**
   * Access Token 재발급(with 쿠키의 Refresh Token)
   * @returns
   */
  reissueAccessToken() {
    return http.post('/api/auth/reissue-access-token', undefined, { withCredentials: true })
  },
}
