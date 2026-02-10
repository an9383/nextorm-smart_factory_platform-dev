<template>
  <q-page class="q-pa-sm">
    <q-card class="cust_subPage">
      <q-card-section class="row search_section_wrap">
        <!-- <div class="text-h5 text-weight-bold q-mt-sm">{{ $t('Summary 설정') }}</div> -->
        <q-item class="search_left"></q-item>
        <q-item class="col-4 search_right">
          <q-item-section>
            <q-btn
              v-permission:summaryConfig.create
              @click="handleCreateButtonClick"
              class="add_btn with_icon_btn sBtn secondary"
              >{{ $t('추가') }}</q-btn
            >
            <q-btn
              v-permission:summaryConfig.update
              :disabled="isNotSelected"
              @click="handleModifyButtonClick"
              class="edit_btn with_icon_btn sBtn secondary"
              >{{ $t('수정') }}</q-btn
            >
            <q-btn
              v-permission:summaryConfig.delete
              :disabled="isNotSelected"
              @click="handleDeleteButtonClick"
              class="delete_btn with_icon_btn sBtn secondary"
              >{{ $t('삭제') }}</q-btn
            >
          </q-item-section>
          <summary-config-modify
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
import SummaryConfigService from 'src/services/modeling/SummaryConfigService.js'
import useUI from 'src/common/module/ui'
import { t } from 'src/plugins/i18n'
import ConfigTable from 'components/modeling/configTable/ConfigTable.vue'
import SummaryConfigModify from 'pages/modeling/summaryConfig/SummaryConfigModify.vue'

const ui = useUI()

const isVisibleDialog = ref(false)
const selectedConfigId = ref(null)

const configs = ref([])
const selected = ref([])

const isNotSelected = computed(() => selected.value?.length === 0)

const getSummaryConfigs = async () => {
  configs.value = await SummaryConfigService.getSummaryConfigs()
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
  ui.confirm(t('삭제'), t('선택된 항목을 삭제 하시겠습니까?'), t('삭제'), t('취소')).onOk(deleteSummaryConfig)
}

const closeDialog = () => {
  if (selectedConfigId.value) {
    selectedConfigId.value = null
  }
  isVisibleDialog.value = false
}

const handleDialogOk = async () => {
  await getSummaryConfigs()
  closeDialog()
}

const handleDialogClose = async () => {
  closeDialog()
}

const deleteSummaryConfig = async () => {
  await SummaryConfigService.deleteSummaryConfig(selected.value[0].id)
  ui.notify.success(t('삭제 되었습니다.'))
  await getSummaryConfigs()
}

onMounted(async () => {
  await getSummaryConfigs()
})
</script>
