<template>
  <div class="q-px-md q-py-sm prompts-container">
    <div>
      <q-chip
        v-for="(prompt, id) in activePrompts"
        :key="id"
        clickable
        color="white"
        text-color="grey-7"
        class="prompts"
        @click="selectPrompt(prompt)"
      >
        {{ $t(prompt.message) }}
      </q-chip>
    </div>
    <q-btn flat dense round icon="chat" color="grey-8" class="settings-button">
      <q-menu v-model="isShowMenu">
        <q-list style="min-width: 150px">
          <q-item
            clickable
            v-close-popup
            :active="activeMenu === 'user'"
            active-class="active-item"
            @click="selectMenu('user')"
          >
            <q-item-section
              >{{ $t('사용자 메세지') }}
              <q-btn flat dense round icon="settings" size="sm" @click.stop="openPromptSettings" />
            </q-item-section>
          </q-item>
          <q-item
            clickable
            v-close-popup
            :active="activeMenu === 'system'"
            active-class="active-item"
            @click="selectMenu('system')"
          >
            <q-item-section>{{ t('시스템 메세지') }}</q-item-section>
          </q-item>
        </q-list>
      </q-menu>
    </q-btn>
    <PromptSettingPopup v-model="isShowPromptSettingPopup" @reload="handleReload" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import useUI from 'src/common/module/ui'
import PromptService from 'src/services/prompt/PromptService'
import PromptSettingPopup from './PromptSettingPopup.vue'
import { t } from '/src/plugins/i18n'

const ui = useUI()
const emit = defineEmits(['prompt-selected'])

const userPrompts = ref([])
const systemPrompts = ref([])
const activePrompts = ref([])
const activeMenu = ref('user')
const isShowPromptSettingPopup = ref(false)
const isShowMenu = ref(false)

onMounted(async () => {
  await reloadPrompts() // 데이터 초기화
})

const reloadPrompts = async () => {
  ui.loading.show()

  const [userData, systemData] = await Promise.all([getUserPrompts(), getSystemPrompts()])

  activePrompts.value = userData.length > 0 ? userData : systemData
  activeMenu.value = userData.length > 0 ? 'user' : 'system'

  ui.loading.hide()
}

const getUserPrompts = async () => {
  try {
    const response = await PromptService.getUserPrompt()
    userPrompts.value = response.map((data) => ({
      id: data.id,
      message: data.message,
      sort: data.sort,
    }))
    return userPrompts.value
  } catch (error) {
    ui.notify.error(t('데이터 로딩 중 오류가 발생했습니다.'))
    return []
  }
}

const getSystemPrompts = async () => {
  try {
    const response = await PromptService.getSystemPrompt()
    systemPrompts.value = response.map((data) => ({
      id: data.id,
      message: data.message,
    }))
    return systemPrompts.value
  } catch (error) {
    ui.notify.error(t('데이터 로딩 중 오류가 발생했습니다.'))
    return []
  }
}

const selectMenu = (type) => {
  activePrompts.value = type === 'user' ? userPrompts.value : systemPrompts.value
  activeMenu.value = type
}

const selectPrompt = (prompt) => {
  emit('prompt-selected', t(prompt.message))
}

const openPromptSettings = () => {
  isShowPromptSettingPopup.value = true
  isShowMenu.value = false
}

const handleReload = async () => {
  await reloadPrompts()
  selectMenu('user')
  isShowPromptSettingPopup.value = false
}
</script>

<style scoped>
.prompts-container {
  display: grid;
  grid-template-columns: 1fr 30px;
  position: relative;
}

.settings-button {
  justify-self: end;
  align-self: start;
  z-index: 1;
}

.prompts {
  box-shadow: 0px 2px 5px 0px rgba(0, 0, 0, 0.5);
  border: 1px solid #757575 !important;
  font-size: 10pt;
  transition:
    background-color 0.3s,
    color 0.3s;
}
.active-item {
  background-color: #4d66d2 !important;
  color: white !important;
}
</style>
