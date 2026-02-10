<!-- eslint-disable vue/valid-template-root -->
<template></template>

<script setup>
import { onMounted, onUnmounted, defineProps } from 'vue'
import { useAuthStore } from '/src/stores/auth'
import { getDefaultTheme, loadFlowiseEmbedJS } from 'src/common/module/flowise'

const rootElementName = 'flowise-chatbot'
const authStore = useAuthStore()
const { chatFlowId, apiHost, userImage } = defineProps({
  chatFlowId: {
    type: String,
    required: false,
    default: process.env.flowise.chatFlowId,
  },
  apiHost: {
    type: String,
    required: false,
    default: process.env.flowise.apiHost,
  },
  userImage: {
    type: String, //Base64 or URL
    required: false,
    default: undefined,
  },
})

onMounted(() => {
  initFlowise()
})

onUnmounted(() => {
  deinitFlowise()
})

const initFlowise = async () => {
  const Chatbot = await loadFlowiseEmbedJS()
  Chatbot.default.init({
    chatflowid: chatFlowId,
    apiHost: apiHost,
    theme: getDefaultTheme(userImage || authStore.loginUser.image, {
      chatWindow: {
        width: 500,
        height: 700,
      },
    }),
  })
  removeFlowiseCredit()
  addKeyEventVoiceChat()
}

const deinitFlowise = () => {
  window.localStorage.removeItem(`${chatFlowId}_EXTERNAL`) //대화 내용 제거
  document.querySelector('flowise-chatbot').remove()
}

/**
 * 하단의 Flowise Footer Credit 제거
 */
const removeFlowiseCredit = () => {
  const flowiseElement = document.querySelector(rootElementName)
  const shadowRoot = flowiseElement?.shadowRoot
  if (shadowRoot) {
    //하단 Flowise 로고를 제거하기 위한 Style 추가
    const style = document.createElement('style')
    style.textContent = `
                  .chatbot-container > .flex-col > span {
                      display: none;
                  }
                  .chatbot-container > .flex-col > .w-full.px-5.pt-2.pb-1{
                      margin-bottom: 15px;
                  }
              `
    shadowRoot.appendChild(style)
  }
}

/**
 * PageUp, PageDown 키보드 이벤트로 Recoding, Send 이벤트 추가
 */
const addKeyEventVoiceChat = () => {
  const flowiseElement = document.querySelector(rootElementName)
  const shadowRoot = flowiseElement?.shadowRoot
  if (shadowRoot) {
    //하단 Flowise 로고를 제거하기 위한 Style 추가
    //Pageup 키 입력 시 음성대화 시작, PageDown키 입력 시 음성 전송 처리
    document.addEventListener('keydown', (e) => {
      if (shadowRoot.querySelector('div[part="bot"]').getBoundingClientRect().width > 0) {
        //채팅창이 노출되어있는 경우만 동작
        if (e.key === 'PageUp') {
          //Toggle voice chat
          e.preventDefault()
          shadowRoot.querySelectorAll('.chatbot-input button')[0].click()
        } else if (e.key === 'PageDown') {
          //Send Message
          e.preventDefault()
          shadowRoot.querySelectorAll('.chatbot-input button')[1].click()
        }
      }
    })
  }
}
</script>

<style lang="scss" scoped></style>
