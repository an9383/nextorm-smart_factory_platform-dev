<template>
  <q-dialog v-model="modelValue" persistent>
    <q-card class="dialog-container">
      <q-card-section class="bg-secondary text-white">
        <div class="text-h6">{{ $t('APC 모델') + ' ' + (!modelVersionId ? $t('생성') : $t('수정')) }}</div>
      </q-card-section>
      <q-card-section class="row contents">
        <div class="col-4 q-pr-xs full-height">
          <ApcModelForm ref="formRef" :model-version-id="modelVersionId" @change-version="handleChangeVersion" />
        </div>
        <div :class="'col-8  q-pl-xs relative-position ' + (!isEditMode && modelVersionId ? 'hover-overlay' : '')">
          <q-btn
            v-if="!isEditMode && modelVersionId"
            class="justify-center absolute hover-button-overlay z-max"
            color="green"
            icon="add"
            :label="$t('신규 버전 생성')"
            @click="onCreateVersion"
          />

          <BlocklySection ref="blocklySectionRef" :load-workspace-json="initFormValue.formulaWorkspace" />
        </div>
      </q-card-section>
      <q-separator />
      <q-card-actions class="justify-between">
        <span class="field-required-indicate">{{ $t('필수 입력 필드') }}</span>
        <div>
          <q-btn flat :label="$t('blockly 정보 확인')" @click="handleBlocklyConfirmButtonClick" />
          <q-btn flat color="negative" :label="$t('닫기')" @click="handleCancelButton" />

          <q-btn v-if="!modelVersionId" flat color="primary" :label="$t('저장')" @click="handleSaveButton" />
          <q-btn v-else flat color="primary" :label="$t('수정')" @click="handleModifyButton" />
        </div>
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup>
import { defineEmits, ref, watch } from 'vue'
import useUI from 'src/common/module/ui'
import { t } from '/src/plugins/i18n'
import BlocklySection from 'pages/apc/model/BlocklySection.vue'
import ApcModelForm from 'pages/apc/model/ApcModelForm.vue'
import ApcModelingService from 'src/services/apc/ApcModelService'

const ui = useUI()

const { notify } = useUI()

const formRef = ref()
const blocklySectionRef = ref()

const initFormValue = ref({})
const isEditMode = ref(false)

const { modelVersionId } = defineProps({
  modelVersionId: {
    type: Number,
    required: false,
    default: () => null,
  },
})

const emit = defineEmits(['saveSuccess'])

const onCreateVersion = () => {
  const changeConfirm = () => {
    blocklySectionRef.value.activeEditMode()
    formRef.value.createModelVersion()
    isEditMode.value = true
  }
  ui.confirm(t('신규 버전 생성'), t('현재 계산식의 신규 버전이 생성됩니다.')).onOk(changeConfirm)
}

const handleChangeVersion = (value) => {
  watch(
    () => value,
    () => {
      if (value.version === '') return
      initFormValue.value.version = value.version
      initFormValue.value.formulaScript = value.formulaScript
      initFormValue.value.formulaWorkspace = value.formulaWorkspace
      initFormValue.value.description = value.description
      initFormValue.value.isUse = value.isUse
      initFormValue.value.isUseNotify = value.isUseNotify
    },
    { deep: true },
  )
}

const handleBlocklyConfirmButtonClick = () => {
  blocklySectionRef.value.showBlocklyDebugDialog()
}

const validDataIfGetBody = async () => {
  const { success, formValue } = await formRef.value.validate()
  if (!success) {
    return null
  }
  const { script, workspaceJson } = blocklySectionRef.value.getSectionData()

  if (!script || !workspaceJson) {
    notify.warning(t('Blockly 데이터가 없습니다'))
    return null
  }

  const body = {
    ...formValue,
    formulaScript: script,
    formulaWorkspace: JSON.stringify(workspaceJson),
  }
  return body
}

const handleSaveButton = async () => {
  const body = await validDataIfGetBody()
  if (body) {
    try {
      await ApcModelingService.createApcModel(body)
    } catch (e) {
      return
    }
    close()
    notify.success(t('저장 되었습니다.'))
    emit('saveSuccess')
  }
}

const handleModifyButton = async () => {
  const body = await validDataIfGetBody()
  if (body) {
    try {
      ui.loading.show()
      if (!isEditMode.value) {
        await ApcModelingService.modifyApcModel(body.apcModelId, body)
      } else {
        body.version = ''
        await ApcModelingService.createApcModelVersion(body.apcModelId, body)
      }
    } finally {
      ui.loading.hide()
    }
    close()
    notify.success(t('수정 되었습니다.'))
    emit('saveSuccess')
  }
}

const close = () => {
  isEditMode.value = false
  initFormValue.value = {}
  emit('update:modelValue', false)
}

const handleCancelButton = () => {
  close()
}
watch(
  () => blocklySectionRef.value,
  () => {
    if (!modelVersionId) {
      blocklySectionRef.value.activeEditMode()
    }
  },
)
</script>

<style scoped lang="scss">
.dialog-container {
  width: 1500px;
  max-width: 1500px;

  .contents {
    height: auto;
  }
}

.hover-overlay::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(128, 128, 128, 0);
  transition: background-color 0.3s ease;
}

.hover-overlay:hover::after {
  background-color: rgba(128, 128, 128, 0.5);
}

.hover-button-overlay {
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: none;
  align-items: center;
}

.relative-position:hover .hover-button-overlay {
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>
