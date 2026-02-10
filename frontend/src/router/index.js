import { createRouter, createMemoryHistory, createWebHistory, createWebHashHistory } from 'vue-router'
import { publicRoutes, protectedRoute, isPublicRoute } from './routes'
import { useAuthStore } from '/src/stores/auth'
import { jwtDecode } from 'jwt-decode'

const env = import.meta.env
const routes = [...publicRoutes, ...protectedRoute]

const createHistory = process.env.SERVER
  ? createMemoryHistory
  : process.env.VUE_ROUTER_MODE === 'history'
    ? createWebHistory
    : createWebHashHistory

const router = createRouter({
  scrollBehavior: () => ({ left: 0, top: 0 }),
  routes,
  history: createHistory(process.env.VUE_ROUTER_BASE),
})

const createRedirectPath = (to, removeQueryKeys = []) => {
  const params = new URLSearchParams()
  Object.keys(to.query).forEach((key) => {
    if (removeQueryKeys.includes(key)) {
      return
    }

    const v = to.query[key]
    if (v === undefined || v === null) {
      return
    }
    if (Array.isArray(v)) {
      v.forEach((item) => {
        params.append(key, String(item))
      })
    } else {
      params.append(key, String(v))
    }
  })
  return `${to.path}?${params.toString()}`
}

router.beforeEach(async (to, from, next) => {
  let { isAuthenticated } = useAuthStore()
  const { loginUser, isPermittedByPath, loadLoginUserInfo, loginByToken, accessToken, logout } = useAuthStore()

  const { 'VITE_function.looseSso.use': useLooseSSO } = env
  const useLooSSOFlag = useLooseSSO === 'true' || useLooseSSO === true

  // 주영테크 ERP와 로그인 연계를 위한 토큰기반 자동로그인 처리 start
  // 쿼리 파라미터에 id, token을 받아서, 이를 활용해 처리하도록 함
  // 기존에 로그인 된 토큰 기록이 있더라도, id가 다르면 재로그인 처리
  const { id, token } = to.query
  const hasAutoLoginParams = !!(useLooSSOFlag && id && token) // 세 값이 모두 true여야 true
  let isAutoLoginTarget = hasAutoLoginParams

  if (isAuthenticated && hasAutoLoginParams) {
    const payload = jwtDecode(accessToken)
    const { loginId } = payload
    if (loginId !== id) {
      isAutoLoginTarget = true
    }
  }

  if (isAutoLoginTarget) {
    try {
      await loginByToken(id, token)
    } catch (error) {
      // 로그인 실패시 로그아웃 처리 (로그인 페이지로 보내기 위함)
      await logout()
    }
  }
  isAuthenticated = useAuthStore().isAuthenticated
  // 주영테크 ERP와 로그인 연계를 위한 토큰기반 자동로그인 처리 end

  if (!isPublicRoute(to.path)) {
    if (!isAuthenticated) {
      // 리다이렉트 할 때, 자동로그인 파라미터(id, token)는 제거하고 리다이렉트 경로 생성
      const removeQueryKeys = hasAutoLoginParams ? ['id', 'token'] : []
      const redirect = createRedirectPath(to, removeQueryKeys)

      next({ path: '/login', query: { redirect } })
    } else if (loginUser == null) {
      await loadLoginUserInfo()
    }
    if (
      !isPermittedByPath(to.path, 'view') ||
      (to.path === '/dashboard' && !isPermittedByPath(to.path, 'create')) //dashboard의 경우는 생성권한이 있는지 체크
    ) {
      next({ path: '/403' })
    }
  }
  next()
})

// afterEach에서 replace 방식(히스토리 교체, 추가 항목 없음)
router.afterEach((to) => {
  const { id, token, ...rest } = to.query
  if (id && token) {
    const cleanedQuery = { ...rest }
    // replace를 사용하면 브라우저 히스토리에 새 항목을 추가하지 않습니다
    router.replace({ path: to.path, query: cleanedQuery })
  }
})

export default router
