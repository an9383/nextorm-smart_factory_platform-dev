import { useQuasar, Dialog, Loading } from 'quasar'
import router from '/src/router'

import { t } from '/src/plugins/i18n'
import { useAuthStore } from '/src/stores/auth'

export default function useUI() {
  const $q = useQuasar()
  return {
    alert: (title, message, ok = t('확인'), opts) => {
      return $q.dialog({
        title,
        message,
        ok: {
          flat: true,
          label: ok,
        },
        ...opts,
      })
    },
    confirm: (title, message, ok = t('확인'), cancel = t('취소'), opts) => {
      return $q.dialog({
        title,
        message,
        ok: {
          flat: true,
          label: ok,
        },
        cancel: {
          flat: true,
          color: 'negative',
          label: cancel,
        },
        ...opts,
      })
    },
    notify: {
      info: (message, timeout = 1000, opts) => {
        return $q.notify({
          type: 'info',
          message,
          timeout,
          ...opts,
        })
      },
      warning: (message, timeout = 1000, opts) => {
        return $q.notify({
          type: 'warning',
          message,
          timeout,
          color: 'orange-6',
          textColor: 'white',
          ...opts,
        })
      },
      error: (message, timeout = 1000, opts) => {
        return $q.notify({
          type: 'negative',
          message,
          timeout,
          ...opts,
        })
      },
      success: (message, timeout = 1000, opts) => {
        return $q.notify({
          type: 'positive',
          message,
          timeout,
          ...opts,
        })
      },
    },
    loading: {
      show: (opts = null) => {
        $q.loading.show({ spinnerColor: 'primary', spinnerSize: 80, ...opts })
      },
      hide: () => {
        $q.loading.hide()
      },
    },
  }
}

/**
 * 세션아웃 dialog
 */
export const showSessionOutAlert = () => {
  //로그아웃 처리
  const { isAuthenticated, logout } = useAuthStore()
  if (isAuthenticated) {
    Loading.hide() //Loading바 hide 처리
    logout()
    Dialog.create({
      title: t('세션만료'),
      message: `<div class="flex">
                  <i class="q-icon text-orange notranslate material-icons q-mr-md" style="font-size: 32px;">warning</i>
                  <div>
                    ${t('오랫동안 활동이 없어 세션이 종료되었습니다.')}<br/>
                    ${t('안전한 사용을 위해 다시 로그인 해 주세요.')}
                  </div>
                </div>`,
      html: true,
      persistent: true,
      icon: 'warning',
    }).onOk(() => {
      router.push('/login')
    })
  }
}
