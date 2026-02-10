<template>
  <q-dialog v-model="isShow" persistent>
    <q-card class="dialog-container">
      <q-card-section class="bg-secondary text-white">
        <div class="text-h6">{{ $t('가상 파라미터') + ' ' + modeName }}</div>
      </q-card-section>
      <q-card-section class="row contents">
        <div class="col-4 q-pr-xs full-height">
          <VirtualParameterForm
            ref="formRef"
            :init-value="initFormValue"
            :is-create-mode="isCreateMode"
            @change-tool-id="handleChangeToolId"
          />
        </div>
        <div class="col-8 flex column q-pl-xs">
          <BlocklySection
            ref="blocklySectionRef"
            :current-modify-parameter-id="modifyParameterId"
            :init-selected-parameter-ids="initFormValue.mappingParameterIds"
            :load-workspace-json="initFormValue.virtualWorkspace"
            :selected-tool-id="initFormValue.toolId"
          />
        </div>
      </q-card-section>
      <q-separator />
      <q-card-actions class="justify-between">
        <span class="field-required-indicate">{{ $t('필수 입력 필드') }}</span>
        <div>
          <q-btn flat :label="$t('blockly 정보 확인')" @click="handleBlocklyConfirmButtonClick" />
          <q-btn flat color="negative" :label="$t('닫기')" @click="handleCancelButton" />

          <q-btn v-if="isCreateMode" flat color="primary" :label="$t('저장')" @click="handleSaveButton" />
          <q-btn v-else flat color="primary" :label="$t('수정')" @click="handleModifyButton" />
        </div>
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup>
import { computed, defineEmits, ref } from 'vue'
import VirtualParameterForm from 'pages/modeling/virtualParameter/VirtualParameterForm.vue'
import BlocklySection from 'pages/modeling/virtualParameter/BlocklySection.vue'
import ParameterService from 'src/services/modeling/ParameterService'
import useUI from 'src/common/module/ui'
import { t } from '/src/plugins/i18n'

const { notify } = useUI()
const isShow = ref(false)
const isCreateMode = ref(true)
const modeName = computed(() => (isCreateMode.value ? t('추가') : t('수정')))

const formRef = ref()
const blocklySectionRef = ref()

const modifyParameterId = ref(null)
const initFormValue = ref({})

const emit = defineEmits(['close', 'saveSuccess'])

const showCreateDialog = (selectedTool) => {
  const toolId = selectedTool?.value?.id ?? selectedTool?.id ?? null
  isCreateMode.value = true
  modifyParameterId.value = null
  initFormValue.value = {
    toolId,
    mappingParameterIds: [],
    virtualWorkspace: null,
  }
  isShow.value = true
}

const props = defineProps({
  selectedToolId: {
    type: Number,
    required: false,
    default: null,
  },
})

const selectedToolId = ref(props.selectedToolId)

const showModifyDialog = async (parameterId) => {
  const parameter = await ParameterService.getVirtualParameterById(parameterId)
  modifyParameterId.value = parameterId
  initFormValue.value = parameter
  selectedToolId.value = parameter.toolId
  isCreateMode.value = false
  isShow.value = true
}

defineExpose({
  showCreate: showCreateDialog,
  showModify: showModifyDialog,
})
const handleChangeToolId = async (toolId) => {
  initFormValue.value.toolId = toolId
  initFormValue.value.virtualWorkspace = null
}

const handleBlocklyConfirmButtonClick = () => {
  blocklySectionRef.value.showBlocklyDebugDialog()
}

const validDataIfGetBody = async () => {
  const { success, formValue } = await formRef.value.validate()
  if (!success) {
    return null
  }

  const { mappingParameters, script, workspaceJson } = blocklySectionRef.value.getSectionData()

  if (!script || !workspaceJson) {
    notify.warning(t('Blockly 데이터가 없습니다'))
    return null
  }

  if (blocklySectionRef.value) {
    const parameterValidationResult = blocklySectionRef.value.validateAiModelParameters()
    if (!parameterValidationResult.isValid) {
      notify.error(parameterValidationResult.message)
      return null
    }
  }

  const body = {
    ...formValue,
    virtualScript: script,
    virtualWorkspace: JSON.stringify(workspaceJson),
    mappingParameters,
  }
  delete body['mappingParameterIds']
  return body
}

const handleSaveButton = async () => {
  const body = await validDataIfGetBody()
  if (body) {
    await ParameterService.createVirtualParameter(body)
    closeDialog()
    notify.success(t('저장 되었습니다.'))
    emit('saveSuccess')
  }
}

const handleModifyButton = async () => {
  const body = await validDataIfGetBody()
  if (body) {
    await ParameterService.modifyVirtualParameter(modifyParameterId.value, body)
    closeDialog()
    notify.success(t('수정 되었습니다.'))
    emit('saveSuccess')
  }
}

const closeDialog = () => {
  isShow.value = false
  initFormValue.value = {}
  selectedToolId.value = null
}

const handleCancelButton = () => {
  closeDialog()
  emit('close')
}
</script>

<style scoped lang="scss">
.dialog-container {
  width: 1500px;
  max-width: 1500px;

  .contents {
    height: 700px;
    overflow-y: auto;
  }
}
</style>
