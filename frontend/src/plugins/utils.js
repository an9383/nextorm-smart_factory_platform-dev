import utils from '/src/common/utils'

export default {
  // eslint-disable-next-line no-unused-vars
  install(app, options) {
    app.config.globalProperties.$utils = utils
  },
}
