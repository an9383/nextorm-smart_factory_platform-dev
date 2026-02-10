import { boot } from 'quasar/wrappers'
import rules from '/src/plugins/rules'
import utils from '/src/plugins/utils'
import cascader from 'src/components/cascader/Cascader.vue'
import InnerLoading from 'src/components/common/InnerLoading.vue'
import SimpleTable from 'src/components/common/SimpleTable.vue'
import vPermission from 'src/directives/v-permission'
import FilterableSelect from 'src/components/common/FilterableSelect.vue'

export default boot(({ app }) => {
  app.use(rules)
  app.use(utils)
  app.directive('permission', vPermission)
  app.component('cascader', cascader)
  app.component('inner-loading', InnerLoading)
  app.component('simple-table', SimpleTable)
  app.component('filterable-select', FilterableSelect)
})
