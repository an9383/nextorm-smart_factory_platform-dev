<template>
  <flowise-fullchatbot></flowise-fullchatbot>
</template>

<script setup>
import { onMounted, onUnmounted, defineProps } from 'vue'
import { useAuthStore } from '/src/stores/auth'
import { getDefaultTheme, loadFlowiseEmbedJS } from 'src/common/module/flowise'
import { t } from '/src/plugins/i18n'

const rootElementName = 'flowise-fullchatbot'
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
  Chatbot.default.initFull({
    chatflowid: chatFlowId,
    apiHost: apiHost,
    theme: getDefaultTheme(userImage || authStore.loginUser.image),
    observersConfig: {
      observeLoading: () => {
        addMoveToEcoTwinButton()
      },
    },
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
      if (e.key === 'PageUp') {
        //Toggle voice chat
        e.preventDefault()
        shadowRoot.querySelectorAll('.chatbot-input button')[0].click()
      } else if (e.key === 'PageDown') {
        //Send Message
        e.preventDefault()
        shadowRoot.querySelectorAll('.chatbot-input button')[1].click()
      }
    })
  }
}

const addMoveToEcoTwinButton = () => {
  const flowiseElement = document.querySelector(rootElementName)
  const shadowRoot = flowiseElement?.shadowRoot
  if (shadowRoot) {
    const header = shadowRoot.querySelector('.chatbot-container').children[0]
    if (!header.querySelector('.btn-ecotwin')) {
      const refreshButton = header.querySelector('button')
      const newButton = refreshButton.cloneNode(true)
      newButton.innerHTML = `<img src="/img/widgets/ecotwin-icon/eco-logo-03.svg" />`
      newButton.disabled = false
      newButton.classList.add('btn-ecotwin')
      newButton.title = t('에코트윈으로 이동')
      newButton.style.paddingRight = 0
      newButton.addEventListener('click', () => {
        window.open('/', '_blank')
      })
      header.insertBefore(newButton, refreshButton)
    }
  }
}
</script>

<style lang="scss" scoped></style>
