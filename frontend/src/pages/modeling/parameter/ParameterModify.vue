<template>
  <q-dialog v-model="show" persistent>
    <q-card class="dialog-container">
      <q-card-section class="bg-secondary text-white">
        <div class="text-h6">{{ $t('파라미터') }} {{ $t(status) }}</div>
      </q-card-section>
      <q-card-section>
        <q-form ref="form">
          <div class="row">
            <q-input
              v-model="modify.name"
              :label="$t('파라미터명')"
              :rules="[$rules.required]"
              class="input-required col q-pr-sm"
            />
            <filterable-select
              :disable="isModifyMode"
              v-model="modify.toolName"
              :label="$t('설비')"
              :options="toolList"
              option-value="id"
              option-label="name"
              :rules="[$rules.required]"
              class="input-required col q-pl-sm"
            />
          </div>
          <div class="row">
            <q-input v-model="modify.unit" :label="$t('단위')" class="col-6 q-pr-sm" />
            <q-input
              v-model="modify.order"
              :label="$t('순서')"
              :rules="[$rules.natural, $rules.required]"
              class="input-required col-6 q-pl-sm"
            />
          </div>
          <div class="row">
            <filterable-select
              v-model="modify.type"
              :label="$t('타입')"
              :options="typeOptions"
              :rules="[$rules.required]"
              class="input-required col-6 q-pr-sm"
            />
            <filterable-select
              v-model="modify.dataType"
              :label="$t('데이터 타입')"
              :options="computedDataTypeOptions"
              :rules="[$rules.required]"
              option-value="value"
              option-label="label"
              option-disable="disable"
              emit-value
              map-options
              class="input-required col-6 q-pl-sm"
            />
          </div>
          <div class="row">
            <q-toggle v-model="modify.isSpecAvailable" :label="$t('Spec 설정')" class="col-6 q-pr-sm" />
            <q-input
              v-if="modify.type === 'META_DATA'"
              v-model="modify.metaValue"
              :rules="dynamicMetaDataValueRules"
              :label="$t('메타데이터')"
              class="col-6 q-pl-sm input-required"
            />
          </div>
          <q-slide-transition appear>
            <div class="spec_wrap" v-if="modify.isSpecAvailable">
              <div class="row">
                <q-input
                  v-model="modify.lsl"
                  :label="$t('LSL')"
                  :rules="[modify.isSpecAvailable, $rules.realNumber]"
                  :disable="modify.isAutoSpec"
                  class="col q-pr-sm"
                />
                <q-input
                  v-model="modify.lcl"
                  :label="$t('LCL')"
                  :rules="[modify.isSpecAvailable, $rules.realNumber]"
                  :disable="modify.isAutoSpec"
                  class="col q-pr-sm"
                />
                <q-input
                  v-model="modify.target"
                  :label="$t('TARGET')"
                  :rules="[modify.isSpecAvailable, $rules.realNumber]"
                  class="col q-pr-sm"
                />
                <q-input
                  v-model="modify.ucl"
                  :label="$t('UCL')"
                  :rules="[modify.isSpecAvailable, $rules.realNumber]"
                  :disable="modify.isAutoSpec"
                  class="col q-pr-sm"
                />
                <q-input
                  v-model="modify.usl"
                  :label="$t('USL')"
                  :rules="[modify.isSpecAvailable, $rules.realNumber]"
                  :disable="modify.isAutoSpec"
                  class="col q-sm"
                />
              </div>
              <q-toggle v-model="modify.isAutoSpec" :label="$t('자동 Spec 설정')" />
              <div class="autospec_wrap" v-if="modify.isAutoSpec">
                <q-input
                  v-model="modify.autoCalcPeriod"
                  :label="$t('자동 계산 주기(일)')"
                  :rules="[$rules.integer(1)]"
                />
                <q-toggle v-model="modify.isReqRecalculate" :label="$t('Request Recalculate')" />
              </div>
            </div>
          </q-slide-transition>
        </q-form>
      </q-card-section>

      <q-separator />

      <q-card-actions class="justify-between">
        <span class="field-required-indicate">{{ $t('필수 입력 필드') }}</span>
        <div>
          <q-btn flat color="negative" :label="$t('닫기')" @click="onCancel" />
          <q-btn flat color="primary" :label="$t('저장')" @click="onOk" />
        </div>
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>
<script setup>
import ParameterService from 'src/services/modeling/ParameterService'
import { computed, defineEmits, defineExpose, ref, watch } from 'vue'
import _ from 'lodash'
import useUI from 'src/common/module/ui'
import { t } from 'src/plugins/i18n'
import ToolService from 'src/services/modeling/ToolService'
import rules from 'src/common/rules'

const ui = useUI()

const emit = defineEmits(['close'])
const show = ref(false)

const modify = ref({})
const toolList = ref()
const typeOptions = ref([t('TRACE'), t('EVENT'), t('META_DATA')])
const dataTypeOptions = ref([t('DOUBLE'), t('STRING'), t('INTEGER'), t('IMAGE')])

const status = ref(t('추가'))
const form = ref(null)

const isModifyMode = computed(() => status.value !== t('추가'))
const dynamicMetaDataValueRules = computed(() => {
  if (modify.value.type !== 'META_DATA') {
    return []
  }
  if (modify.value.dataType === t('STRING')) {
    return [rules.required]
  } else if (modify.value.dataType === t('DOUBLE')) {
    return [rules.realNumber, rules.required]
  } else if (modify.value.dataType === t('INTEGER')) {
    return [rules.natural, rules.required]
  }
  return []
})

watch(
  () => modify.value.type,
  (newType) => {
    if (newType !== 'META_DATA') {
      modify.value.metaValue = null
    } else if (modify.value.dataType === t('IMAGE')) {
      modify.value.dataType = t('DOUBLE')
    }
  },
)

const open = async (data, selectedToolInfo) => {
  toolList.value = await ToolService.getTools()

  if (data == null) {
    status.value = t('추가')
    data = {
      id: null,
      toolId: selectedToolInfo.value.id,
      toolName: selectedToolInfo.value,
      name: '',
      type: t('TRACE'),
      dataType: t('DOUBLE'),
      isSpecAvailable: false,
      isAutoSpec: false,
      isReqRecalculate: false,
      autoCalcPeriod: null,
      order: null,
      lsl: null,
      lcl: null,
      ucl: null,
      usl: null,
      metaValue: null,
      createBy: 'admin',
      createAt: null,
    }
  } else {
    status.value = t('수정')
  }
  modify.value = { ...data }
  show.value = true
}

defineExpose({ open })

const onOk = async () => {
  const success = await form.value.validate()
  if (success) {
    if (_.isObject(modify.value.toolName)) {
      modify.value.toolId = modify.value.toolName.id
      modify.value.toolName = modify.value.toolName.name
    }
    try {
      ui.loading.show()
      modify.value.id === null
        ? await ParameterService.createParameter(modify.value)
        : await ParameterService.modifyParameter(modify.value.id, modify.value)

      ui.notify.success(t('저장 되었습니다.'))
      show.value = false
      emit('close', 'ok')
    } finally {
      ui.loading.hide()
    }
  } else {
    ui.notify.warning(t('유효하지 않은 입력값이 있습니다.'))
  }
}
const onCancel = () => {
  show.value = false
  emit('close', 'cancel')
}

const computedDataTypeOptions = computed(() => {
  return dataTypeOptions.value.map((option) => ({
    label: option,
    value: option,
    disable: modify.value.type === 'META_DATA' && option === t('IMAGE'),
  }))
})
</script>
<style scoped>
.dialog-container {
  width: 600px;
  max-width: 600px;
}

.spec_wrap {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.spec_wrap > .row,
.autospec_wrap > .row {
  min-height: 50px;
}
</style>
