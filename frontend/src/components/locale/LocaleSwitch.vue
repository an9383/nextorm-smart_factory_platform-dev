<template>
  <q-btn flat icon="translate" :label="t(locale)">
    <q-menu transition-show="jump-down" transition-hide="jump-up" auto-close>
      <q-list style="min-width: 100px">
        <q-item clickable v-for="lang in availableLocales" :key="lang" @click="changeLocale(lang)">
          <q-item-section>{{ t(lang) }}</q-item-section>
        </q-item>
      </q-list>
    </q-menu>
  </q-btn>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
import MetaDataService from 'src/services/modeling/MetaDataService'
import { useAppStore } from 'stores/app'
import LocalStorageService from 'src/services/LocalStorageService'
import UserService from 'src/services/system/UserService'
import { onBeforeMount } from 'vue'
import { useAuthStore } from 'stores/auth'

const store = useAppStore()
const { loginUser } = useAuthStore()
const { availableLocales, locale, t, setLocaleMessage } = useI18n()

const changeLocale = async (changeLocale) => {
  locale.value = changeLocale
  LocalStorageService.setLocale(changeLocale)
  UserService.changeLocale(changeLocale).then(() => {})

  const isCachedLanguage = store.isCachedLanguage(changeLocale)
  if (!isCachedLanguage) {
    const { messages } = await MetaDataService.getI18nMessagesByLanguage(changeLocale)
    setLocaleMessage(changeLocale, messages)
    store.putCachedLanguage(changeLocale)
  }
}

onBeforeMount(() => {
  const { locale: userLocale } = loginUser
  if (userLocale && userLocale !== locale.value) {
    changeLocale(userLocale)
  }
})
</script>
