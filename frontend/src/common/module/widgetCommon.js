import { ref, onMounted, onUnmounted, watch, getCurrentInstance } from 'vue'

export const widgetProps = {
  config: Object,
  refreshable: Boolean,
}

export const widgetSettingProps = {
  modelValue: Object,
}

export function useWidgetSettingConfig(defaultProps) {
  const { emit, props } = getCurrentInstance()
  const config = ref({ ...defaultProps, ...props.modelValue })

  watch(
    config,
    () => {
      emit('update:model-value', config.value)
    },
    { deep: true },
  )

  return config
}

export function useWidgetRefresh(refresh) {
  const refreshTimer = ref(null)
  const isRefreshing = ref(false)

  const { emit, props } = getCurrentInstance()

  const refreshAction = async () => {
    if (!isRefreshing.value) {
      isRefreshing.value = true
      emit('refreshStart')

      try {
        await refresh(props.config)
        emit('refreshEnd')
      } catch (error) {
        console.error('위젯 리프레시 중 에러 발생:', error)
      } finally {
        isRefreshing.value = false
      }
    }
  }

  const cancelRefreshTimer = () => {
    if (refreshTimer.value) {
      clearInterval(refreshTimer.value)
    }
  }

  /** Refresh 주기적 호출 timer */
  const startRefreshTimer = () => {
    cancelRefreshTimer()
    refreshAction()
    let interval = props.config.interval === 'SECS' ? props.config.intervalSecs : props.config.interval
    refreshTimer.value = setInterval(refreshAction, interval * 1000)
  }

  onMounted(() => {
    if (props.config) {
      //widget config가 설정된 경우만 refresh timer 실행
      startRefreshTimer()
    }
  })
  onUnmounted(() => {
    cancelRefreshTimer()
  })

  watch(
    () => props.config,
    (config) => {
      if (config) {
        //widget의 config가 설정되면 refresh
        if (!props.refreshable) {
          //Edit모드 일때는 refresh를 한번만 실행
          refreshAction()
        } else {
          //Edit모드가 아닐때는 refresh timer 실행
          startRefreshTimer()
        }
      } else {
        cancelRefreshTimer()
      }
    },
    { deep: true },
  )

  watch(
    () => props.refreshable,
    (refreshable) => {
      if (refreshable && props.config) {
        //Edit 모드 해제 시 timer 실행
        startRefreshTimer()
      } else {
        //Edit 모드 전환 시 timer 중지
        cancelRefreshTimer()
      }
    },
  )
  getCurrentInstance().exposed = { refreshAction }

  return { isRefreshing, refreshAction, startRefreshTimer, cancelRefreshTimer }
}
