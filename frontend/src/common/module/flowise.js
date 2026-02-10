import axios from 'axios'
import { t } from '/src/plugins/i18n'
import aiChatProfile from '/src/assets/ai_chat_profile.png'
import chatTitleImage from '/src/assets/ai_animated.gif'
import _ from 'lodash'
import { FlowiseClient } from 'flowise-sdk'
import { useAuthStore } from '/src/stores/auth'

export const AI_CHAT_NAME = process.env.app.aiChatName || 'STORMY'

export const loadFlowiseEmbedJS = () => {
  return import('https://cdn.jsdelivr.net/npm/flowise-embed/dist/web.js')
}

export const getDefaultTheme = (userImage, options = {}) => {
  const defaultTheme = {
    button: {
      backgroundColor: '#4D66D2',
      right: 20,
      bottom: 20,
      size: 'medium',
      iconColor: 'white',
    },
    chatWindow: {
      showTitle: true, // show/hide the title bar
      title: AI_CHAT_NAME,
      titleAvatarSrc: `${chatTitleImage}`,
      showAgentMessages: false,
      welcomeMessage: `${t('안녕하세요.')} ${AI_CHAT_NAME} ${t('입니다.')} ${t('무엇을 도와드릴까요?')}`,
      backgroundColor: '#ffffff',
      fontSize: 15,
      poweredByTextColor: '#ffffff',
      botMessage: {
        backgroundColor: '#4D66D21a',
        textColor: '#011D49',
        showAvatar: true,
        avatarSrc: `${aiChatProfile}`,
      },
      userMessage: {
        backgroundColor: '#4D66D2',
        textColor: '#ffffff',
        showAvatar: true,
        avatarSrc: userImage,
      },
      textInput: {
        placeholder: t('질문을 입력해주세요.'),
        backgroundColor: '#ffffff',
        textColor: '#011D49',
        sendButtonColor: '#4D66D2',
      },
    },
  }

  return _.merge({}, defaultTheme, options)
}

/**
 * Flowise Streaming(Web Socket) 방식
 * @param {*} apiHost
 * @param {*} chatFlowId
 * @returns
 */
// eslint-disable-next-line no-unused-vars
export function useFlowise(
  apiHost,
  chatFlowId,
  callback = {
    onStart: () => {},
    onToken: () => {},
    onEnd: () => {},
    onCreateSession: () => {},
  },
) {
  let client = undefined
  let sessionId = undefined
  const API_KEY = process.env.flowise.apiKey

  /**
   * 채팅방 Refresh
   */
  const createSession = () => {
    sessionId = undefined
    client = new FlowiseClient({ baseUrl: apiHost, apiKey: API_KEY })
  }

  const getMessages = async (paramSessionId) => {
    sessionId = paramSessionId

    const response = await axios.get(
      `${apiHost}/api/v1/chatmessage/${chatFlowId}?order=ASC&feedback=true&chatId=${sessionId}&memoryType=Buffer+Memory&sessionId=${sessionId}`,
      {
        headers: {
          // 'Content-Type': 'application/json',
          Authorization: `Bearer ${API_KEY}`,
        },
      },
    )
    return response.data
  }

  const applyCallback = (callback, ...args) => {
    if (typeof callback === 'function') {
      if (args.length > 0) {
        callback(...args)
      } else {
        callback()
      }
    }
  }

  /**
   * 메시지 전송
   * @param {*} message 보낼 메시지
   * @returns
   */
  const sendMessage = async (message) => {
    let response = ''
    // eslint-disable-next-line no-unused-vars
    const { accessToken } = useAuthStore()
    try {
      // For streaming prediction
      const prediction = await client.createPrediction({
        chatflowId: chatFlowId,
        question: message,
        streaming: true,
        chatId: sessionId,
        overrideConfig: {
          vars: {
            FOM_ACCESS_TOKEN: accessToken,
            FOM_BASE_URL: window.location.origin,
          },
        },
      })

      for await (const chunk of prediction) {
        switch (chunk.event) {
          case 'start':
            applyCallback(callback.onStart)
            break
          case 'end':
            applyCallback(callback.onEnd)
            break
          case 'metadata':
            if (!sessionId) {
              sessionId = chunk.data.chatId
              applyCallback(callback.onCreateSession, sessionId, message)
            }
            break
          case 'error':
            applyCallback(callback.onError, chunk.data)
            break
          case 'token':
            response += chunk.data
            applyCallback(callback.onToken, chunk.data)
            break
        }
      }
    } catch (error) {
      console.error(error)
      applyCallback(callback.onError)
    }
    return response
  }
  const requestColumnMapping = async (message, chatFlowId) => {
    try {
      const prediction = await client.createPrediction({
        chatflowId: chatFlowId,
        question: message,
        streaming: false,
        chatId: sessionId,
      })
      return prediction.text || prediction.data || prediction
    } catch (error) {
      console.error(error)
      return null
    }
  }
  const aiRequestMessage = (type, selectRow) => {
    return `selectRow = ${JSON.stringify(selectRow.value)}
    mapping ${type} You Must response just json array, not any word or sentence.`
  }
  createSession()
  return {
    createSession,
    sendMessage,
    getMessages,
    requestColumnMapping,
    aiRequestMessage,
  }
}
