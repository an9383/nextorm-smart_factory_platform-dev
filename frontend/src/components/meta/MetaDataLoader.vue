<template>
  <slot />
</template>
<script setup>
import { useI18n } from 'vue-i18n'
import MetaDataService from 'src/services/modeling/MetaDataService'
import { useAppStore } from 'stores/app'
import LocalStorageService from 'src/services/LocalStorageService'

const DEFAULT_LOCALE = 'ko'

const store = useAppStore()
const { setLocaleMessage, locale } = useI18n()

const loadMetaData = async () => {
  await store.loadSystemInfo()
  const languages = await MetaDataService.getI18nLanguages()

  let initLocale = LocalStorageService.getLocale(DEFAULT_LOCALE)
  if (!languages.includes(initLocale)) {
    initLocale = languages.length !== 0 ? languages[0] : DEFAULT_LOCALE
  }

  languages.forEach((lang) => {
    setLocaleMessage(lang, {})
  })

  const { messages } = await MetaDataService.getI18nMessagesByLanguage(initLocale)
  setLocaleMessage(initLocale, messages)
  locale.value = initLocale
  store.putCachedLanguage(locale)
}
await loadMetaData()
</script>

<style>
.blocklyDropDownDiv {
  z-index: 6000;
}
</style>
