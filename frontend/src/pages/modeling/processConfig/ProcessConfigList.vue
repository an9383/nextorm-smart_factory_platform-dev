<template>
  <q-page class="q-pa-sm">
    <q-card class="cust_subPage">
      <q-card-section class="row search_section_wrap">
        <!-- <div class="text-h5 text-weight-bold q-mt-sm">{{ $t('Process 설정') }}</div> -->
        <q-item class="search_left"></q-item>
        <q-item class="col-4 search_right">
          <q-item-section>
            <q-btn
              v-permission:processConfig.create
              @click="handleCreateButtonClick"
              class="add_btn with_icon_btn sBtn secondary"
              >{{ $t('추가') }}</q-btn
            >
            <q-btn
              v-permission:processConfig.update
              :disabled="isNotSelected"
              @click="handleModifyButtonClick"
              class="edit_btn with_icon_btn sBtn secondary"
              >{{ $t('수정') }}</q-btn
            >
            <q-btn
              v-permission:processConfig.delete
              :disabled="isNotSelected"
              @click="handleDeleteButtonClick"
              class="delete_btn with_icon_btn sBtn secondary"
              >{{ $t('삭제') }}</q-btn
            >
          </q-item-section>
          <process-config-modify
            v-if="isVisibleDialog"
            :config-id="selectedConfigId"
            @ok="handleDialogOk"
            @close="handleDialogClose"
          />
        </q-item>
      </q-card-section>
      <config-table :configs="configs" v-model:selected="selected" />
    </q-card>
  </q-page>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import ProcessConfigService from 'src/services/modeling/ProcessConfigService.js'
import useUI from 'src/common/module/ui'
import { t } from 'src/plugins/i18n'
import ProcessConfigModify from 'pages/modeling/processConfig/ProcessConfigModify.vue'
import ConfigTable from 'components/modeling/configTable/ConfigTable.vue'

const ui = useUI()

const isVisibleDialog = ref(false)
const selectedConfigId = ref(null)

const configs = ref([])
const selected = ref([])

const isNotSelected = computed(() => selected.value?.length === 0)

const getProcessConfigs = async () => {
  configs.value = await ProcessConfigService.getProcessConfigs()
  selected.value = []
}

const handleModifyButtonClick = () => {
  const [selectedConfig] = selected.value
  selectedConfigId.value = selectedConfig.id
  isVisibleDialog.value = true
}

const handleCreateButtonClick = () => {
  isVisibleDialog.value = true
}

const handleDeleteButtonClick = () => {
  ui.confirm(t('삭제'), t('선택된 항목을 삭제 하시겠습니까?'), t('삭제'), t('취소')).onOk(deleteProcessConfig)
}

const closeDialog = () => {
  if (selectedConfigId.value) {
    selectedConfigId.value = null
  }
  isVisibleDialog.value = false
}

const handleDialogOk = async () => {
  await getProcessConfigs()
  closeDialog()
}

const handleDialogClose = async () => {
  closeDialog()
}

const deleteProcessConfig = async () => {
  await ProcessConfigService.deleteProcessConfig(selected.value[0].id)
  ui.notify.success(t('삭제 되었습니다.'))
  await getProcessConfigs()
}

onMounted(async () => {
  await getProcessConfigs()
})
</script>
