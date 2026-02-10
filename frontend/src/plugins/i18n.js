import { createI18n } from 'vue-i18n'

export const PARAMETER_KEY_PREFIX = 'param.'
const PARAMETER_KEY_PREFIX_LENGTH = PARAMETER_KEY_PREFIX.length

const i18n = createI18n({
  locale: 'ko',
  fallbackRoot: false,
  silentFallbackWarn: true,
  silentTranslationWarn: true,
  globalInjection: true,
  missing: (locale, key) => {
    if (key.startsWith(PARAMETER_KEY_PREFIX)) {
      return key.slice(PARAMETER_KEY_PREFIX_LENGTH)
    }
    return key
  },
})
export default i18n
export const t = i18n.global.t
export const pt = (parameterName) => t(`${PARAMETER_KEY_PREFIX}${parameterName}`)
