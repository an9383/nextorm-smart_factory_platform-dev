<template>
  <q-dialog v-model="IS_VISIBLE" persistent>
    <q-card class="dialog-container">
      <q-card-section class="bg-secondary text-white">
        <div class="text-h6">{{ $t('Summary') }} {{ $t(dialogModeName) }}</div>
      </q-card-section>
      <q-card-section>
        <q-form ref="form">
          <q-input v-model="config.name" :label="$t('이름')" :rules="[$rules.required]" class="input-required" />
          <filterable-select
            v-model="config.tools"
            :label="$t('설비')"
            :options="toolList"
            option-value="id"
            option-label="name"
            multiple
            clearable
            class="input-required"
            :rules="[$rules.required]"
          />
          <q-toggle v-model="config.isUseFailover" :label="$t('Enable High Availability')" />
          <q-slide-transition appear>
            <div v-if="config.isUseFailover">
              <div class="row">
                <q-input
                  v-model="config.systemIp"
                  :label="$t('시스템 IP')"
                  :rules="config.isUseFailover ? [$rules.required] : undefined"
                  class="col-6 q-pr-sm input-required"
                />
                <q-input
                  v-model="config.connectionTimeout"
                  :label="$t('연결 확인 시간')"
                  :rules="config.isUseFailover ? [$rules.onlyInteger, $rules.required] : [$rules.onlyInteger]"
                  class="col-6 q-pl-sm input-required"
                />
                <q-input
                  v-model="config.hosts"
                  :label="$t('Hosts')"
                  :rules="config.isUseFailover ? [$rules.required] : undefined"
                  class="col-6 q-pr-sm input-required"
                />
              </div>
            </div>
          </q-slide-transition>
        </q-form>
      </q-card-section>

      <q-separator />

      <q-card-actions class="justify-between">
        <span class="field-required-indicate">{{ $t('필수 입력 필드') }}</span>
        <div>
          <q-btn flat color="negative" :label="$t('닫기')" @click="handleCloseClick" />
          <q-btn flat color="primary" :label="$t('저장')" @click="handleOkClick" />
        </div>
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>
<script setup>
import ToolService from 'src/services/modeling/ToolService'
import { defineEmits, onBeforeMount, ref, toRefs } from 'vue'
import useUI from 'src/common/module/ui'
import { t } from 'src/plugins/i18n'
import SummaryConfigService from 'src/services/modeling/SummaryConfigService'

const IS_VISIBLE = true
const MODIFY_MODE = 'modify'
const CREATE_MODE = 'create'

const DEFAULT_CONFIG_FORM = {
  name: '',
  tools: [],
  isUseFailover: false,
  systemIp: null,
  hosts: null,
  connectionTimeout: null,
}

const props = defineProps({
  configId: {
    type: Number,
    require: false,
  },
})

const emit = defineEmits(['ok', 'close'])
const { configId } = toRefs(props)

const ui = useUI()
const form = ref(null)

const config = ref(DEFAULT_CONFIG_FORM)
const toolList = ref([])

const dialogMode = configId.value ? MODIFY_MODE : CREATE_MODE
const dialogModeName = dialogMode === MODIFY_MODE ? t('수정') : t('추가')

const handleOkClick = async () => {
  const isValid = await form.value.validate()
  if (!isValid) {
    ui.notify.warning(t('유효하지 않은 입력값이 있습니다.'))
    return false
  }

  if (!config.value.isUseFailover) {
    config.value.systemIp = null
    config.value.connectionTimeout = null
    config.value.hosts = null
  }

  const body = {
    ...config.value,
    toolIds: config.value.tools.map((tool) => tool.id),
  }

  try {
    ui.loading.show()
    dialogMode === CREATE_MODE
      ? await SummaryConfigService.createSummaryConfig(body)
      : await SummaryConfigService.modifySummaryConfig(configId.value, body)

    ui.notify.success(t('저장 되었습니다.'))
    emitOk()
  } finally {
    ui.loading.hide()
  }
}

const emitOk = () => {
  emit('ok')
}

const handleCloseClick = () => {
  emit('close')
}

onBeforeMount(async () => {
  if (configId.value) {
    const configs = await SummaryConfigService.getSummaryConfigs()
    config.value = configs.find((config) => config.id === configId.value)
  }
  toolList.value = await ToolService.getTools()
})
</script>

<style scoped>
.dialog-container {
  width: 500px;
  max-width: 500px;
}
</style>
