import { defineStore } from 'pinia'
import jwt from 'src/common/storage/jwt.js'
import { findInTree } from '/src/common/utils'
import { findByPath, findHierarchyById } from 'src/common/constant/menu'
import AuthService from 'src/services/AuthService'
import UserService from 'src/services/system/UserService'
import _ from 'lodash'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    accessToken: jwt.getToken(),
    loginUser: null,
    menuPermissions: null,
    userSettings: null,
  }),
  getters: {
    isAuthenticated: (state) => !!state.accessToken,
  },
  actions: {
    /**
     * Login 처리
     * @param {*} loginId
     * @param {*} password
     */
    async login(loginId, password) {
      const response = await AuthService.login(loginId, password)
      if (response.status === 200) {
        const accessToken = response.headers.getAuthorization()
        jwt.saveToken(accessToken)
        this.accessToken = accessToken
      } else {
        throw response
      }
    },
    /**
     * Login 처리 (토큰pw 기반)
     * @param {*} loginId
     * @param {*} token
     */
    async loginByToken(loginId, token) {
      const response = await AuthService.loginByToken(loginId, token)
      if (response.status === 200) {
        const accessToken = response.headers.getAuthorization()
        jwt.saveToken(accessToken)
        this.accessToken = accessToken
      } else {
        throw response
      }
    },
    /**
     * 로그인 사용자 로그아웃 처리
     */
    async logout() {
      await AuthService.logout()
      this.accessToken = null
      this.loginUser = null
      jwt.destroyToken()
    },
    /**
     * Access Token 재발급
     */
    async reissueAccessToken() {
      const response = await AuthService.reissueAccessToken()
      if (response.status === 200) {
        const accessToken = response.headers.getAuthorization()
        jwt.saveToken(accessToken)
        this.accessToken = accessToken
      } else {
        throw response
      }
    },
    /**
     * 현재 로그인 된 사용자 정보 조회
     */
    async loadLoginUserInfo() {
      const user = await UserService.getLoginUser()
      this.loginUser = user

      const mergedPermissions = user.roles.reduce((prev, curr) => {
        const permission = JSON.parse(curr.permission || '{"menu" : {}}').menu
        return _.mergeWith(prev, permission, (objValue, srcValue) => {
          if (_.isBoolean(objValue)) {
            return objValue || srcValue
          }
        })
      }, {})
      this.menuPermissions = mergedPermissions
      this.userSettings = JSON.parse(user.userSetting?.settingJson || '{}')
    },

    async updateUserSetting(setting) {
      const combineSetting = JSON.stringify(Object.assign({}, this.userSettings, setting))
      const result = await UserService.createOrUpdateUserSetting({ settingJson: combineSetting })
      this.userSettings = JSON.parse(result.settingJson)
    },

    /**
     * Menu ID로 권한이 있는지 체크
     * @param {*} menuId
     * @param {*} action 'create' | 'update' | 'delete' | 'view'
     * @returns
     */
    isPermitted(menuId, action) {
      if (!this.loginUser) {
        return false
      }

      if (this.menuPermissions[action] === true) {
        //root 권한이 있는 경우
        return true
      }

      const menuHierarchy = findHierarchyById(menuId)
      if (!menuHierarchy) {
        //없는 메뉴인 경우
        return false
      }

      //부모 권한부터 요청하는 권한까지 권한이 있는지 확인
      const checkPermit = (menu, action) => {
        const { id } = menu
        const permission = findInTree(this.menuPermissions.children || [], 'id', id)
        if (permission && permission[action] === true) {
          return true
        } else if (menu.children?.length > 0) {
          return checkPermit(menu.children[0], action)
        } else {
          return false
        }
      }

      return checkPermit(menuHierarchy, action)
    },
    /**
     * Route Path로 권한이 있는지 체크
     * @param {*} path
     * @param {*} action 'create' | 'update' | 'delete' | 'view'
     * @returns
     */
    isPermittedByPath(path, action) {
      if (!this.loginUser) {
        return false
      }

      if (path === '/' || path.startsWith('/fs') || path === '/ai-chat' || path === '/dashboard/slides') {
        //메인 페이지 인 경우
        return true
      }

      if (/^\/dashboard\/\d+$/.test(path)) {
        //Dashboard인 경우 dashboard의 권한 체크
        path = '/dashboard'
      } else if (/^\/apc\/simulation(\/\d+)?$/.test(path)) {
        //APC 시뮬레이션의 경우 APC 모델의 권한 체크
        path = '/apc/model'
      }

      const menu = findByPath(path)
      if (!menu) {
        return false
      }

      return this.isPermitted(menu.id, action)
    },
  },
})
