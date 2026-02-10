<template>
  <q-dialog v-model="showDialog" persistent>
    <q-card class="dialog-container">
      <q-card-section class="bg-secondary text-white">
        <div class="text-h6">{{ dialogTitle }}</div>
      </q-card-section>
      <q-card-section>
        <q-form ref="form">
          <q-input v-model="formData.name" :label="$t('이름')" :rules="[$rules.required]" class="input-required" />
          <filterable-select
            v-model="formData.toolId"
            :label="$t('설비')"
            :rules="[$rules.required]"
            class="input-required"
            :options="tools"
            option-value="id"
            option-label="name"
            outlined
            emit-value
            map-options
            @update:modelValue="handleToolChange"
          />
          <filterable-select
            v-model="formData.parameterId"
            :label="$t('파라미터')"
            :rules="[$rules.required]"
            class="input-required"
            :options="parameters"
            option-value="id"
            :option-label="formattingParameterLabel"
            outlined
            emit-value
            map-options
          />

          <filterable-select
            v-model="formData.alarmIntervalCodeId"
            :label="$t('알람 주기')"
            :rules="[$rules.required]"
            class="input-required"
            :options="alarmIntervalCodes"
            option-value="id"
            option-label="name"
            outlined
            emit-value
            map-options
          />

          <div class="row">
            <q-checkbox v-model="formData.isAlarmControlSpecOver" :label="$t('컨트롤 스펙오버 알람사용')" />
            <q-checkbox v-model="formData.isAlarmSpecOver" :label="$t('스펙오버 알람사용')" />
          </div>

          <div class="q-mt-md">
            <div class="text-subtitle2 q-mb-sm">{{ $t('수신자 및 알림 유형') }}</div>
            <q-table
              flat
              bordered
              :rows="recipientRows"
              :columns="recipientColumns"
              row-key="userId"
              :pagination="{ rowsPerPage: 0 }"
              hide-pagination
              no-data-label="사용자를 선택해주세요"
            >
              <template v-slot:body-cell-user="props">
                <q-td :props="props">
                  {{ props.row.name }}
                </q-td>
              </template>
              <template v-slot:body-cell-email="props">
                <q-td :props="props">
                  <q-checkbox
                    v-model="props.row.email.selected"
                    :disable="!props.row.email.available"
                    @update:model-value="handleNotificationChange(props.row)"
                  />
                  <q-tooltip v-if="!props.row.email.available" class="bg-negative">
                    {{ t('이메일 정보가 없습니다') }}
                  </q-tooltip>
                </q-td>
              </template>
              <template v-slot:body-cell-sms="props">
                <q-td :props="props">
                  <q-checkbox
                    v-model="props.row.sms.selected"
                    :disable="!props.row.sms.available"
                    @update:model-value="handleNotificationChange(props.row)"
                  />
                  <q-tooltip v-if="!props.row.sms.available" class="bg-negative">
                    {{ t('전화번호 정보가 없습니다') }}
                  </q-tooltip>
                </q-td>
              </template>
              <template v-slot:body-cell-kakao="props">
                <q-td :props="props">
                  <q-checkbox
                    v-model="props.row.kakao.selected"
                    :disable="!props.row.kakao.available"
                    @update:model-value="handleNotificationChange(props.row)"
                  />
                  <q-tooltip v-if="!props.row.kakao.available" class="bg-negative">
                    {{ t('전화번호 정보가 없습니다') }}
                  </q-tooltip>
                </q-td>
              </template>
            </q-table>
            <div v-if="recipientValidationError" class="text-negative text-caption q-mt-xs">
              {{ recipientValidationError }}
            </div>
          </div>
        </q-form>
      </q-card-section>
      <q-separator />
      <q-card-actions class="justify-between">
        <span class="field-required-indicate">{{ $t('필수 입력 필드') }}</span>
        <div>
          <q-btn flat color="negative" :label="$t('닫기')" @click="emit('close')" />
          <q-btn flat :label="saveButtonLabel" color="primary" @click="handleSaveButtonClick" />
        </div>
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup>
import { computed, defineEmits, onBeforeMount, ref } from 'vue'
import useUI from 'src/common/module/ui'
import { useI18n } from 'vue-i18n'
import UserService from 'src/services/system/UserService'
import CodeService from 'src/services/system/CodeService'
import ToolService from 'src/services/modeling/ToolService'
import ParameterService from 'src/services/modeling/ParameterService'
import OcapService from 'src/services/ocap/OcapService'
import FilterableSelect from 'components/common/FilterableSelect.vue'

const ALARM_INTERVAL_CODE_CATEGORY = 'OCAP_ALARM_INTERVAL'

const showDialog = true

const props = defineProps({
  ocapAlarmId: {
    type: Number,
    required: false,
    default: undefined,
  },
})
const emit = defineEmits(['close', 'save'])

const ui = useUI()
const { t } = useI18n()

const isCreateMode = computed(() => !props.ocapAlarmId)
const dialogTitle = computed(() => (isCreateMode.value ? t('OCAP 추가') : t('OCAP 수정')))
const saveButtonLabel = computed(() => (isCreateMode.value ? t('저장') : t('수정')))

const form = ref()
const formData = ref({
  name: undefined,
  alarmIntervalCodeId: undefined,
  toolId: undefined,
  parameterId: undefined,
  isAlarmControlSpecOver: false,
  isAlarmSpecOver: false,
  recipients: [], // 수신인 및 연락처 목록
})

const users = ref([])
const alarmIntervalCodes = ref([])
const tools = ref([])
const parameters = ref([])
const recipientRows = ref([])
const recipientValidationError = ref('')

const recipientColumns = [
  { name: 'user', label: t('사용자'), align: 'left', field: 'name' },
  { name: 'email', label: t('이메일'), align: 'center', field: 'email' },
  { name: 'sms', label: t('SMS'), align: 'center', field: 'sms' },
  { name: 'kakao', label: t('카카오톡'), align: 'center', field: 'kakao' },
]

const formattingParameterLabel = (parameter) => {
  const { name, lsl, lcl, ucl, usl, target } = parameter
  const specNotSettingLabel = t('미설정')
  return `${name} (lsl: ${lsl ?? specNotSettingLabel}, lcl: ${lcl ?? specNotSettingLabel}, target: ${target ?? specNotSettingLabel} ucl: ${ucl ?? specNotSettingLabel}, usl: ${usl ?? specNotSettingLabel})`
}

const initializeRecipientRows = () => {
  recipientRows.value = users.value.map((user) => ({
    userId: user.id,
    name: user.name,
    selected: false,
    email: {
      selected: false,
      available: !!user.email,
      value: user.email,
    },
    sms: {
      selected: false,
      available: !!user.phone,
      value: user.phone,
    },
    kakao: {
      selected: false,
      available: !!user.phone,
      value: user.phone,
    },
  }))
}

const handleNotificationChange = (row) => {
  // 알림 타입 중 하나라도 선택되어 있으면 유저 선택
  row.selected = row.email.selected || row.sms.selected || row.kakao.selected
}

const prepareRecipientsData = () => {
  const recipients = []

  recipientRows.value.forEach((row) => {
    const notificationTypes = []
    if (row.email.selected) notificationTypes.push('EMAIL')
    if (row.sms.selected) notificationTypes.push('SMS')
    if (row.kakao.selected) notificationTypes.push('KAKAO')

    // 알림 타입이 하나라도 선택된 경우에만 추가
    if (notificationTypes.length > 0) {
      recipients.push({
        userId: row.userId,
        notificationTypes: notificationTypes,
      })
    }
  })
  return recipients
}

const handleSaveButtonClick = async () => {
  const valid = await form.value.validate()
  if (!valid) {
    ui.notify.warning(t('유효하지 않은 입력값이 있습니다.'))
    return false
  }
  try {
    ui.loading.show()

    if (isCreateMode.value) {
      await fetchCreateApi()
    } else {
      await fetchModifyApi()
    }

    emit('save', { success: true })
  } catch (error) {
    emit('save', { success: false, error })
  } finally {
    ui.loading.hide()
  }
}

const fetchCreateApi = async () => {
  await OcapService.createOcap({
    name: formData.value.name,
    alarmIntervalCodeId: formData.value.alarmIntervalCodeId,
    toolId: formData.value.toolId,
    parameterId: formData.value.parameterId,
    isAlarmControlSpecOver: formData.value.isAlarmControlSpecOver,
    isAlarmSpecOver: formData.value.isAlarmSpecOver,
    recipients: prepareRecipientsData(),
  })
}

const fetchModifyApi = async () => {
  await OcapService.modifyOcap(props.ocapAlarmId, {
    name: formData.value.name,
    alarmIntervalCodeId: formData.value.alarmIntervalCodeId,
    toolId: formData.value.toolId,
    parameterId: formData.value.parameterId,
    isAlarmControlSpecOver: formData.value.isAlarmControlSpecOver,
    isAlarmSpecOver: formData.value.isAlarmSpecOver,
    recipients: prepareRecipientsData(),
  })
}

const handleToolChange = async (toolId) => {
  formData.value.parameterId = undefined
  parameters.value = await fetchParametersByToolId(toolId)
}

const fetchParametersByToolId = async (toolId) => {
  return await ParameterService.getParameters({ toolId })
}

const loadInitialData = async () => {
  const [usersData, alarmIntervalCodesData, toolsData] = await Promise.all([
    UserService.getUsers(),
    CodeService.getCodesByCategory(ALARM_INTERVAL_CODE_CATEGORY),
    ToolService.getTools(),
  ])
  users.value = usersData
  alarmIntervalCodes.value = alarmIntervalCodesData
  tools.value = toolsData
  initializeRecipientRows()
}

const initModifyFormData = async () => {
  const ocapAlarm = await OcapService.getOcapById(props.ocapAlarmId)
  formData.value = {
    name: ocapAlarm.name,
    alarmIntervalCodeId: ocapAlarm.alarmIntervalCodeId,
    toolId: ocapAlarm.toolId,
    parameterId: ocapAlarm.parameterId,
    isAlarmControlSpecOver: ocapAlarm.isAlarmControlSpecOver,
    isAlarmSpecOver: ocapAlarm.isAlarmSpecOver,
  }

  // 기존 수신자 정보를 테이블 행 데이터로 변환
  if (ocapAlarm.recipients && ocapAlarm.recipients.length > 0) {
    // 사용자별로 알림 타입을 수집
    const userNotificationMap = new Map()

    ocapAlarm.recipients.forEach((recipient) => {
      const userId = recipient.userId
      if (!userNotificationMap.has(userId)) {
        userNotificationMap.set(userId, [])
      }
      // notificationType 필드에서 알림 타입 추출
      userNotificationMap.get(userId).push(recipient.notificationType)
    })

    // recipientRows 업데이트
    recipientRows.value.forEach((row) => {
      if (userNotificationMap.has(row.userId)) {
        row.selected = true
        const notificationTypes = userNotificationMap.get(row.userId)
        row.email.selected = notificationTypes.includes('EMAIL')
        row.sms.selected = notificationTypes.includes('SMS')
        row.kakao.selected = notificationTypes.includes('KAKAO')
      }
    })
  }

  parameters.value = await fetchParametersByToolId(ocapAlarm.toolId)
}

onBeforeMount(async () => {
  await loadInitialData()
  if (!isCreateMode.value) {
    await initModifyFormData()
  }
})
</script>

<style scoped lang="scss">
.dialog-container {
  width: 700px;
  max-width: 700px;
}
</style>
