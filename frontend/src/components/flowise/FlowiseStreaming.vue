<template>
  <div class="row col q-mx-md q-mb-lg full-height justify-center">
    <q-card bordered flat class="chat-container q-mr-md">
      <q-card-section class="q-py-md q-px-xs chat-contents">
        <div class="chat-message-container q-px-lg q-py-sm" ref="chatMessageContainer">
          <template v-for="(message, index) in chatMessages" :key="index">
            <q-chat-message
              v-if="message.isSent"
              :name="$t('You')"
              :text="[marked.parse(message?.message || '')]"
              text-color="white"
              sent
              text-html
              bg-color="primary"
            >
              <template v-slot:avatar>
                <UserAvatar
                  :user-image="userImage"
                  :user-name="userName"
                  size="48px"
                  class="q-ml-sm"
                  color="secondary"
                />
              </template>
            </q-chat-message>
            <template v-else>
              <q-chat-message v-if="message.isSending" :name="AI_CHAT_NAME" :avatar="aiChatProfile" bg-color="green-4">
                <q-spinner-dots size="2rem" color="white" class="q-mx-lg" />
              </q-chat-message>
              <q-chat-message
                v-else
                :name="AI_CHAT_NAME"
                :avatar="aiChatProfile"
                :text="[summaryMessage(message?.message || '')]"
                text-color="black"
                text-html
                bg-color="grey-2"
                @click="setSelectedMessageIndex(index)"
                class="cursor-pointer"
              >
              </q-chat-message>
              <div class="result-spin">
                <q-spinner-bars color="purple" size="2em" v-if="index === chatMessages.length - 1 && isReceiving" />
              </div>
            </template>
          </template>
        </div>
        <q-separator />
        <PromptList :disable="isCommunicating || isMicOn" @prompt-selected="sendPrompt" />
        <div class="q-mx-sm">
          <q-input
            ref="inputRef"
            v-model="inputText"
            filled
            dense
            :disable="isCommunicating || isMicOn"
            :placeholder="$t('메시지를 입력해주세요.')"
            @keyup.enter="sendInputText"
          >
            <template v-if="isMicOn" #default>
              <div class="recording">
                <q-spinner-audio color="primary" size="2em" />
                <span class="text-caption q-mx-sm">{{ t('음성 인식 중입니다.') }}...</span>
                <span v-if="speechText" class="text-body2 text-black">({{ speechText }})</span>
              </div>
            </template>
            <template v-slot:after>
              <q-btn
                round
                dense
                flat
                :icon="isMicOn ? 'mic_off' : 'mic'"
                :disable="isCommunicating"
                @click="toggleMic"
                color="red"
              />
              <q-btn
                round
                dense
                flat
                icon="send"
                color="secondary"
                :disable="isCommunicating || (!inputText && !isMicOn)"
                @click="sendInputText"
              />
            </template>
          </q-input>
        </div>
      </q-card-section>
    </q-card>

    <FlowiseStreamingDetail :message="detailMessage" v-if="selectedMessageIndex >= 0" />
  </div>
</template>

<script setup>
import { computed, nextTick, ref, watch } from 'vue'
import aiChatProfile from 'src/assets/ai_chat_profile.png'
import { AI_CHAT_NAME, useFlowise } from 'src/common/module/flowise'
import { marked } from 'marked'
import FlowiseStreamingDetail from './FlowiseStreamingDetail.vue'
import PromptList from 'src/pages/stormy/prompt/PromptList.vue'
import useSpeechToText from 'src/common/module/speechToText'
import { useI18n } from 'vue-i18n'
import UserAvatar from '../common/UserAvatar.vue'
import useUI from 'src/common/module/ui'

const { t } = useI18n()

const DEFAULT_CHAT_MESSAGES = [
  {
    message: `${t('DEFAULT_CHAT_MESSAGE', { name: AI_CHAT_NAME }, '안녕하세요. {name} 입니다.')} ${t('무엇을 도와드릴까요?')}`,
  },
]

const { userImage, userName } = defineProps({
  userImage: { type: String, default: '' },
  userName: { type: String, default: '' },
})
const currentSessionId = defineModel('sessionId', {
  type: String,
  required: false,
})
const emit = defineEmits(['create:session', 'update:session', 'request:new-session'])
const inputRef = ref()
const chatMessageContainer = ref(null)
const inputText = ref('')
const speechText = ref('')
const isMicOn = ref(false)
const chatMessages = ref(DEFAULT_CHAT_MESSAGES)
const selectedMessageIndex = ref(-1)
let isNewSession = false //신규 세션 여부
const ui = useUI()

watch(
  chatMessages,
  () => {
    nextTick(() => {
      chatMessageContainer.value.scrollTop = chatMessageContainer.value.scrollHeight
    })
  },
  { deep: true },
)

const isReceiving = ref(false)
// const isReceived = ref(true)
const isSending = computed(() => {
  return chatMessages.value.at(-1)?.isSending || false
})
const isCommunicating = computed(() => isSending.value || isReceiving.value)

watch(currentSessionId, async (value) => {
  if (value) {
    if (isNewSession === false) {
      const messages = await getMessages(value)
      chatMessages.value = messages.map((message) => ({
        message: message.content,
        isSent: message.role == 'userMessage',
      }))
      selectedMessageIndex.value = messages.length - 1
    }
  } else {
    selectedMessageIndex.value = 0
    createNewSession()
  }
})

const createNewSession = () => {
  chatMessages.value = DEFAULT_CHAT_MESSAGES
  createSession()
}

const chatCallback = {
  onCreateSession: (chatId, message) => {
    isNewSession = true
    currentSessionId.value = chatId
    emit('create:session', { chatFlowId: process.env.flowise.chatFlowId, chatId, message })
  },
  onStart: () => {
    isReceiving.value = true
    selectedMessageIndex.value = [chatMessages.value.length - 1]
    setTimeout(() => {
      chatMessageContainer.value.scrollTop = chatMessageContainer.value.scrollHeight
    })
  },
  onToken: (token) => {
    if (token) {
      chatMessages.value[chatMessages.value.length - 1] = {
        isSent: false,
        message: chatMessages.value[chatMessages.value.length - 1].message + token,
      }
    }
  },
  onEnd: () => {
    if (isNewSession === false) {
      //신규 세션 생성이되는 메시지 전송인 경우는 update 처리하지 않음
      emit('update:session')
    }
    isNewSession = false
    isReceiving.value = false
    focusInput()
  },
}

const { createSession, sendMessage, getMessages } = useFlowise(
  process.env.flowise.apiHost,
  process.env.flowise.chatFlowId,
  chatCallback,
)

const detailMessage = computed(() => {
  return selectedMessageIndex.value > 0 && chatMessages.value[selectedMessageIndex.value]?.message.trim()
    ? marked.parse(chatMessages.value[selectedMessageIndex.value].message)
    : null
})

const setSelectedMessageIndex = (messageIndex) => {
  if (messageIndex === 0) {
    return
  }
  selectedMessageIndex.value = messageIndex
}

const summaryMessage = (message) => {
  return marked.parse(message.split('\n')[0])
}

useSpeechToText(isMicOn, {
  grammarList: [
    '오늘',
    '에이징',
    '공정',
    '생산',
    '완료',
    '품목',
    '보여줘',
    '최근',
    '한달',
    '발생',
    '알람',
    'TOP10',
    '보여줘',
  ],
  onResult: (text) => {
    speechText.value = text // 인식 중 텍스트는 speechText에 저장
  },
})
const toggleMic = () => {
  if (!isMicOn.value) {
    // 마이크 켜질 때 초기화
    speechText.value = ''
    inputText.value = ''
  } else if (isMicOn.value) {
    // 마이크 끌 때 최종 인식된 텍스트를 inputText로 복사
    inputText.value = speechText.value
    speechText.value = ''
  }
  isMicOn.value = !isMicOn.value
}

const sendPrompt = (command) => {
  inputText.value = command
  sendInputText()
}

const sendInputText = async (ignoreMessageLimit = false) => {
  const message = isMicOn.value ? speechText.value : inputText.value
  if (!message) {
    return
  }
  if (isCommunicating.value) {
    return
  }
  if (!ignoreMessageLimit && !checkMessageLimit()) {
    return
  }

  isMicOn.value = false

  chatMessages.value = chatMessages.value.concat([
    { message, isSent: true }, //보낸 메시지 추가
    { message: '', isSent: false, isSending: true }, //수신 대기 메시지 추가
  ])

  inputText.value = ''
  speechText.value = ''

  try {
    await sendMessage(message)
  } catch (e) {
    console.error('sendMessage', e)
    chatMessages.value = chatMessages.value.concat({
      message: t('메시지 수신 중 오류가 발생하였습니다.\n잠시 후 다시 시도해 주시기 바랍니다.'),
    })
  }
}

const checkMessageLimit = () => {
  const messageLimit = Number(process.env.flowise.messageLimit || -1)
  if (messageLimit > -1) {
    const sentMessageCount = chatMessages.value.filter((it) => it.isSent === true).length
    if (sentMessageCount >= messageLimit) {
      ui.confirm(
        t('긴 대화 알림'),
        t(
          '현재 채팅이 길어지고 있어 응답이 지연될 수 있습니다.보다 원활한 서비스를 위해 새로운 채팅으로 시작하시는 것을 권장드립니다.',
        ),
        t('새 채팅 시작'),
        t('메시지 전송'),
      )
        .onOk(() => {
          emit('request:new-session')
        })
        .onCancel(() => {
          sendInputText(true)
        })
      return false
    }
  }
  return true
}

const focusInput = () => {
  nextTick(() => {
    inputRef.value.focus()
  })
}
</script>

<style lang="scss" scoped>
.chat-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  flex: 0 0 650px;
  .chat-contents {
    display: flex;
    flex-direction: column;
    overflow: hidden;
    // flex: 1 1 auto;
    flex: 1;
    .chat-message-container {
      overflow-y: auto;
      flex: 1 1 auto;
      :deep(.q-message-avatar--received) {
        background-color: $blue-grey-2 !important;
        padding: 5px;
      }
      :deep(.q-message-text-content) {
        font-size: 15px;
        padding-left: 10px;
        padding-right: 10px;
        padding-top: 10px;
        max-width: 800px;
        iframe {
          border: 1px solid lightblue;
        }
        li {
          margin-bottom: 15px;
        }
        p {
          line-height: normal;
          margin-bottom: 10px;
        }
        h3 {
          font-size: 1.25rem;
        }
        h4 {
          font-size: 1.35rem;
        }
        strong {
          font-weight: 600;
        }
      }
    }
  }
  .result-spin {
    width: 100%;
    padding-left: 10px;
    padding-top: 20px;
  }
}

:deep(.q-field__control-container) {
  &:has(.recording) {
    .q-field__native.q-placeholder {
      display: none;
    }
  }
  .recording {
    display: flex;
    align-items: center;
  }
}
</style>
