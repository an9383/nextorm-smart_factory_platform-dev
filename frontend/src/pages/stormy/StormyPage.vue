<template>
  <q-header>
    <q-toolbar class="justify-between">
      <q-item clickable class="q-py-sm q-pl-none header" @click="showSidebar">
        <q-item-section avatar class="q-pr-none">
          <img :src="ai_animated" width="50" />
        </q-item-section>
        <q-item-section class="no-wrap q-py-xs">
          <q-item-label class="text-white">{{ AI_CHAT_NAME }}</q-item-label>
          <q-item-label caption class="text-white"> {{ $t('AI 데이터분석 전문가가 도와 드리겠습니다.') }}</q-item-label>
        </q-item-section>
      </q-item>
      <ChatTitle
        :chat-id="currentSession.sessionId"
        :title="currentSession.title"
        @title-change="changeTitle"
        @delete="onChatDeleted"
        class="chat-title"
      />
      <div>
        <q-btn flat round @click="updateFavorite" v-if="currentSession.sessionId">
          <q-icon v-if="currentSession?.isFavorite" name="star" size="sm" color="orange"></q-icon>
          <q-icon v-else name="star_outline" size="sm"></q-icon>
        </q-btn>
        <q-btn flat round color="white" icon="logout" @click="onClickLogout">
          <q-tooltip> {{ $t('로그아웃') }} </q-tooltip>
        </q-btn>
      </div>
    </q-toolbar>
  </q-header>
  <q-drawer v-model="isSidebarVisible" bordered class="q-pb-md" :overlay="!isDrawerOverlay">
    <q-list>
      <q-item>
        <q-item-section avatar>
          <q-img :src="logo" />
        </q-item-section>
        <q-item-section>
          <q-item-label header class="text-weight-bold q-pl-none">
            {{ title }}
          </q-item-label>
        </q-item-section>
        <q-item-section avatar style="width: auto">
          <q-btn flat round @click="toggleDrawerOverlay">
            <q-icon v-if="!isDrawerOverlay" name="mdi-pin-outline" size="xs" color="secondary"></q-icon>
            <q-icon v-else name="mdi-pin" size="xs" color="secondary"></q-icon>
            <q-tooltip>
              {{ isDrawerOverlay ? $t('패널 고정 해제') : $t('패널 고정') }}
            </q-tooltip>
          </q-btn>
          <q-btn v-if="!isDrawerOverlay" flat round @click="hideSidebar">
            <q-icon name="mdi-arrow-collapse-left" size="xs" color="secondary"></q-icon>
          </q-btn>
        </q-item-section>
      </q-item>
    </q-list>
    <q-list :bordered="false">
      <q-item clickable v-ripple @click="startNewChat">
        <q-item-section avatar>
          <q-icon name="add" color="primary" size="sm" />
        </q-item-section>
        <q-item-section class="q-pa-none">
          <span class="q-pl-sm text-bold">{{ $t('새 채팅 시작하기') }}</span>
        </q-item-section>
      </q-item>
    </q-list>
    <q-item-label header class="q-mt-lg q-py-sm">
      <q-icon name="bookmarks" size="xs" color="secondary" class="q-mr-sm q-mb-xs" />
      {{ $t('즐겨찾기') }}
    </q-item-label>
    <q-list class="chat-list favorite">
      <q-item
        v-ripple
        v-for="session in favoriteSessions"
        :key="session.id"
        class="chat-list-item"
        clickable
        @click="setCurrentSession(session)"
      >
        <q-item-section avatar>
          <span>💬</span>
        </q-item-section>
        <q-item-section>
          <q-item-label lines="1">{{ session.title }}</q-item-label>
        </q-item-section>
      </q-item>
    </q-list>
    <q-item-label header class="q-mt-lg q-py-sm">
      <q-icon name="schedule" size="sm" color="secondary" class="q-mr-sm q-mb-xs" />
      {{ $t('최근 채팅') }}
      <ChatHistoryPopup
        v-if="isShowChatHistoryPopup"
        v-model="isShowChatHistoryPopup"
        @select="setCurrentSession"
      ></ChatHistoryPopup>
    </q-item-label>
    <q-list :bordered="false" class="chat-list recent col">
      <q-item
        v-ripple
        v-for="session in recentSessions"
        :key="session.id"
        class="chat-list-item"
        clickable
        @click="setCurrentSession(session)"
      >
        <q-item-section avatar>
          <span>💬</span>
        </q-item-section>
        <q-item-section class="label-section">
          <q-item-label lines="1">{{ session.title }}</q-item-label>
          <q-item-label caption>{{ formatRelativeDate(session.updateAt) }}</q-item-label>
        </q-item-section>
      </q-item>
    </q-list>
    <q-btn flat color="grey" class="full-width" @click="() => (isShowChatHistoryPopup = true)">{{
      $t('전체 이력 보기')
    }}</q-btn>
  </q-drawer>
  <q-page class="full-height">
    <FlowiseStreaming
      v-model:session-id="currentSession.sessionId"
      :user-image="loginUser?.image"
      :user-name="loginUser?.name"
      @create:session="createChatSession"
      @update:session="updateChatSession"
      @request:new-session="startNewChat"
    >
    </FlowiseStreaming>
  </q-page>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import ChatTitle from './ChatTiltile.vue'
import { storeToRefs } from 'pinia'
import { useAppStore } from '/src/stores/app'
import { useAuthStore } from '/src/stores/auth'
import FlowiseStreaming from '/src/components/flowise/FlowiseStreaming.vue'
import ChatService from '/src/services/ChatService'
import { date } from 'quasar'
import ChatHistoryPopup from './ChatHistoryPopup.vue'
import ai_animated from '/src/assets/ai_animated.gif'
import { AI_CHAT_NAME } from 'src/common/module/flowise'
import { useI18n } from 'vue-i18n'
import useUI from 'src/common/module/ui'

const { t } = useI18n()
const router = useRouter()
const authStore = useAuthStore()
const appStore = useAppStore()
const ui = useUI()
const { title, logo } = storeToRefs(appStore)
const { loginUser } = storeToRefs(authStore) //Store state를 컴포넌트 state로 변경

const isShowChatHistoryPopup = ref(false)

const formatRelativeDate = (dateString) => {
  const MILLISECONDS_IN_MINUTE = 1000 * 60
  const MILLISECONDS_IN_HOUR = MILLISECONDS_IN_MINUTE * 60
  const MILLISECONDS_IN_DAY = MILLISECONDS_IN_HOUR * 24

  const fromDate = new Date(dateString)
  const now = new Date()

  const diffMilliseconds = now - fromDate
  const diffDays = Math.floor(diffMilliseconds / MILLISECONDS_IN_DAY)
  const diffHours = Math.floor(diffMilliseconds / MILLISECONDS_IN_HOUR)
  const diffMinutes = Math.floor(diffMilliseconds / MILLISECONDS_IN_MINUTE)

  if (diffDays > 6) {
    return formatToDateTime(fromDate) // 7일 이상
  }
  if (diffDays > 0) {
    return t('BEFORE_DAYS_LABEL', { day: diffDays }, '{day}일 전') // 1일 이상
  }
  if (diffHours > 0) {
    return t('BEFORE_HOURS_LABEL', { hours: diffHours }, '{hours}시간 전') // 1시간 이상
  }
  if (diffMinutes > 0) {
    return t('BEFORE_MINUTES_LABEL', { min: diffMinutes }, '{min}분 전') // 1분 이상
  }
  return t('JUST_NOW_LABEL', '방금 전') // 1분 미만
}

const formatToDateTime = (fromDate) => {
  return date.formatDate(fromDate, 'YYYY-MM-DD A hh:mm')
}

const onChatDeleted = () => {
  getChatSessions()
  startNewChat()
}

const onClickLogout = () => {
  ui.confirm(t('로그아웃'), t('로그아웃 하시겠습니까?'), t('로그아웃'), t('취소')).onOk(() => {
    authStore.logout()
    router.push({ path: '/login', replace: true })
  })
}

const startNewChat = () => {
  currentSession.value = {
    sessionId: undefined,
    title: undefined,
    isFavorite: false,
  }
}
const changeTitle = (title) => {
  currentSession.value.title = title
  getChatSessions()
}
const updateFavorite = async () => {
  const isFavorite = !currentSession.value.isFavorite
  await ChatService.updateChatFavorite(currentSession.value.sessionId, isFavorite)
  currentSession.value.isFavorite = isFavorite
  getChatSessions()
}

const updateChatSession = async () => {
  await ChatService.updateChatTitle(currentSession.value.sessionId, currentSession.value.title)
  getChatSessions()
}

const createChatSession = async ({ chatFlowId, chatId, message }) => {
  await ChatService.createChat(chatFlowId, chatId, message)
  currentSession.value.title = message
  getChatSessions()
}

const chatSessions = ref([])
const getChatSessions = async () => {
  const sessions = await ChatService.getChatSessions()
  chatSessions.value = sessions
}

const favoriteSessions = computed(() => {
  return chatSessions.value.filter((it) => it.isFavorite)
})

const recentSessions = computed(() => {
  return chatSessions.value.slice(0, 8)
})

const isDrawerOverlay = ref(true)
const isSidebarVisible = ref(true)
const sidebarPanelRef = ref(null)

const currentSession = ref({
  sessionId: undefined,
  title: undefined,
  isFavorite: false,
})

const setCurrentSession = (message) => {
  currentSession.value = {
    sessionId: message.sessionId,
    title: message.title,
    isFavorite: message.isFavorite,
  }
}

const toggleDrawerOverlay = () => {
  isDrawerOverlay.value = !isDrawerOverlay.value
  if (isDrawerOverlay.value) {
    isSidebarVisible.value = true
  }
}

const hideSidebar = () => {
  isSidebarVisible.value = false
}

const showSidebar = () => {
  isSidebarVisible.value = true
}

const handleClickOutside = (event) => {
  if (
    !isDrawerOverlay.value &&
    sidebarPanelRef.value &&
    !sidebarPanelRef.value.contains(event.target) &&
    !event.target.closest('.sidebar-toggle__button')
  ) {
    isSidebarVisible.value = false
  }
}

// Lifecycle hooks
onMounted(() => {
  document.addEventListener('mousedown', handleClickOutside)
  getChatSessions()
})

onUnmounted(() => {
  document.removeEventListener('mousedown', handleClickOutside)
})
</script>

<style lang="scss" scoped>
.chat-list {
  .chat-list-item {
    min-height: 48px !important;
    height: 48px !important;
    border-bottom: none !important;
    gap: unset !important;
  }
  &.favorite {
    max-height: 165px;
    overflow-y: auto;
    .chat-list-item {
      min-height: 40px !important;
      height: 40px !important;
    }
  }
  &.recent .chat-list-item {
    .label-section {
      justify-content: start !important;
      height: auto !important;
    }
  }
}
.chat-title {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  align-self: center;
}
</style>
