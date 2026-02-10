<template>
  <q-dialog v-model="show" persistent>
    <q-card class="dialog-container">
      <q-card-section class="bg-secondary text-white">
        <div class="text-h6">{{ $t('DCP') }} {{ $t(status) }}</div>
      </q-card-section>
      <q-separator />
      <div class="row">
        <div class="col-4">
          <q-card-section>
            <q-form ref="form">
              <div class="row">
                <filterable-select
                  :disable="isModifyMode"
                  v-model="modify.toolName"
                  :label="$t('설비')"
                  :options="toolList"
                  option-value="id"
                  option-label="name"
                  :rules="[$rules.required]"
                  class="input-required col-6 q-pr-sm"
                  @update:model-value="loadParametersByTool"
                />
              </div>
              <div class="row">
                <filterable-select
                  v-model="modify.command"
                  :label="$t('Command')"
                  :options="commandOptions"
                  :rules="[$rules.required]"
                  class="input-required col q-pr-sm"
                />
                <q-input
                  v-model="modify.dataInterval"
                  :label="$t('데이터 수집 주기(초)')"
                  :rules="[$rules.natural, $rules.required]"
                  class="input-required col q-pl-sm"
                />
              </div>
              <div class="row">
                <filterable-select
                  v-model="modify.collectorType"
                  :label="$t('수집 유형')"
                  :options="collectorTypes"
                  option-value="type"
                  option-label="displayName"
                  map-options
                  emit-value
                  :rules="[$rules.required]"
                  class="input-required col"
                  @update:model-value="handleCollectorTypeChange"
                />
              </div>
              <div v-for="item in collectorArguments" :key="item.key" class="row">
                <!-- TODO:: 타입 제약조건 걸어야함 -->
                <q-input
                  v-if="item.type === 'STRING'"
                  v-model="collectorArgumentsValue[item.key]"
                  :label="$t(item.key)"
                  :class="`col ${item.required && 'input-required'}`"
                  :rules="item.required && [$rules.required]"
                />
                <q-btn
                  v-else-if="item.type === EXTRA_DATA_MAPPING_ARGUMENTS_TYPE_KEY"
                  :label="$t(item.key)"
                  class="col"
                  @click="() => handleParameterMappingButtonClick(item.extraDataDefines)"
                />
              </div>
              <div>
                <q-toggle v-model="modify.isGeoDataType" :label="$t('Geo 데이터 타입 사용 여부')" />
                <div class="toggle_input_wrap">
                  <q-input
                    v-if="modify.isGeoDataType"
                    v-model="modify.latitudeParameterName"
                    :label="$t('위도 파라미터명')"
                    :rules="[$rules.required]"
                    class="input-required col"
                  />
                  <q-input
                    v-if="modify.isGeoDataType"
                    v-model="modify.longitudeParameterName"
                    :label="$t('경도 파라미터명')"
                    :rules="[$rules.required]"
                    class="input-required col"
                  />
                </div>
              </div>
            </q-form>
          </q-card-section>
        </div>
        <div class="col-8 q-pa-md">
          <q-tabs
            v-model="tab"
            inline-label
            :breakpoint="0"
            align="justify"
            class="bg-grey-8 text-white"
            narrow-indicator
          >
            <q-tab name="parameter" :label="$t('파라미터')" />
            <q-separator vertical color="white" />
            <q-tab name="rule" :label="$t('Rule')" />
          </q-tabs>
          <q-tab-panels v-model="tab" animated>
            <q-tab-panel name="parameter" class="q-pa-none">
              <q-table
                flat
                bordered
                :rows="parameterRows"
                :columns="parameterColumns"
                :filter="searchText"
                :filter-method="parameterFilterMethod"
                row-key="id"
                color="primary"
                selection="multiple"
                v-model:selected="selectedParameters"
                virtual-scroll
                :virtual-scroll-item-size="100"
                :rows-per-page-options="[0]"
              >
                <template v-slot:top-left>
                  <q-input
                    v-model="searchText"
                    :placeholder="$t('파라미터명')"
                    dense
                    style="width: 280px"
                    outlined
                    clearable
                  >
                    <template v-slot:append>
                      <q-icon name="search" />
                    </template>
                  </q-input>
                </template>
              </q-table>
            </q-tab-panel>
            <q-tab-panel name="rule" class="q-pa-none">
              <q-table
                flat
                bordered
                :rows="ruleRows"
                :columns="ruleColumns"
                row-key="id"
                color="primary"
                selection="multiple"
                v-model:selected="selectedRules"
                virtual-scroll
                :virtual-scroll-item-size="100"
                :rows-per-page-options="[0]"
                @selection="updateSelectedRules"
              >
              </q-table>
            </q-tab-panel>
          </q-tab-panels>
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

  <ExtraDataDialog
    v-if="extraDataDialogVisible"
    v-model="extraDataMap"
    :parameters="extraDataDialogProps.parameters"
    :extra-data-defines="extraDataDialogProps.extraDataDefines"
    @close="handleExtraDataDialogClose"
  />
</template>
<script setup>
import DcpConfigService from 'src/services/modeling/DcpConfigService'
import ParameterService from 'src/services/modeling/ParameterService'
import { computed, defineEmits, defineExpose, ref } from 'vue'
import _ from 'lodash'
import RuleService from 'src/services/modeling/RuleService'
import useUI from 'src/common/module/ui'
import { pt, t } from 'src/plugins/i18n'
import MetaDataService from 'src/services/modeling/MetaDataService'
import ToolService from 'src/services/modeling/ToolService'
import ExtraDataDialog from 'pages/modeling/dcpConfig/ExtraDataDialog.vue'

const EXTRA_DATA_MAPPING_ARGUMENTS_TYPE_KEY = 'PARAMETER_MAPPING'
const PARAMETER_TYPE_META_DATA = 'META_DATA'

// extraData 테스트 영역
const extraDataDialogVisible = ref(false)
const extraDataDialogProps = ref({
  parameters: [],
  extraDataDefines: [],
})
// key: parameterId, value: extraData
const extraDataMap = ref({})

const isNotVirtualParameter = (parameter) => !parameter.isVirtual
const isNotMetaDataParameter = (parameter) => parameter.type !== PARAMETER_TYPE_META_DATA

// 상태 초기화 및 기본 modify 생성 유틸
const resetStateForCreate = () => {
  selectedDcpConfigID.value = undefined
  collectorArguments.value = []
  collectorArgumentsValue.value = {}
  selectedParameters.value = []
  selectedRules.value = []
  selectedChangeRules.value = []
  parameterRows.value = []
  ruleRows.value = []
  searchText.value = ''
  extraDataDialogVisible.value = false
  resetExtraDataMap()
  form.value?.resetValidation && form.value.resetValidation()
  tab.value = 'parameter'
}

const makeDefaultModify = (tool) => ({
  toolId: tool?.id ?? null,
  toolName: tool ?? null,
  bootstrapServer: '',
  collectorType: '',
  collectApiUrl: '',
  topic: '',
  command: null,
  dataInterval: null,
  isGeoDataType: false,
  latitudeParameterName: '',
  longitudeParameterName: '',
  parameters: [],
  rules: [],
})

const fetchAndContainExtraData = async (parameterIds) => {
  const fetchExtraData = await ParameterService.getExtraDataByIds(parameterIds)
  fetchExtraData.forEach((it) => {
    const { parameterId, extraData } = it
    extraDataMap.value[parameterId] = extraData
  })
}

const handleParameterMappingButtonClick = async (extraDataDefines) => {
  if (selectedParameters.value.length === 0) {
    ui.notify.warning('선택된 파라미터가 없습니다.')
    return
  }

  const collectParameters = selectedParameters.value.filter(
    (p) => isNotVirtualParameter(p) && isNotMetaDataParameter(p),
  )

  if (collectParameters.length === 0) {
    ui.notify.warning('일반 파라미터 정보가 필요합니다.')
    return
  }

  const notIncludedParameterIds = collectParameters
    .map((parameter) => parameter.id)
    .filter((parameterId) => !extraDataMap.value[parameterId])

  if (notIncludedParameterIds.length > 0) {
    await fetchAndContainExtraData(notIncludedParameterIds)
  }

  extraDataDialogProps.value = {
    parameters: collectParameters,
    extraDataDefines,
  }
  extraDataDialogVisible.value = true
}

const handleExtraDataDialogClose = () => {
  extraDataDialogVisible.value = false
}

const resetExtraDataMap = () => {
  extraDataMap.value = {}
}

// extraData 테스트 영역 end

const ui = useUI()

const emit = defineEmits(['close'])
const show = ref(false)

const parameterColumns = ref([
  { name: 'id', align: 'left', label: t('Id'), field: 'id' },
  {
    align: 'left',
    label: t('설비명'),
    field: 'toolName',
  },
  { name: 'name', align: 'left', label: t('파라미터명'), field: 'name', sortable: true, format: (val) => pt(val) },
  { name: 'isVirtual', align: 'left', label: t('가상 파라미터 여부'), field: 'isVirtual', sortable: true },
  { name: 'type', align: 'left', label: t('타입'), field: 'type' },
  { name: 'dataType', align: 'left', label: t('데이터 타입'), field: 'dataType' },
  { name: 'order', align: 'left', label: t('순서'), field: 'order' },
])

const ruleColumns = ref([
  { name: 'id', align: 'left', label: t('Id'), field: 'id' },
  { name: 'name', align: 'left', label: t('규칙이름'), field: 'name', sortable: true },
  { name: 'className', align: 'left', label: t('Class Name'), field: 'className' },
  {
    name: 'description',
    align: 'left',
    label: t('설명'),
    field: 'description',
  },
])

const tab = ref('parameter')
const selectedParameters = ref([])
const selectedRules = ref([])
const selectedChangeRules = ref([])
const parameterRows = ref([])
const ruleRows = ref([])
const modify = ref({})
const toolList = ref()
const commandOptions = ref(['TRACE', 'EVENT'])

const collectorTypes = ref([])
const collectorArguments = ref([])
const collectorArgumentsValue = ref({})

const status = ref(t('추가'))
const form = ref(null)
const selectedDcpConfigID = ref()

const isModifyMode = computed(() => status.value !== t('추가'))

const searchText = ref('')

const topic = computed(() => {
  if (_.isObject(modify.value.toolName) && !_.isUndefined(modify.value.toolName)) {
    return modify.value.toolName.name
  } else if (!_.isUndefined(modify.value.toolName)) {
    return modify.value.toolName
  }
  return ''
})

const parameterFilterMethod = (rows, terms) => {
  const q = (terms || '').toString().trim().toLowerCase()
  if (!q) return rows

  return rows.filter((row) => {
    const name = (row.name || '').toString().toLowerCase()
    // pt 변환 실패/빈값 대비
    const displayName = ((pt(row.name || '') || row.name || '') + '').toLowerCase()
    return name.includes(q) || displayName.includes(q)
  })
}

const open = async (data, selectedToolInfo) => {
  const [foundCollectorTypes, foundTools] = await Promise.all([
    MetaDataService.getCollectorTypes(),
    ToolService.getTools(),
  ])

  collectorTypes.value = foundCollectorTypes
  toolList.value = foundTools

  if (data == null) {
    status.value = t('추가')
    // 선택 설비 기준으로 완전한 초기값 구성 (이전 수정 데이터 차단)
    const toolObj = typeof selectedToolInfo?.value === 'object' ? selectedToolInfo.value : selectedToolInfo
    data = makeDefaultModify(toolObj)

    // 생성 모드 초기화: 이전 수정 상태들 제거
    resetStateForCreate()

    RuleService.getRules().then((rules) => {
      ruleRows.value = rules
    })

    modify.value = { ...data }

    // 선택 설비 기준 파라미터 즉시 로드
    if (toolObj?.id) {
      await loadParametersByTool(toolObj.id)
    } else {
      parameterRows.value = []
    }
  } else {
    resetStateForCreate()
    collectorArguments.value = collectorTypes.value.find((it) => it.type === data.collectorType).arguments
    collectorArgumentsValue.value = data.collectorArguments

    selectedDcpConfigID.value = data.id
    status.value = t('수정')
    if (tab.value === 'parameter') {
      parameterRows.value = []
    } else {
      ruleRows.value = []
    }
    modify.value = { ...data }

    await loadParametersByTool(data.toolId)
    RuleService.getRules().then((rules) => {
      ruleRows.value = rules
      let ruleKeys = {}
      modify.value.rules.map((d) => {
        ruleKeys[d.id] = d.id
      })
      selectedRules.value = ruleRows.value.filter((v) => (ruleKeys[v.id] == null ? false : true))
      selectedChangeRules.value = selectedRules.value
    })
  }

  show.value = true
  tab.value = 'parameter'
}

defineExpose({ open })

const validateExtraData = async () => {
  const collectorArguments = collectorTypes.value.find((it) => it.type === modify.value.collectorType).arguments
  const extraDataKeys = collectorArguments
    .find((argument) => argument.type === EXTRA_DATA_MAPPING_ARGUMENTS_TYPE_KEY)
    .extraDataDefines.filter((define) => define.required)
    .map((define) => define.key)

  const collectParameters = selectedParameters.value.filter(
    (p) => isNotVirtualParameter(p) && isNotMetaDataParameter(p),
  )
  const notIncludedParameterIds = collectParameters
    .map((parameter) => parameter.id)
    .filter((parameterId) => !extraDataMap.value[parameterId])

  if (notIncludedParameterIds.length > 0) {
    await fetchAndContainExtraData(notIncludedParameterIds)
  }

  /**
    일반 파라미터에 대해 아래의 내용을 검증한다
    1. extraDataMap에 해당 파라미터의 데이터가 존재하는가
    2. 존재한다면, 컬렉터에서 필요로 하는 추가 데이터 key의 값이 모두 존재하는가
  */
  return collectParameters.every((parameter) => {
    const extraData = extraDataMap.value[parameter.id]
    if (!extraData) {
      return false
    }
    return extraDataKeys.every((key) => {
      const data = extraData[key]
      return !(data === null || data === undefined)
    })
  })
}

const onOk = async () => {
  const success = await form.value.validate()
  if (!success) {
    ui.notify.warning(t('유효하지 않은 입력값이 있습니다.'))
    return
  }

  const collectorArguments = collectorTypes.value.find((it) => it.type === modify.value.collectorType).arguments
  const hasExtraDataArguments = collectorArguments.some(
    (argument) => argument.type === EXTRA_DATA_MAPPING_ARGUMENTS_TYPE_KEY,
  )
  if (hasExtraDataArguments) {
    const isValid = await validateExtraData()
    if (!isValid) {
      ui.notify.warning(t('추가 데이터가 입력되지 않은 파라미터가 존재합니다.'))
      return
    }
  }

  modify.value.topic = topic
  if (status.value == t('추가')) {
    try {
      await DcpConfigService.createDcpConfig({
        ...modify.value,
        toolId: modify.value.toolName.id,
        toolName: modify.value.toolName.name,
        parameterIds: selectedParameters.value.map((parameters) => parameters.id),
        ruleIds: selectedRules.value.map((rules) => rules.id),
        collectorArguments: collectorArgumentsValue.value,
        parameterCollectExtraData: extraDataMap.value,
      })
      ui.notify.success(t('저장 되었습니다.'))
      show.value = false
      emit('close', 'ok')
    } catch (error) {
      ui.notify.error('저장 중 에러가 발생하였습니다')
    }
  } else {
    try {
      let toolId
      if (typeof modify.value.toolName === 'object') {
        toolId = modify.value.toolName.id
      } else {
        toolId = modify.value.toolId
      }
      await DcpConfigService.modifyDcpConfig(
        {
          ...modify.value,
          toolId: toolId,
          parameterIds: selectedParameters.value.map((parameters) => parameters.id),
          ruleIds: selectedRules.value.map((rules) => rules.id),
          collectorArguments: collectorArgumentsValue.value,
          parameterCollectExtraData: extraDataMap.value,
        },
        modify.value.id,
      )
      ui.notify.success(t('저장 되었습니다.'))
      show.value = false
      emit('close', 'ok')
    } catch (error) {
      ui.notify.error('수정 중 에러가 발생하였습니다')
    }
  }
}

const updateSelectedRules = (data) => {
  const newData = data.rows[0]

  if (!data.added) {
    if (selectedChangeRules.value.some((v) => v.id === newData.id)) {
      selectedChangeRules.value = selectedChangeRules.value.map((v) =>
        v.id === newData.id ? { ...v, dcpConfigId: null } : v,
      )
    }
  } else {
    if (selectedChangeRules.value.some((v) => v.id === newData.id)) {
      selectedChangeRules.value = selectedChangeRules.value.map((v) =>
        v.id === newData.id ? { ...v, dcpConfigId: selectedDcpConfigID } : v,
      )
    } else {
      selectedChangeRules.value.push({
        ...newData,
        dcpConfigId: selectedDcpConfigID,
      })
    }
  }
}

const onCancel = () => {
  show.value = false
  emit('close', 'cancel')
}

const loadParametersByTool = async (tool) => {
  let toolId
  if (typeof tool === 'object') {
    toolId = tool.id
  } else {
    toolId = tool
  }
  if (status.value == t('추가')) {
    // parameterRows.value = await ParameterService.getParameters({ toolId: modify.value.toolName.id })
    parameterRows.value = await ParameterService.getParameters({ toolId })
  } else if (status.value == t('수정')) {
    parameterRows.value = await ParameterService.getParameters({ toolId })

    let paramKeys = []
    modify.value.parameters.map((d) => {
      paramKeys[d.id] = d.id
    })
    selectedParameters.value = parameterRows.value.filter((v) =>
      // v.dcpConfigId === selectedDcpConfigID.value
      paramKeys[v.id] == null ? false : true,
    )
  }
  resetExtraDataMap()
}

const handleCollectorTypeChange = (collectorType) => {
  collectorArguments.value = collectorTypes.value.find((it) => it.type === collectorType).arguments
  collectorArgumentsValue.value = {}
  resetExtraDataMap()
}
</script>
<style scoped>
.dialog-container {
  width: 1500px;
  max-width: 1500px;
}

:deep(.q-table__container) {
  height: 452px;
}
</style>
