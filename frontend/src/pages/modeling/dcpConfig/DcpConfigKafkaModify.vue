<template>
  <q-dialog v-model="show" persistent>
    <q-card class="dialog-container">
      <q-card-section class="bg-secondary text-white">
        <div class="text-h6">{{ $t('Kafka 설정') }} {{ $t(status) }}</div>
      </q-card-section>
      <q-separator />
      <div class="row">
        <div class="col-4">
          <q-card-section>
            <q-form ref="form">
              <div v-if="!isCreateMode" class="row-topic">
                <q-input v-model="formData.topic" :label="$t('Topic')" class="input-required col" disable />
              </div>
              <div class="row-bootstrapServer">
                <q-input
                  v-model="formData.bootstrapServer"
                  :label="$t('Bootstrap 서버')"
                  :rules="[$rules.required]"
                  class="input-required col-bootstrap"
                />
              </div>
            </q-form>
          </q-card-section>
        </div>
      </div>

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
import { computed, defineEmits, onBeforeMount, ref } from 'vue'
import ToolService from 'src/services/modeling/ToolService'
import { t } from 'src/plugins/i18n'
import useUI from 'src/common/module/ui'

const isCreateMode = ref(true)

const emit = defineEmits(['close'])
const show = ref(true)
const ui = useUI()
const form = ref(null)

const formData = ref({
  topic: null,
  bootstrapServer: null,
})

const props = defineProps({
  toolId: {
    required: true,
    type: Number,
  },
})

const status = computed(() => {
  return isCreateMode.value ? t('추가') : t('수정')
})

const onOk = async () => {
  const success = await form.value.validate()
  if (!success) {
    ui.notify.warning(t('유효하지 않은 입력값이 있습니다.'))
    return false
  }
  try {
    isCreateMode.value === true
      ? await ToolService.createToolKafka(props.toolId, formData.value.bootstrapServer)
      : await ToolService.modifyToolKafka(props.toolId, formData.value.bootstrapServer)
    ui.notify.success(t('저장 되었습니다.'))
    show.value = false
    emit('close', 'ok')
  } finally {
    /* empty */
  }
}

const onCancel = () => {
  emit('close', 'cancel')
}

onBeforeMount(async () => {
  const toolId = props.toolId
  if (await ToolService.checkExistsToolKafka(props.toolId)) {
    const toolKafka = await ToolService.getToolKafka(toolId)
    formData.value = { ...toolKafka }
    isCreateMode.value = false
  }
})
</script>

<style scoped>
.dialog-container {
  width: 500px;
  max-width: 500px;
}
.col-4 {
  width: 350px;
}
</style>
