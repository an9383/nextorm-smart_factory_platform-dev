<template>
  <q-page class="q-pa-sm">
    <q-card>
      <q-card-section class="row">
        <q-item class="col-lg-4 col-md-4 col-sm-6 col-xs-6">
          <q-item-section>
            <q-input filled v-model="fromDateTime" stack-label :label="$t('조회 시작일')">
              <template v-slot:prepend>
                <q-icon name="event" class="cursor-pointer">
                  <q-popup-proxy cover transition-show="scale" transition-hide="scale">
                    <q-date v-model="fromDateTime" mask="YYYY-MM-DD HH:mm">
                      <div class="row items-center justify-end">
                        <q-btn v-close-popup :label="$t('Close')" color="primary" flat />
                      </div>
                    </q-date>
                  </q-popup-proxy>
                </q-icon>
              </template>
              <template v-slot:append>
                <q-icon name="access_time" class="cursor-pointer">
                  <q-popup-proxy cover transition-show="scale" transition-hide="scale">
                    <q-time v-model="fromDateTime" mask="YYYY-MM-DD HH:mm" format24h>
                      <div class="row items-center justify-end">
                        <q-btn v-close-popup :label="$t('Close')" color="primary" flat />
                      </div>
                    </q-time>
                  </q-popup-proxy>
                </q-icon>
              </template>
            </q-input>
          </q-item-section>
          <q-item-section>
            <q-input filled v-model="toDateTime" stack-label :label="$t('조회 종료일')">
              <template v-slot:prepend>
                <q-icon name="event" class="cursor-pointer">
                  <q-popup-proxy cover transition-show="scale" transition-hide="scale">
                    <q-date v-model="toDateTime" mask="YYYY-MM-DD HH:mm">
                      <div class="row items-center justify-end">
                        <q-btn v-close-popup :label="$t('Close')" color="primary" flat />
                      </div>
                    </q-date>
                  </q-popup-proxy>
                </q-icon>
              </template>
              <template v-slot:append>
                <q-icon name="access_time" class="cursor-pointer">
                  <q-popup-proxy cover transition-show="scale" transition-hide="scale">
                    <q-time v-model="toDateTime" mask="YYYY-MM-DD HH:mm" format24h>
                      <div class="row items-center justify-end">
                        <q-btn v-close-popup :label="$t('Close')" color="primary" flat />
                      </div>
                    </q-time>
                  </q-popup-proxy>
                </q-icon>
              </template>
            </q-input>
          </q-item-section>
        </q-item>
        <q-item class="col-lg-3 col-md-3 col-sm-4 col-xs-4">
          <q-item-section>
            <filterable-select
              outlined
              v-model="selectTool"
              :options="toolOptions"
              option-value="id"
              option-label="name"
              @update:model-value="getParametersByTool"
              :label="$t('Tool')"
            />
          </q-item-section>
          <q-item-section>
            <filterable-select
              outlined
              v-model="selectParameter"
              :options="parameterOptions"
              option-value="id"
              option-label="name"
              @update:model-value="autoSearchCheck"
              :label="$t('Parameter')"
            />
          </q-item-section>
        </q-item>
        <q-item class="col-lg-3 col-md-3 col-sm-3 col-xs-3">
          <q-item-section>
            <q-btn icon="search" color="primary" @click="onSearch" :label="t('조회')" />
          </q-item-section>
          <q-item-section>
            <q-checkbox v-model="autoSearch" :label="$t('자동 조회')" />
          </q-item-section>
        </q-item>
      </q-card-section>
      <q-separator />
      <q-card-section>
        <div style="height: calc(100vh - 220px)">
          <heat-map :datas="datas"></heat-map>
        </div>
      </q-card-section>
    </q-card>
  </q-page>
</template>

<script setup>
import { ref } from 'vue'
import ToolService from 'src/services/modeling/ToolService.js'
import ParameterService from 'src/services/modeling/ParameterService.js'
import { date } from 'quasar'
import HeatMap from 'src/components/chart/HeatMap.vue'
import useUI from 'src/common/module/ui'
import ParameterDataService from 'src/services/parameterData/ParameterDataService'

const ui = useUI()

const toDay = date.formatDate(Date.now(), 'YYYY/MM/DD HH:mm')
const fromDay = date.formatDate(date.subtractFromDate(Date.now(), { days: 1 }), 'YYYY/MM/DD HH:mm')
// const dateRange = ref({ from: fromDay, to: toDay })

const fromDateTime = ref(fromDay)
const toDateTime = ref(toDay)

const toolOptions = ref([])
const parameterOptions = ref([])
const selectTool = ref(null)
const selectParameter = ref(null)
const autoSearch = ref(true)
const datas = ref([])

ToolService.getTools().then((tools) => {
  toolOptions.value = tools
  if (toolOptions.value.length > 0) {
    selectTool.value = toolOptions.value[0]
    getParametersByTool()
  }
})

const getParametersByTool = () => {
  ParameterService.getParameters({ toolId: selectTool.value.id }).then((parameters) => {
    parameterOptions.value = parameters
    if (parameterOptions.value.length > 0) {
      selectParameter.value = parameterOptions.value[0]
    }
  })
  selectParameter.value = []
}
const autoSearchCheck = () => {
  if (autoSearch.value) {
    onSearch()
  }
}
const onSearch = () => {
  if (selectParameter.value == null) {
    ui.notify.warning(t('파라미터는 필수입니다.'))
    return
  }
  ui.loading.show()

  const fromDate = new Date(fromDateTime.value).toISOString()
  const toDate = new Date(toDateTime.value).toISOString()
  ParameterDataService.getEcoParameters([selectParameter.value.id], fromDate, toDate)
    .then((data) => {
      datas.value = data.datas
        .filter((d) => d[1] != null && d[2] != null && d[3] != null)
        .map((d) => [d[1], d[2], d[3]])
    })
    .finally(() => {
      ui.loading.hide()
    })
}
</script>
