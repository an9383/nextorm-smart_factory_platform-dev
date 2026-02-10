<template>
  <q-page class="q-pa-sm">
    <q-card class="cust_subPage">
      <q-card-section class="row search_section_wrap">
        <q-item class="search_left"></q-item>
        <q-item class="col-4 search_right">
          <q-item-section>
            <q-btn
              v-permission:ocap.create
              class="add_btn with_icon_btn sBtn secondary"
              @click="handleCreateButtonClick"
              >{{ $t('추가') }}</q-btn
            >
            <q-btn
              v-permission:ocap.update
              class="edit_btn with_icon_btn sBtn secondary"
              :disabled="isRowNotSelected"
              @click="handleModifyButtonClick"
              >{{ $t('수정') }}</q-btn
            >
            <q-btn
              v-permission:ocap.delete
              class="delete_btn with_icon_btn sBtn secondary"
              :disabled="isRowNotSelected"
              @click="handleDeleteButtonClick"
              >{{ $t('삭제') }}</q-btn
            >
          </q-item-section>
        </q-item>
      </q-card-section>
      <simple-table
        bordered
        :rows="tableRows"
        :columns="columns"
        row-key="id"
        color="amber"
        selection="single"
        v-model:selected="selectedTableRows"
      >
        <template v-slot:body="props">
          <q-tr class="cursor-pointer" :props="props" @click="props.selected = !props.selected">
            <q-td v-for="col in props.cols" :key="col.name" :props="props">
              <div v-if="col.name === 'recipients'">
                <div
                  v-for="(recipient, idx) in formatRecipientsWithChips(props.row.recipients)"
                  :key="idx"
                  class="q-mb-xs"
                >
                  <span>{{ recipient.userName }}: </span>
                  <q-chip
                    v-for="type in recipient.types"
                    :key="type.value"
                    :color="type.color"
                    text-color="black"
                    size="sm"
                    dense
                    class="q-ml-xs"
                  >
                    {{ type.label }}
                    <q-tooltip class="bg-black text-white text-caption">
                      {{ type.contact }}
                    </q-tooltip>
                  </q-chip>
                </div>
              </div>
              <template v-else>
                {{ col.value }}
              </template>
            </q-td>
          </q-tr>
        </template>
      </simple-table>
    </q-card>
  </q-page>
  <ocap-form-dialog
    v-if="showFormDialog"
    :ocap-alarm-id="selectedRowId"
    @close="showFormDialog = false"
    @save="handleSaveEvent"
  />
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import OcapFormDialog from 'pages/ocap/settings/OcapFormDialog.vue'
import useUI from 'src/common/module/ui'
import OcapService from 'src/services/ocap/OcapService'
import CodeService from 'src/services/system/CodeService'

const ALARM_INTERVAL_CODE_CATEGORY = 'OCAP_ALARM_INTERVAL'

const { t } = useI18n()
const ui = useUI()

const showFormDialog = ref(false)
const selectedRowId = ref(undefined)

const alarmIntervalCodeMap = ref({})

const tableRows = ref([])
const selectedTableRows = ref([])
const isRowNotSelected = computed(() => selectedTableRows.value.length === 0)

const columns = ref([
  { name: 'id', align: 'left', label: t('Id'), field: 'id' },
  { name: 'name', align: 'left', label: t('이름'), field: 'name' },
  { name: 'toolName', align: 'left', label: t('설비'), field: 'toolName' },
  {
    name: 'parameterName',
    align: 'left',
    label: t('파라미터'),
    field: 'parameterName',
  },
  {
    name: 'alarmInterval',
    align: 'left',
    label: t('알람 주기'),
    field: 'alarmIntervalCodeId',
    format: (codeId) => alarmIntervalCodeMap.value[codeId],
  },
  { name: 'isAlarmControlSpecOver', align: 'left', label: t('컨트롤 스펙오버 알람'), field: 'isAlarmControlSpecOver' },
  { name: 'isAlarmSpecOver', align: 'left', label: t('스펙오버 알람'), field: 'isAlarmSpecOver' },
  {
    name: 'recipients',
    align: 'left',
    label: t('수신자'),
    field: 'recipients',
    format: (recipients) => formatRecipients(recipients),
  },
])

const handleCreateButtonClick = () => {
  selectedRowId.value = undefined
  showFormDialog.value = true
}

const handleModifyButtonClick = () => {
  if (isRowNotSelected.value) {
    ui.notify.error(t('수정할 항목을 선택하세요.'))
    return
  }

  selectedRowId.value = selectedTableRows.value[0].id
  showFormDialog.value = true
}

const handleDeleteButtonClick = async () => {
  if (isRowNotSelected.value) {
    ui.notify.error(t('삭제할 항목을 선택하세요.'))
    return
  }

  const handleOk = async () => {
    try {
      ui.loading.show()
      await OcapService.deleteOcapById(selectedTableRows.value[0].id)
      ui.notify.success(t('삭제 되었습니다.'))
    } catch (error) {
      console.log(error)
      ui.notify.error(t('삭제에 실패했습니다.'))
    } finally {
      ui.loading.hide()
      await fetchRows()
    }
  }

  ui.confirm(t('삭제'), t('선택된 항목을 삭제 하시겠습니까?'), t('삭제'), t('취소')).onOk(handleOk)
}

const handleSaveEvent = async ({ success, error }) => {
  if (success) {
    ui.notify.success(t('저장 되었습니다.'))
    showFormDialog.value = false
    await fetchRows()
    return
  }

  console.log(error)
  ui.notify.error(t('저장에 실패했습니다.'))
}

const getNotificationTypeLabel = (type) => {
  const labels = {
    EMAIL: t('메일'),
    SMS: 'SMS',
    KAKAO: t('카카오톡'),
  }
  return labels[type] || type
}

const getNotificationTypeChipInfo = (type) => {
  const chipInfo = {
    EMAIL: { label: t('메일'), color: 'orange' },
    SMS: { label: 'SMS', color: 'green' },
    KAKAO: { label: t('카카오톡'), color: 'yellow' },
  }
  return chipInfo[type]
}

const formatRecipientsWithChips = (recipients) => {
  if (!recipients || recipients.length === 0) return []

  // userId별로 그룹화하면서 타입별 연락처 정보도 저장
  const userGroups = recipients.reduce((acc, recipient) => {
    if (!acc[recipient.userId]) {
      acc[recipient.userId] = {
        userName: recipient.userName,
        typesWithContact: {},
      }
    }
    // notificationType과 연락처 정보 함께 저장
    if (recipient.notificationType) {
      acc[recipient.userId].typesWithContact[recipient.notificationType] = recipient.contact
    }
    return acc
  }, {})

  // 포맷팅: 각 사용자별로 칩 정보와 연락처 포함
  return Object.values(userGroups).map((user) => {
    const typeChips = Object.entries(user.typesWithContact).map(([type, contact]) => {
      const chipInfo = getNotificationTypeChipInfo(type)
      return {
        value: type,
        label: chipInfo.label,
        color: chipInfo.color,
        contact: contact,
      }
    })
    return {
      userName: user.userName,
      types: typeChips,
    }
  })
}

const formatRecipients = (recipients) => {
  if (!recipients || recipients.length === 0) return ''

  // userId별로 그룹화
  const userGroups = recipients.reduce((acc, recipient) => {
    if (!acc[recipient.userId]) {
      acc[recipient.userId] = {
        userName: recipient.userName,
        types: [],
      }
    }
    // notificationType이 있으면 추가
    if (recipient.notificationType) {
      acc[recipient.userId].types.push(recipient.notificationType)
    }
    return acc
  }, {})

  // 포맷팅: "사용자명: 알림타입들" - 줄바꿈으로 구분
  return Object.values(userGroups)
    .map((user) => {
      const uniqueTypes = [...new Set(user.types)] // 중복 제거
      const typeLabels = uniqueTypes.map(getNotificationTypeLabel)
      return `${user.userName}: ${typeLabels.join(', ')}`
    })
    .join('\n')
}

const fetchRows = async () => {
  tableRows.value = await OcapService.getAllOcap()
}

const init = async () => {
  const codes = await CodeService.getCodesByCategory(ALARM_INTERVAL_CODE_CATEGORY)
  codes.forEach((code) => {
    alarmIntervalCodeMap.value[code.id] = code.name
  })
  await fetchRows()
}

onMounted(() => {
  init()
})
</script>

<style scoped lang="scss"></style>
