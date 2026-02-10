<template>
  <card-option-select
    v-model="config.toolId"
    :options="tools"
    :title="$t('설비')"
    :sub-title="$t('설비를 선택해 주세요.')"
    :placeholder="$t('설비')"
    :required="true"
  />
  <card-option-select-multiple
    v-model="config.parameterIds"
    :options="parameters"
    :title="$t('파라미터')"
    :sub-title="$t('파라미터를 선택해 주세요.')"
    :placeholder="$t('파라미터')"
    :required="true"
  />
  <card-option-select
    v-model="config.timeCriteria"
    :options="timeCriteriaOptions"
    :title="$t('통계 기준')"
    :sub-title="$t('통계 기준을 선택해 주세요.')"
    :placeholder="$t('통계 기준')"
    :required="true"
  />
  <card-option-select
    v-model="config.operator"
    :options="operatorOptions"
    :title="$t('연산')"
    :sub-title="$t('연산을 선택해 주세요.')"
    :placeholder="$t('연산')"
    :required="true"
  />
  <q-card flat bordered class="q-mt-md">
    <q-card-section class="bg-primary text-white">
      <div class="text-subtitle2 field-required">
        <q-icon name="date_range" color="white" class="q-mr-xs" size="20px" />
        {{ $t('조회기간') }}
      </div>
      <div class="text-caption">{{ $t('조회기간을 선택해 주세요.') }}</div>
    </q-card-section>
    <q-card-section class="q-ma-sm flex" style="gap: 20px">
      <filterable-select
        v-model="config.period"
        :placeholder="$t('조회기간')"
        :options="[
          { label: $t('기본'), value: 'DEFAULT' },
          { label: $t('기간선택'), value: 'PERIOD' },
        ]"
        option-value="value"
        option-label="label"
        emit-value
        map-options
        filled
        dense
        lazy-rules
        :rules="[$rules.required]"
      />
      <div class="row hasCustom-input" v-if="config.period === 'PERIOD'">
        <div class="col">
          <q-input filled v-model="from" lazy-rules :rules="[$rules.required]">
            <template v-slot:prepend>
              <q-icon name="event" class="cursor-pointer">
                <q-popup-proxy cover transition-show="scale" transition-hide="scale">
                  <q-date v-model="from" mask="YYYY-MM-DD HH:mm:ss">
                    <div class="row items-center justify-end">
                      <q-btn v-close-popup :label="$t('닫기')" color="primary" flat />
                    </div>
                  </q-date>
                </q-popup-proxy>
              </q-icon>
            </template>

            <template v-slot:append>
              <q-icon name="access_time" class="cursor-pointer">
                <q-popup-proxy cover transition-show="scale" transition-hide="scale">
                  <q-time v-model="from" mask="YYYY-MM-DD HH:mm:ss" with-seconds>
                    <div class="row items-center justify-end">
                      <q-btn v-close-popup :label="$t('닫기')" color="primary" flat />
                    </div>
                  </q-time>
                </q-popup-proxy>
              </q-icon>
            </template>
          </q-input>
        </div>
        <div class="q-mx-sm row items-center">~</div>
        <div class="col">
          <q-input filled v-model="to" lazy-rules :rules="[$rules.required]">
            <template v-slot:prepend>
              <q-icon name="event" class="cursor-pointer">
                <q-popup-proxy cover transition-show="scale" transition-hide="scale">
                  <q-date v-model="to" mask="YYYY-MM-DD HH:mm:ss">
                    <div class="row items-center justify-end">
                      <q-btn v-close-popup :label="$t('닫기')" color="primary" flat />
                    </div>
                  </q-date>
                </q-popup-proxy>
              </q-icon>
            </template>

            <template v-slot:append>
              <q-icon name="access_time" class="cursor-pointer">
                <q-popup-proxy cover transition-show="scale" transition-hide="scale">
                  <q-time v-model="to" mask="YYYY-MM-DD HH:mm:ss" with-seconds>
                    <div class="row items-center justify-end">
                      <q-btn v-close-popup :label="$t('닫기')" color="primary" flat />
                    </div>
                  </q-time>
                </q-popup-proxy>
              </q-icon>
            </template>
          </q-input>
        </div>
      </div>
    </q-card-section>
  </q-card>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useWidgetSettingConfig, widgetSettingProps } from '/src/common/module/widgetCommon'

import ToolService from 'src/services/modeling/ToolService'
import ParameterService from 'src/services/modeling/ParameterService'
import CardOptionSelect from 'components/dashboard/widgets/CardOptionSelect.vue'
import CardOptionSelectMultiple from 'components/dashboard/widgets/CardOptionSelectMultiple.vue'
import { pt, t } from 'src/plugins/i18n'
import useUI from 'src/common/module/ui'
import { date } from 'quasar'

defineProps(widgetSettingProps)

const timeCriteriaOptions = [
  {
    id: 'DAY',
    name: t('일'),
  },
  {
    id: 'WEEK',
    name: t('주'),
  },
  {
    id: 'MONTH',
    name: t('월'),
  },
]

const operatorOptions = [
  {
    id: 'AVG',
    name: t('평균'),
  },
  {
    id: 'MIN',
    name: t('최소'),
  },
  {
    id: 'MAX',
    name: t('최대'),
  },
]

const config = useWidgetSettingConfig({
  period: 'DEFAULT',
  statisticsCriteria: 'DAY',
  operator: 'AVG',
})

const ui = useUI()
const tools = ref([])
const parameters = ref([])

const loadTools = async () => {
  tools.value = await ToolService.getTools()
  if (tools.value.length !== 0 && !config.value.toolId) {
    config.value.toolId = tools.value[0].id
  }
}

const loadParameters = async (toolId) => {
  const parameterList = await ParameterService.getParameters({ toolId })
  parameters.value = parameterList.map((it) => ({ ...it, name: pt(it.name) }))
}

watch(
  () => config.value.toolId,
  (newValue) => {
    //설비 변경 시 설비 하위의 파라미터 목록 조회
    config.value.parameterIds = []
    parameters.value = []
    loadParameters(newValue)
  },
)

const validateFromTo = () => {
  const fromDate = new Date(config.value.from)
  const toDate = new Date(config.value.to)
  if (fromDate > toDate) {
    const temp = config.value.from
    config.value.from = config.value.to
    config.value.to = temp
    ui.notify.warning(t('조회기간 시작일시는 종료일시보다 작아야 합니다.'))
  }
}
watch(() => config.value.from, validateFromTo)
watch(() => config.value.to, validateFromTo)
const from = computed({
  get() {
    //화면 표시는 YYYY-MM-DD HH:mm:ss 포맷으로
    return config.value.from ? date.formatDate(new Date(config.value.from), 'YYYY-MM-DD HH:mm:ss') : ''
  },
  set(value) {
    //실제 위젯 config에는 ISO 포맷으로 저장
    config.value.from = value ? new Date(value).toISOString() : undefined
  },
})
const to = computed({
  get() {
    //화면 표시는 YYYY-MM-DD HH:mm:ss 포맷으로
    return config.value.to ? date.formatDate(new Date(config.value.to), 'YYYY-MM-DD HH:mm:ss') : ''
  },
  set(value) {
    //실제 위젯 config에는 ISO 포맷으로 저장
    config.value.to = value ? new Date(value).toISOString() : undefined
  },
})

onMounted(() => {
  loadTools()
  if (config.value.toolId) {
    loadParameters(config.value.toolId)
  }
})
</script>
