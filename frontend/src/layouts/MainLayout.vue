<template>
  <q-layout view="lHh Lpr lFf">
    <q-header elevated>
      <q-toolbar>
        <q-btn flat dense round @click="toggleLeftDrawer" icon="menu" aria-label="Menu" />
        <q-toolbar-title> {{ $t(title) }}</q-toolbar-title>
        <q-space />
        <div class="q-gutter-sm row items-center no-wrap">
          <q-btn
            round
            dense
            flat
            color="white"
            :icon="$q.fullscreen.isActive ? 'fullscreen_exit' : 'fullscreen'"
            @click="$q.fullscreen.toggle()"
            v-if="$q.screen.gt.sm"
          >
          </q-btn>
          <locale-switch />
          <q-btn v-if="USE_FLOWISE" round dense flat color="white" class="q-mr-sm" href="/ai-chat" target="_blank">
            <q-tooltip>
              <div class="aichat-message">
                {{ `${$t('안녕하세요?')} ${AI_CHAT_NAME} ${$t('입니다.')}` }}
                <br />
                {{ $t('저와의 채팅을 원하시면 눌러주세요.') }}
              </div>
            </q-tooltip>
            <q-img :src="aiChatProfile" fit="contain" height="23px" />
          </q-btn>
          <!--          <q-btn round dense flat color="white" icon="notifications">-->
          <!--            <q-badge color="red" text-color="white" floating> 5</q-badge>-->
          <!--            <q-menu>-->
          <!--              <q-list style="min-width: 100px">-->
          <!--                <q-card class="text-center no-shadow no-border">-->
          <!--                  <q-btn label="View All" style="max-width: 120px !important" flat dense class="text-indigo-8"></q-btn>-->
          <!--                </q-card>-->
          <!--              </q-list>-->
          <!--            </q-menu>-->
          <!--          </q-btn>-->
          <q-btn round flat>
            <q-avatar color="white" size="26px">
              <img v-if="loginUser?.image" :src="loginUser?.image" />
              <span v-else class="avatar-text">{{ loginUser?.name[0].toUpperCase() }}</span>
            </q-avatar>
            <q-menu>
              <div class="row no-wrap q-pa-md">
                <!--                <div class="column">-->
                <!--                  <div class="text-h6 q-mb-md">Settings</div>-->
                <!--                  <q-toggle v-model="mobileData" label="Use Mobile Data" />-->
                <!--                  <q-toggle v-model="bluetooth" label="Bluetooth" />-->
                <!--                </div>-->

                <!--                <q-separator vertical inset class="q-mx-lg" />-->
                <div class="column items-center">
                  <q-avatar
                    color="primary"
                    size="72px"
                    @mouseover="profileImageHover = true"
                    @mouseleave="profileImageHover = false"
                  >
                    <img v-if="loginUser?.image" :src="loginUser.image" />
                    <span v-else class="avatar-text text-white">{{ loginUser?.name[0].toUpperCase() }}</span>
                    <q-icon
                      v-if="profileImageHover"
                      name="edit"
                      class="avatar-icon"
                      @click="isShowUserProfilePopup = true"
                    />
                  </q-avatar>
                  <div class="text-subtitle1 q-mt-md q-mb-xs">{{ loginUser?.name }}</div>

                  <q-btn color="primary" :label="$t('로그아웃')" push size="sm" v-close-popup @click="onClickLogout" />
                </div>
              </div>
            </q-menu>
          </q-btn>
        </div>
      </q-toolbar>
      <UserProfilePopup
        v-if="isShowUserProfilePopup"
        v-model="isShowUserProfilePopup"
        :user-id="loginUser.id"
        @save="userProfileReload"
      ></UserProfilePopup>
    </q-header>
    <q-drawer v-model="leftDrawerOpen" show-if-above bordered>
      <q-list>
        <q-item>
          <q-item-section avatar>
            <!--            <q-img src="/img/widgets/ecotwin-icon/eco-logo-03.svg" />-->
            <q-img :src="logo" />
          </q-item-section>
          <q-item-label header class="text-weight-bold q-pl-none">
            {{ piniaTitle }}
          </q-item-label>
        </q-item>

        <navigation :items="menuLinks"></navigation>
      </q-list>
    </q-drawer>

    <q-page-container class="eco-mainContainer">
      <router-view />
      <!--
      <q-page-sticky position="bottom-right" :offset="[30, 30]">
        <q-btn fab :icon="isChatOpen ? 'close' : 'chat'" color="secondary" padding="10px" @click="toggleChat" />
      </q-page-sticky>
       -->
      <q-page-sticky v-if="USE_FLOWISE" position="bottom-right" :offset="[10, 80]">
        <q-slide-transition appear>
          <FlowiseStreaming v-show="isChatOpen" :user-image="loginUser?.image" :user-name="loginUser?.name" />
        </q-slide-transition>
      </q-page-sticky>
    </q-page-container>
    <v-idle v-if="SESSION_TIMEOUT !== '*'" v-show="false" @idle="onIdle" :duration="SESSION_TIMEOUT" />
  </q-layout>
  <FlowiseEmbed v-if="USE_FLOWISE" />
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from 'src/stores/app'
import { useAuthStore } from 'src/stores/auth'
import { useAPCStore } from 'src/stores/apc'
import { storeToRefs } from 'pinia'
import useUI, { showSessionOutAlert } from 'src/common/module/ui'
import Navigation from 'src/components/common/Navigation.vue'
import LocaleSwitch from 'src/components/locale/LocaleSwitch.vue'
import menus, { findByPath } from 'src/common/constant/menu.js'
import { t } from 'src/plugins/i18n'
import UserProfilePopup from 'src/layouts/popup/UserProfilePopup.vue'
import aiChatProfile from 'src/assets/ai_chat_profile_white.png'
import FlowiseEmbed from 'components/flowise/FlowiseEmbed.vue'
import FlowiseStreaming from 'components/flowise/FlowiseStreaming.vue'

const USE_FLOWISE = (process.env.flowise.use && process.env.flowise.use === 'true') || false
const SESSION_TIMEOUT = process.env.sessionTimeout //세션아웃 시간

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const appStore = useAppStore()
const apcStore = useAPCStore()
const ui = useUI()
const profileImageHover = ref(false)
const isShowUserProfilePopup = ref(false)
const leftDrawerOpen = ref(false)
const isChatOpen = ref(false)

const { loginUser } = storeToRefs(authStore) //Store state를 컴포넌트 state로 변경
const { logo, title: piniaTitle } = storeToRefs(appStore)

const onIdle = () => {
  //세션 아웃
  showSessionOutAlert()
}

const onClickLogout = () => {
  ui.confirm(t('로그아웃'), t('로그아웃 하시겠습니까?'), t('로그아웃'), t('취소')).onOk(() => {
    authStore.logout()
    router.push({ path: '/login', replace: true })
  })
}

const userProfileReload = async () => {
  await authStore.loadLoginUserInfo()
}

const toggleLeftDrawer = () => {
  leftDrawerOpen.value = !leftDrawerOpen.value
}
const title = ref('')
watch(
  route,
  (from, to) => {
    title.value = findByPath((to || from)?.path)?.title || piniaTitle.value
  },
  { immediate: true },
)
onMounted(() => {
  appStore.loadDashboards()
  apcStore.loadConditions()
})

onUnmounted(() => {})

const menuLinks = computed(() => {
  const filterTree = (items) => {
    return items
      .map((item) => {
        if (item.id === 'dashboard' && authStore.isPermitted(item.id, 'view')) {
          //대시보드 권한이 있는 경우 대시보드 목록 추가
          return {
            ...item,
            children: appStore.dashboards?.map((k) => ({
              title: k.name,
              icon: '',
              to: `/dashboard/${k.id}`,
            })),
          }
        } else if (item.children?.length > 0) {
          const filteredChildren = filterTree(item.children)
          if (filteredChildren.length > 0) {
            //자식 메뉴에 권한이 하나라도 있는 경우 권한 있음 처리
            return { ...item, children: filteredChildren }
          } else {
            return null
          }
        } else {
          return authStore.isPermitted(item.id, 'view') ? { ...item } : null
        }
      })
      .filter((item) => item !== null)
  }
  return filterTree(menus)
})
</script>
<style scoped>
.avatar-text {
  color: black;
  font-weight: bold;
}

.avatar-icon {
  position: absolute;
  color: white;
  cursor: pointer;
  opacity: 0.5;
  background-color: black;
  border-radius: 50%;
  width: 100%;
  height: 100%;
}
</style>
