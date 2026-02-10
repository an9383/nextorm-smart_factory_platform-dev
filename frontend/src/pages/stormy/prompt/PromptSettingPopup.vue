<template>
  <q-dialog v-model="modelValue" persistent>
    <q-card class="dialog-container">
      <q-card-section class="bg-secondary text-white">
        <div class="text-h6">{{ $t('User 메시지 관리') }}</div>
      </q-card-section>
      <q-card-section>
        <q-form ref="form">
          <div class="row">
            <q-input
              v-model="newMessage"
              :label="$t('프롬프트 내용')"
              filled
              dense
              clearable
              class="input-required col q-pr-sm"
            />
            <q-btn
              :label="$t('추가')"
              class="add_btn with_icon_btn sBtn secondary col-auto add-btn"
              @click="addMessage"
            />
          </div>
        </q-form>
        <UserMessageList v-model:userPrompts="userPrompts" @removedIds="trackRemovedIds" />
      </q-card-section>
      <q-separator />
      <q-card-actions class="justify-between">
        <span class="field-required-indicate">{{ $t('필수 입력 필드') }}</span>
        <div>
          <q-btn color="negative" :label="$t('닫기')" @click.stop="closeDialog" />
          <q-btn color="primary" :label="$t('저장')" @click="saveChanges" />
        </div>
      </q-card-actions>
      <InnerLoading v-model="isLoading" />
    </q-card>
  </q-dialog>
</template>

<script setup>
import { ref, watch, defineModel, defineEmits } from 'vue'
import useUI from 'src/common/module/ui'
import PromptService from 'src/services/prompt/PromptService'
import UserMessageList from './UserMessageList.vue'
import { t } from '/src/plugins/i18n'
import InnerLoading from 'src/components/common/InnerLoading.vue'
const ui = useUI()

const emit = defineEmits(['update:modelValue', 'reload'])
const modelValue = defineModel('modelValue', {
  type: Boolean,
  required: true,
})

const userPrompts = ref([])
const initialUserPrompts = ref([]) // 초기 상태 저장
const newMessage = ref('')
const removedIds = ref([])
const isLoading = ref(false)

watch(
  () => modelValue.value,
  async () => {
    if (modelValue.value) {
      showLoading(getUserPrompts)
    } else {
      initLocalPrompt()
    }
  },
)

const showLoading = async (task) => {
  try {
    isLoading.value = true
    await task()
  } finally {
    isLoading.value = false
  }
}

const addMessage = () => {
  if (!newMessage.value.trim()) {
    ui.notify.warning(t('사용자 메세지가 입력되지 않았습니다.'))
    return
  }
  userPrompts.value.push({
    id: `new_${Date.now()}`, // 임시 id
    message: newMessage.value,
    sort: userPrompts.value.length + 1,
  })
  newMessage.value = ''
}

const trackRemovedIds = (id) => {
  if (!removedIds.value.includes(id)) {
    removedIds.value.push(id)
  }
}

const saveChanges = async () => {
  const sortedPrompts = userPrompts.value.map((item, index) => ({
    ...item,
    sort: index + 1,
  }))

  const addedPrompts = sortedPrompts.filter((p) => typeof p.id === 'string' && p.id.startsWith('new_'))

  // confirmMessage 확인용
  const updatedText = sortedPrompts.filter((p) =>
    initialUserPrompts.value.some((o) => o.id === p.id && o.message !== p.message),
  )

  // sort 또는 userMessage 통합 (API 호출용)
  const updatedPrompts = sortedPrompts.filter((p) =>
    initialUserPrompts.value.some((o) => o.id === p.id && (o.message !== p.message || o.sort !== p.sort)),
  )

  const confirmMessage = `
    ${t('신규')}: ${addedPrompts.length}<br>
    ${t('수정')}: ${updatedText.length}<br>
    ${t('삭제')}: ${removedIds.value.length}
  `

  const totalCount = addedPrompts.length + updatedPrompts.length + removedIds.value.length

  ui.confirm(t('변경사항을 저장하시겠습니까?'), confirmMessage, t('저장'), t('취소'), { html: true }).onOk(async () => {
    try {
      if (totalCount === 0) {
        ui.notify.info(t('변경사항이 없습니다.'))
        return
      }

      await PromptService.modifyUserPrompt(
        addedPrompts.map(({ message, sort }) => ({
          message,
          sort,
        })),
        updatedPrompts.map(({ id, message, sort }) => ({
          id,
          message,
          sort,
        })),
        removedIds.value,
      )

      ui.notify.success(t('저장 되었습니다.'))
      emit('update:modelValue', false)
      emit('reload')
    } catch (error) {
      ui.notify.error(t('저장 중 오류가 발생했습니다.'))
    }
  })
}

const getUserPrompts = async () => {
  try {
    const response = await PromptService.getUserPrompt()

    if (!Array.isArray(response)) {
      throw new Error(t('Invalid response format'))
    }
    userPrompts.value = response.map((data) => ({
      id: data.id,
      message: data.message,
      sort: data.sort,
    }))

    initialUserPrompts.value = JSON.parse(JSON.stringify(userPrompts.value)) // 초기 상태 저장
  } catch (error) {
    ui.notify.error(t('데이터 로딩 중 오류가 발생했습니다.'))
  }
}

const closeDialog = () => {
  initLocalPrompt()
  emit('update:modelValue', false)
}

const initLocalPrompt = () => {
  initialUserPrompts.value = []
  removedIds.value = []
  newMessage.value = ''
}
</script>

<style scoped>
.dialog-container {
  width: 600px;
  max-width: 800px;
}

.add-btn {
  margin-top: 20px;
}
</style>
