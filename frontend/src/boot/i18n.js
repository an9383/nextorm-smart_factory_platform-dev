import { boot } from 'quasar/wrappers'
import i18n, { pt } from 'src/plugins/i18n'

export default boot(({ app }) => {
  app.use(i18n)
  app.config.globalProperties.$pt = pt
})
