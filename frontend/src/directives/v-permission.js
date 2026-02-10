/**
 * 사용자 권한 체크를 위한 Custom Directive
 * v-permission:메뉴ID.권한1.권한2..
 * 또는
 * v-permission:메뉴ID="isTrue? 'create' : ['create', 'update']"
 */
import _ from 'lodash'
import { useAuthStore } from 'src/stores/auth'
import { findInTree } from 'src/common/utils'
import menus from 'src/common/constant/menu'

const updateVisibility = (el, binding) => {
  const authStore = useAuthStore()
  const { arg, modifiers, value } = binding

  const menu = findInTree(menus, 'id', arg)
  if (!menu) {
    //메뉴가 없는 경우
    throw new Error('The menu ID set in the v-permission directive does not exist.')
  }

  let isPermitted = false

  if (!_.isEmpty(modifiers)) {
    if (Object.keys(modifiers).find((it) => !['create', 'update', 'delete', 'view'].includes(it))) {
      //권한 지정이 잘못된 경우
      throw new Error(
        'The permissions for the v-permission directive can only be set to "create", "update", "delete", or "view".',
      )
    }
    //v-permission:메뉴ID.권한1.권한2...
    isPermitted = Object.keys(modifiers).find((crud) => !authStore.isPermitted(arg, crud)) === undefined
  } else if (value) {
    //v-permission:메뉴ID="isTrue? 'create' : ['create', 'update']"
    const cruds = _.isArray(value) ? value : [value]
    isPermitted = cruds.find((crud) => !authStore.isPermitted(arg, crud)) === undefined
  } else {
    isPermitted = true
  }
  if (!isPermitted) {
    if (!el.__comment) {
      el.__comment = document.createComment('v-permission placeholder')
      el.replaceWith(el.__comment) // element를 comment node로 교체
    }
  } else {
    if (el.__comment) {
      el.__comment.replaceWith(el) // comment node를 원래 element로 교체
      el.__comment = null
    }
  }
}

export default {
  mounted(el, binding) {
    updateVisibility(el, binding)
  },
  updated(el, binding) {
    updateVisibility(el, binding)
  },
  beforeUnmount(el) {
    // Clean up the element and comment node
    if (el.__comment && el.__comment.parentNode) {
      el.__comment.parentNode.removeChild(el.__comment)
    }
    if (el.parentNode) {
      el.parentNode.removeChild(el)
    }
  },
}
