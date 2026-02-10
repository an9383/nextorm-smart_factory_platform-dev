import rules from '/src/common/rules'

export default {
  // eslint-disable-next-line no-unused-vars
  install(app, options) {
    app.config.globalProperties.$rules = rules
  },
}
