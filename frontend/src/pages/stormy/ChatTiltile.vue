<template>
  <div class="chat-title-container">
    <q-btn flat ripple="false" unelevated>
      <q-icon name="chat" color="white" class="q-mr-sm" />
      <span>{{ title || $t('제목 없음') }}</span>
      <template v-if="chatId">
        <q-icon name="expand_more"></q-icon>
        <q-menu v-model="isMenuShow">
          <q-list style="min-width: 100px">
            <q-item clickable>
              <q-item-section>{{ $t('제목 수정') }} </q-item-section>
              <q-popup-edit v-slot="scope" @save="updateTitle" @before-hide="() => (isMenuShow = false)">
                <q-item class="q-pa-none q-mt-lg">
                  <q-item-section>
                    <q-input
                      type="text"
                      v-model="scope.value"
                      :hint="$t('새로운 Chat 이름을 입력 해 주세요.')"
                      dense
                      autofocus
                      class="input-field input-title"
                      @keyup.enter="scope.set"
                    />
                  </q-item-section>
                </q-item>
                <q-item class="row q-my-md justify-end">
                  <q-btn
                    @click="scope.cancel"
                    class="negative-btn q-mr-xs"
                    flat
                    :label="$t('취소')"
                    v-close-popup="2"
                  />
                  <q-btn class="positive-btn" flat @click="scope.set" :label="$t('확인')" />
                </q-item>
              </q-popup-edit>
            </q-item>
            <q-item clickable v-close-popup @click="confirmDelete">
              <q-item-section>{{ $t('삭제') }}</q-item-section>
            </q-item>
          </q-list>
        </q-menu>
      </template>
    </q-btn>
  </div>
</template>
<script setup>
import { ref, defineProps } from 'vue'
import { t } from 'src/plugins/i18n'
import useUI from 'src/common/module/ui'
import ChatService from '/src/services/ChatService'
const props = defineProps({
  chatId: {
    type: String,
    required: false,
  },
  title: {
    type: String,
    required: false,
  },
})
const isMenuShow = ref(false)
const ui = useUI()
const emit = defineEmits(['titleChange', 'delete'])

const confirmDelete = () => {
  ui.confirm(t('삭제'), t('채팅 내용이 전부 삭제 됩니다. 삭제하시겠습니까?'), t('삭제'), t('취소')).onOk(deleteMessage)
}

const deleteMessage = async () => {
  await ChatService.deleteChat(props.chatId)
  ui.notify.success(t('채팅이 삭제 되었습니다.'))
  emit('delete')
}

const updateTitle = async (value) => {
  await ChatService.updateChatTitle(props.chatId, value)
  ui.notify.success(t('제목이 수정 되었습니다.'))
  emit('titleChange', value)
}
</script>
<style lang="scss">
.chat-title-container {
  display: flex;
  justify-content: center;
  button {
    border: none;
    margin-left: 5px;
    padding: 0 5px;
  }
  button:hover {
    background-color: lightgray;
    cursor: pointer;
  }
}
.input-title {
  width: 300px !important;
}
.positive-btn {
  background-color: var(--mainBgColor) !important;
  border: 1px solid var(--mainColor);
  color: var(--mainColor) !important;
  min-height: 30px !important;
}

.negative-btn {
  background-color: rgba(193, 0, 21, 0.1019607843) !important;
  border: 1px solid #c10015;
  color: #c10015 !important;
  min-height: 30px !important;
}
</style>
