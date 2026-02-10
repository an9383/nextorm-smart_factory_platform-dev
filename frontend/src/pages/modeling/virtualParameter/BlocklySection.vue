<template>
  <q-card class="column full-height blocklySectionWrap">
    <q-card-section class="col">
      <q-table
        flat
        :title="$t('파라미터')"
        :rows="parameters"
        style="height: 300px"
        :columns="parameterTableColumns"
        row-key="name"
        :hide-bottom="true"
        :pagination="{
          rowsPerPage: 0,
        }"
      >
        <template #top-right>
          <q-btn
            no-caps
            color="primary"
            :label="$t('파라미터 선택')"
            class="sBtn"
            @click="handleParameterSelectButtonClick"
          />
        </template>
      </q-table>
    </q-card-section>
    <q-separator />
    <q-card-section class="col bg-grey-8 q-pa-none flex justify-center items-center">
      <template v-if="isEmptySelectParameters">
        <h5 class="text-white">
          <q-icon name="warning" color="orange" class="q-mr-sm" />
          {{ $t('파라미터를 먼저 선택 해 주세요.') }}
        </h5>
      </template>
      <template v-else>
        <Blockly
          ref="blocklyRef"
          :workspace-json-string="workspaceJsonString"
          :parameters="parameters"
          :ai-model-list="aiModelList"
          :selected-tool-id="props.selectedToolId"
        />
      </template>
    </q-card-section>
  </q-card>

  <ParameterSelectDialog
    v-if="isParameterSelectDialogShow"
    :listing-exclude-ids="parameterSelectDialogListingExcludeIds"
    :tool-id="selectedToolId"
    :selected-parameter-ids="selectedParameterIds"
    :required-include-parameter-ids="blocklyUsingParameterIds"
    @close="handleParameterSelectDialogClose"
    @ok="handleParameterSelectDialogOk"
  />

  <!-- Blockly 결과 확인을 위한 임시 다이얼로그-->
  <q-dialog v-model="blocklyResult.open" full-height full-width>
    <q-card class="flex full-width">
      <q-card-section class="full-width" horizontal>
        <q-card-section style="width: 50%">
          <pre>{{ blocklyResult.script }}</pre>
        </q-card-section>
        <q-separator vertical />
        <q-card-section style="flex: 1">
          <pre>{{ JSON.stringify(blocklyResult.workspaceJson, null, 2) }}</pre>
        </q-card-section>
      </q-card-section>
    </q-card>
  </q-dialog>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import ParameterSelectDialog from 'pages/modeling/virtualParameter/ParameterSelectDialog.vue'
import Blockly from 'components/blockly/Blockly.vue'
import ParameterService from 'src/services/modeling/ParameterService'
import useUI from 'src/common/module/ui'
import { t } from '/src/plugins/i18n'
import { parameterTableColumnsDefinition } from 'pages/modeling/virtualParameter/virtualParameter'
import AiService from 'src/services/ai/AiService'

const { notify } = useUI()

const props = defineProps({
  selectedToolId: {
    type: Number,
    required: false,
  },
  initSelectedParameterIds: {
    type: Array,
    required: false,
    default: () => [],
  },
  loadWorkspaceJson: {
    type: String,
    required: false,
  },
  currentModifyParameterId: {
    type: Number,
    required: false,
  },
})

const parameterTableColumns = parameterTableColumnsDefinition

const blocklyRef = ref(null)
const parameters = ref([])
const isParameterSelectDialogShow = ref(false)
const blocklyUsingParameterIds = ref([])
const parameterSelectDialogListingExcludeIds = ref([])

const workspaceJsonString = ref(props.loadWorkspaceJson)
const tempBlocklyWorkspace = ref('')

const isEmptySelectParameters = computed(() => parameters.value.length === 0)
const selectedParameterIds = computed(() => parameters.value.map((p) => p.id))

// Blockly 디버깅용
const blocklyResult = ref({ open: false })

//ai모델
const aiModelList = ref([])

watch(
  () => props.selectedToolId,
  async (newSelectedToolId) => {
    if (!newSelectedToolId) return

    parameters.value = []
    blocklyUsingParameterIds.value = []
    aiModelList.value = [] // 초기화

    try {
      const response = await AiService.getModelByToolId(newSelectedToolId)

      if (response && response.length > 0) {
        aiModelList.value = response
      }
    } catch (error) {
      console.error('Error loading AI models:', error)
    }
  },
  { immediate: true },
)

const closeParameterSelectDialog = () => (isParameterSelectDialogShow.value = false)
const handleParameterSelectDialogClose = closeParameterSelectDialog

const handleParameterSelectButtonClick = () => {
  if (!props.selectedToolId) {
    notify.warning(t('툴을 선택해주세요'))
    return
  }

  if (blocklyRef.value) {
    blocklyUsingParameterIds.value = blocklyRef.value.getUsingParameterIds()
  }

  if (blocklyRef.value) {
    const { workspaceJson } = blocklyRef.value.getWorkspaceData()
    tempBlocklyWorkspace.value = JSON.stringify(workspaceJson)
  }

  isParameterSelectDialogShow.value = true
}

const handleParameterSelectDialogOk = (selectedParameters) => {
  parameters.value = selectedParameters
  workspaceJsonString.value = tempBlocklyWorkspace.value
  tempBlocklyWorkspace.value = null
  closeParameterSelectDialog()
}

onMounted(async () => {
  const validInitSelectedParameterIds = props.initSelectedParameterIds && props.initSelectedParameterIds.length > 0
  if (validInitSelectedParameterIds) {
    parameters.value = await ParameterService.getParameters({ id: props.initSelectedParameterIds })
  }

  if (props.currentModifyParameterId) {
    parameterSelectDialogListingExcludeIds.value = [props.currentModifyParameterId]
  }
  // 여기에 추가
  if (props.selectedToolId) {
    const response = await AiService.getModelByToolId(props.selectedToolId)
    if (response.length > 0) {
      aiModelList.value = response
    }
  }
})

defineExpose({
  getSectionData: () => {
    if (!blocklyRef.value) {
      return {
        parameterIds: [],
        script: null,
        workspaceJson: null,
      }
    }

    const { script, workspaceJson } = blocklyRef.value.getWorkspaceData()
    const usingParameterIds = blocklyRef.value.getUsingParameterIds()
    const mappingParameters = parameters.value
      .map((parameter) => parameter.id)
      .map((parameterId) => {
        return {
          id: parameterId,
          isUsingCalculation: usingParameterIds.includes(parameterId),
        }
      })

    return {
      mappingParameters,
      script,
      workspaceJson,
    }
  },
  showBlocklyDebugDialog: () => {
    if (!blocklyRef.value) {
      notify.warning(t('Blockly 데이터가 없습니다'))
      return
    }

    const { script, workspaceJson } = blocklyRef.value.getWorkspaceData()

    if (!script || !workspaceJson) {
      notify.warning(t('Blockly 데이터가 없습니다'))
      return
    }

    blocklyResult.value = {
      script,
      workspaceJson,
      workspaceJsonPretty: JSON.stringify(workspaceJson, null, 2),
      open: true,
    }
  },
  validateAiModelParameters: () => {
    return blocklyRef.value.validateAiModelParameters()
  },
})
</script>

<style scoped lang="scss">
.blocklySectionWrap {
  .q-card__section:deep {
    padding: 0;

    .q-table__top {
      border-bottom: 1px solid #ddd;

      button.sBtn {
        height: 30px;
        min-height: 30px;
        min-width: 76px;
        padding: 0 12px;
        font-size: 14px;
        background-color: var(--mainBgColor) !important;
        border: 1px solid var(--mainColor) !important;
        border-radius: 4px;
        color: var(--mainColor) !important;

        &::before {
          box-shadow: none;
        }
      }
    }

    .q-table__middle {
      padding: 12px 16px;
    }
  }
}
</style>
