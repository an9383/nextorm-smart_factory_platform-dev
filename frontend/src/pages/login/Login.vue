<template>
  <q-layout view="lHh Lpr lFf">
    <q-page-container>
      <q-page class="flex flex-center bg-grey-2">
        <q-card class="q-pa-md shadow-2 login-card login_wrap" bordered>
          <q-card-section class="row justify-center q-pb-none">
            <q-item class="flex-center">
              <q-item-section avatar>
                <q-img v-if="isAIChatLogin" :src="chatTitleImage" width="100px" />
                <q-img v-else :src="logo" />
              </q-item-section>
              <q-item-label class="text-grey-9 text-h5 text-weight-bold">
                {{ isAIChatLogin ? AI_CHAT_NAME : $t(title) }}
              </q-item-label>
            </q-item>
          </q-card-section>
          <q-card-section>
            <q-form ref="form" lazy-validation>
              <q-input dense outlined v-model="loginId" :label="$t('아이디')" :rules="[$rules.required]">
                <template v-slot:prepend>
                  <q-icon name="person" />
                </template>
              </q-input>
              <q-input
                dense
                outlined
                v-model="password"
                type="password"
                :rules="[$rules.required]"
                :label="$t('비밀번호')"
                @keyup.enter="handleLogin"
              >
                <template v-slot:prepend>
                  <q-icon name="key" />
                </template>
              </q-input>
            </q-form>
          </q-card-section>
          <q-card-section>
            <q-btn
              color="primary"
              size="md"
              :label="$t('로그인')"
              no-caps
              class="full-width"
              @click="handleLogin"
            ></q-btn>
          </q-card-section>
        </q-card>
        <q-page-sticky
          v-if="USE_FLOWISE && !isAIChatLogin"
          position="bottom-right"
          :offset="[30, 30]"
          class="aichat-container"
        >
          <div class="aichat-message">
            {{ `${$t('안녕하세요?')} ${AI_CHAT_NAME} ${$t('입니다.')}` }}
            <br />
            {{ $t('저와의 채팅을 원하시면 눌러주세요.') }}
          </div>
          <q-btn @click="moveToAIChatLogin" round flat class="aichat-btn">
            <img :src="chatTitleImage" height="100" />
          </q-btn>
        </q-page-sticky>
      </q-page>
    </q-page-container>
  </q-layout>
</template>
<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import useUI from 'src/common/module/ui'
import { t } from 'src/plugins/i18n'
import { useAuthStore } from 'src/stores/auth'
import { AI_CHAT_NAME } from 'src/common/module/flowise'
import chatTitleImage from 'src/assets/ai_animated.gif'
import { useAppStore } from 'stores/app'
import { storeToRefs } from 'pinia'

const USE_FLOWISE = (process.env.flowise.use && process.env.flowise.use === 'true') || false
const route = useRoute()
const router = useRouter()
const { login } = useAuthStore()

const { title, logo } = storeToRefs(useAppStore())

const ui = useUI()
const loginId = ref()
const password = ref()
const form = ref(null)

const isAIChatLogin = computed(() => {
  return route.fullPath === '/login/ai-chat'
})

const moveToAIChatLogin = () => {
  router.push('/login/ai-chat')
}

const handleLogin = async () => {
  const result = await form.value.validate()
  if (!result) {
    ui.notify.warning(t('아이디/패스워드를 입력해주세요.'))
    return
  }
  ui.loading.show()
  try {
    await login(loginId.value, password.value)
    if (isAIChatLogin.value) {
      router.push(route.query.redirect || '/ai-chat')
    } else {
      router.push(route.query.redirect || '/')
    }
  } catch (e) {
    console.log('e', e.response)
    if (e.response?.status === 401 && e.response?.data?.status === 'ID_PASSWORD_INVALID') {
      ui.notify.error(t('아이디/패스워드를 확인해주세요.'))
    } else {
      ui.notify.error(t('서버와의 통신중 오류가 발생하였습니다. 잠시 후 다시 시도해 주세요.'))
    }
  } finally {
    ui.loading.hide()
  }
}
</script>
<style lang="scss" scoped>
.login-card {
  width: 28rem;
  border-radius: 8px;
  box-shadow:
    0 20px 25px -5px rgb(0 0 0 / 0.1),
    0 8px 10px -6px rgb(0 0 0 / 0.1);
}

.aichat-message {
  display: none;
  position: absolute;
  width: 250px;
  height: auto;
  left: -115px;
  top: -75px;
  background: #484848;
  color: white;
  border-radius: 5px;
  padding: 12px 12.8px;
}

.aichat-message:after {
  border-top: 15px solid #484848;
  border-left: 7px solid transparent;
  border-right: 7px solid transparent;
  border-bottom: 0px solid transparent;
  content: '';
  position: absolute;
  top: 65px;
  left: 175px;
}

.aichat-container:hover .aichat-message {
  display: block; /* 마우스 오버 시 메시지 표시 */
}
</style>
