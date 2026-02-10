<template>
  <q-card class="full-height">
    <q-card-section>
      <q-form class="full-width full-height" ref="form">
        <div class="row">
          <q-input v-model="formValue.name" :label="$t('이름')" :rules="[$rules.required]" class="input-required col" />
        </div>
        <div class="row">
          <filterable-select
            :disable="!isCreateMode"
            v-model="formValue.toolId"
            :label="$t('설비')"
            :options="tools"
            option-value="id"
            option-label="name"
            :rules="[$rules.required]"
            class="input-required col-6 q-pr-sm"
            emit-value
            map-options
            @update:model-value="handleToolChange"
          />
          <q-input
            v-model="formValue.order"
            :label="$t('순서')"
            :rules="[$rules.natural, $rules.required]"
            class="input-required col-6 q-pl-sm"
          />
        </div>
        <div class="row">
          <filterable-select
            v-model="formValue.type"
            :label="$t('타입')"
            :options="typeOptions"
            :rules="[$rules.required]"
            @change="handleToolChange"
            class="input-required col-6 q-pr-sm"
          />
          <filterable-select
            v-model="formValue.dataType"
            :label="$t('데이터 타입')"
            :options="dataTypeOptions"
            :rules="[$rules.required]"
            class="input-required col-6 q-pl-sm"
          />
        </div>
        <div class="row">
          <filterable-select
            v-model="formValue.dcpConfigId"
            :label="$t('Dcp설정')"
            :options="dcpConfigIds"
            :rules="[$rules.required]"
            class="input-required col"
          />
        </div>

        <q-toggle v-model="formValue.isSpecAvailable" :label="$t('Spec 설정')" />
        <q-slide-transition appear>
          <div v-if="formValue.isSpecAvailable">
            <div class="row">
              <q-input v-model="formValue.usl" :label="$t('USL')" :rules="[$rules.realNumber]" class="col q-pr-sm" />
              <q-input v-model="formValue.ucl" :label="$t('UCL')" :rules="[$rules.realNumber]" class="col q-pr-sm" />
              <q-input
                v-model="formValue.target"
                :label="$t('TARGET')"
                :rules="[$rules.realNumber]"
                class="col q-pr-sm"
              />
              <q-input v-model="formValue.lcl" :label="$t('LCL')" :rules="[$rules.realNumber]" class="col q-pr-sm" />
              <q-input v-model="formValue.lsl" :label="$t('LSL')" :rules="[$rules.realNumber]" class="col q-sm" />
            </div>
            <q-toggle v-model="formValue.isAutoSpec" class="autospec-toggle" :label="$t('자동 Spec 설정')" />
            <div v-if="formValue.isAutoSpec">
              <q-input
                v-model="formValue.autoCalcPeriod"
                :label="$t('자동 계산 주기(일)')"
                :rules="[$rules.integer(1)]"
              />
              <q-toggle v-model="formValue.isReqRecalculate" :label="$t('Request Recalculate')" />
            </div>
          </div>
        </q-slide-transition>
      </q-form>
    </q-card-section>
  </q-card>
</template>

<script setup>
import ToolService from 'src/services/modeling/ToolService'
import { onMounted, ref, watch } from 'vue'
import DcpConfigService from 'src/services/modeling/DcpConfigService'

const typeOptions = ['TRACE', 'EVENT', 'HEALTH']
const dataTypeOptions = ['DOUBLE', 'STRING', 'INTEGER']
const baseFormValue = {
  name: null,
  toolId: null,
  dcpConfigId: null,
  type: typeOptions[0],
  dataType: dataTypeOptions[0],
  order: null,
  target: null,
  lsl: null,
  lcl: null,
  usl: null,
  ucl: null,
  isSpecAvailable: false,
  isAutoSpec: false,
  autoCalcPeriod: null,
  isReqRecalculate: false,
}

const props = defineProps({
  initValue: {
    type: Object,
    required: false,
    default: () => new Object(),
  },
  isCreateMode: {
    type: Boolean,
    required: false,
    default: () => true,
  },
})

const emit = defineEmits({
  changeToolId: (toolId) => toolId,
})

defineExpose({
  validate: async () => {
    const result = await form.value.validate()
    if (!result) {
      return { success: false, formValue: null }
    }
    return {
      success: true,
      formValue: formValue.value,
    }
  },
})

watch(
  () => props.initValue,
  async (val) => {
    if (!formValue.value.toolId) {
      formValue.value = { ...baseFormValue, ...(val || {}) }
    }
    if (formValue.value.toolId) {
      await changeDcpConfigIds(formValue.value.toolId)
    } else {
      dcpConfigIds.value = []
    }
  },
  { immediate: true, deep: true },
)

const initFormValue = {
  ...baseFormValue,
  ...props.initValue,
}

const form = ref(null)
const tools = ref([])
const dcpConfigIds = ref([])
const formValue = ref(initFormValue)

const changeDcpConfigIds = async (toolId) => {
  const dcpConfigList = await DcpConfigService.getDcpConfigsByTool(toolId)
  dcpConfigIds.value = dcpConfigList.map((dcpConfig) => dcpConfig.id)
}

const emitChangeToolId = (toolId) => {
  emit('changeToolId', toolId)
}

const handleToolChange = async (toolId) => {
  formValue.value = {
    ...formValue.value,
    toolId: toolId, // 선택된 새 toolId 설정
    dcpConfigId: null,
  }
  emitChangeToolId(toolId)
  await changeDcpConfigIds(toolId)
}

onMounted(async () => {
  tools.value = await ToolService.getTools()

  let toolId = props.initValue.toolId
  if (!toolId) {
    toolId = tools.value[0] && tools.value[0].id
  }

  if (toolId) {
    await changeDcpConfigIds(toolId)
  }
})
</script>

<style scoped>
.autospec-toggle {
  margin: 15px 0px;
}
</style>
