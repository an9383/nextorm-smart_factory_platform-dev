<template>
  <q-card bordered flat class="chat-detail-container">
    <q-item class="bg-white q-py-sm header">
      <q-item-section class="no-wrap q-py-xs">
        <q-item-label class="text-gray">{{ $t('상세 내용') }}</q-item-label>
        <q-item-label caption class="text-gray">{{ $t('상세 결과 창입니다.') }}</q-item-label>
      </q-item-section>
    </q-item>
    <q-separator color="lightgray" />
    <q-card-section class="q-py-md q-px-xs chat-contents">
      <div class="chat-message-container q-px-lg q-py-sm" ref="chatMessageContainer">
        <q-chat-message
          :name="AI_CHAT_NAME"
          :avatar="aiChatProfile"
          :text="[marked.parse(props.message || '')]"
          text-color="black"
          text-html
          bg-color="grey-2"
        >
        </q-chat-message>
      </div>
    </q-card-section>
  </q-card>
</template>

<script setup scoped>
import { defineProps, ref, watch, nextTick } from 'vue'
import aiChatProfile from '/src/assets/ai_chat_profile.png'
import { AI_CHAT_NAME } from 'src/common/module/flowise'
import { marked } from 'marked'

const props = defineProps({
  message: { type: String, default: '' },
})
const chatMessageContainer = ref()

watch(
  () => props.message,
  () => {
    nextTick(() => {
      chatMessageContainer.value.scrollTop = chatMessageContainer.value.scrollHeight
    })
  },
  { immediate: true },
)
</script>

<style lang="scss" scoped>
.chat-detail-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  flex: 1 1;
  .header {
    flex: 0 0 auto;
    .right-buttons {
      flex-direction: row;
    }
  }
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
      // }
    }
  }
}
</style>
