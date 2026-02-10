<template>
  <q-dialog v-model="show" persistent>
    <q-card class="dialog-container">
      <q-card-section class="bg-secondary text-white">
        <div class="text-h6">{{ $t('규칙') }} {{ $t(status) }}</div>
      </q-card-section>
      <q-card-section>
        <q-form ref="form">
          <q-input v-model="modify.name" :label="$t('이름')" :rules="[$rules.required]" class="input-required" />
          <q-input
            v-model="modify.className"
            :label="$t('클래스명')"
            :rules="[$rules.required]"
            class="input-required"
          />
          <q-input v-model="modify.description" :label="$t('설명')" />
        </q-form>
      </q-card-section>
      <q-separator />
      <q-card-actions class="justify-between">
        <span class="field-required-indicate">{{ $t('필수 입력 필드') }}</span>
        <div>
          <q-btn flat color="negative" :label="$t('닫기')" @click="onCancel" />
          <q-btn flat :label="$t('저장')" color="primary" @click="onOk" />
        </div>
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>
<script setup>
import RuleService from 'src/services/modeling/RuleService'
import { defineEmits, defineExpose, ref } from 'vue'
import useUI from 'src/common/module/ui'
import { t } from '/src/plugins/i18n'

const ui = useUI()

const emit = defineEmits(['close'])
const show = ref(false)

const modify = ref({})

const status = ref(t('추가'))
const form = ref(null)

const open = (data) => {
  if (data == null) {
    status.value = t('추가')
    data = {
      id: null,
      name: '',
      className: '',
      description: '',
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
  if (!success) {
    ui.notify.warning(t('유효하지 않은 입력값이 있습니다.'))
    return false
  }

  try {
    ui.loading.show()
    status.value == t('추가')
      ? await RuleService.createRule(modify.value)
      : await RuleService.modifyRule(modify.value, modify.value.id)

    ui.notify.success(t('저장 되었습니다.'))
    show.value = false
    emit('close', 'ok')
  } finally {
    ui.loading.hide()
  }
}

const onCancel = () => {
  show.value = false
  emit('close', 'cancel')
}
</script>
<style scoped>
.dialog-container {
  width: 700px;
  max-width: 700px;
}
</style>
