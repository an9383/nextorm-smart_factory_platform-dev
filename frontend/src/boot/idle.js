// src/boot/idle.js
import { boot } from 'quasar/wrappers'
import Vidle from 'v-idle-3'

export default boot(({ app }) => {
  app.use(Vidle)
})
