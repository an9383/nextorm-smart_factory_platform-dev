<template>
  <div ref="blocklyDiv" id="blocklyDiv" />
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as Blockly from 'blockly'
import { javascriptGenerator } from 'blockly/javascript'
import {
  AI_MODEL_BLOCK_FIELD,
  AI_MODEL_PREDICTION_BLOCK,
  apcBlocks,
  customBlocks,
  helpUrl,
  NONE_SELECT_KEY,
  NONE_SELECT_VALUE,
} from 'components/blockly/customBlocks'
import { apcToolbox, sfpToolbox, toolboxConfig } from 'components/blockly/toolbox'
import _ from 'lodash'
import useUI from 'src/common/module/ui'
import { t } from '/src/plugins/i18n'

const props = defineProps({
  parameters: {
    type: Array,
    required: false,
    default: () => {},
  },
  workspaceJsonString: {
    type: String,
  },
  isApc: {
    type: Boolean,
    required: false,
    default: () => false,
  },
  selectedToolId: {
    type: Number,
    required: false,
  },
  aiModelList: {
    type: Array,
    required: false,
    default: () => [],
  },
})

let workspace = ref(null)
let blocklyDiv = ref(null)
const { notify } = useUI()

watch(
  () => [props.parameters, props.workspaceJsonString],
  () => {
    clearBlockly()
    initBlockly()
    if (props.workspaceJsonString) {
      loadBlocklyWorkspace(props.workspaceJsonString)
    }
  },
)

const getRequiredAiModelParameterIds = (aiModelBlock) => {
  return Array.from(
    new Set(
      aiModelBlock
        .map((block) => block.getFieldValue(AI_MODEL_BLOCK_FIELD))
        .filter((aiModelId) => aiModelId !== NONE_SELECT_VALUE)
        .map((aiModelId) => props.aiModelList.find((aiModel) => aiModel.id.toString() === aiModelId))
        .flatMap((foundModel) => foundModel.parameterDetails.map((parameterDetail) => parameterDetail.id)),
    ),
  )
}

const loadBlocklyWorkspace = (workspaceJson) => {
  Blockly.Events.disable()
  // 반응형 workspace에 로드하면 에러가 발생하는 문제가 있음
  // 그래서 Blockly.getMainWorkspace()를 사용하여 로드하도록 해야함
  Blockly.serialization.workspaces.load(JSON.parse(workspaceJson), Blockly.getMainWorkspace())
  Blockly.Events.enable()
}

const registerCustomBlocks = () => {
  // 드롭다운 블록 생성
  const parameterDropDown = () =>
    new Blockly.FieldDropdown(() => {
      return props.parameters.map((param) => [param.name, param.id.toString()])
    })

  // 스펙 종류 드롭다운 블록 생성
  const specTypeDropDown = () =>
    new Blockly.FieldDropdown([
      ['LSL', 'LSL'],
      ['LCL', 'LCL'],
      ['USL', 'USL'],
      ['UCL', 'UCL'],
    ])

  const aiModelDropDown = () =>
    new Blockly.FieldDropdown(
      () => {
        return [
          [NONE_SELECT_KEY, NONE_SELECT_VALUE],
          ...props.aiModelList.map((model) => [model.name, model.id.toString()]),
        ]
      },
      (aiModelId) => {
        // '선택해주세요' 아이템은 검증을 하지 않도록 처리한다.
        // 최종적으로 저장을 하는 시점에 이 블록이 남아있는지는 그떄 검증한다
        if (aiModelId === NONE_SELECT_VALUE) {
          return aiModelId
        }

        const validResult = validateRequiredParameters(aiModelId)
        if (!validResult.isValid) {
          const message = `선택된 파라미터 목록에 모델에 필요한 파라미터가 없습니다.)<br>
          <strong>모델명</strong>: ${validResult.modelName}<br>
          <strong>필요한 파라미터</strong>: ${validResult.missingParamNames.join(', ')}
          `
          notify.warning(message, 1000, { html: true })
          return null
        }
        return aiModelId
      },
    )

  const dropdowns = {
    parameterDropDown,
    specTypeDropDown,
    aiModelDropDown,
  }

  const properties = {
    aiModelCount: props.aiModelList.length,
  }

  customBlocks.forEach((customBlock) => {
    Blockly.Blocks[customBlock.name] = customBlock.define(dropdowns, properties)
    Object.assign(javascriptGenerator.forBlock, {
      [customBlock.name]: customBlock.generateFunction(javascriptGenerator),
    })
  })
}

const registerApcBlocks = () => {
  apcBlocks.forEach((customBlock) => {
    Blockly.Blocks[customBlock.name] = customBlock.define()
    Object.assign(javascriptGenerator.forBlock, {
      [customBlock.name]: customBlock.generateFunction(javascriptGenerator),
    })
  })
}

const moveBlocklyDomToDialog = () => {
  // 원하는 위치의 부모 요소 선택
  const newParent = blocklyDiv.value

  // Blockly가 생성한 DOM 요소 선택
  const widgetDiv = document.querySelector('.blocklyWidgetDiv')
  const tooltipDiv = document.querySelector('.blocklyTooltipDiv')
  const computeCanvas = document.querySelector('.blocklyComputeCanvas')

  // DOM 요소를 새로운 부모 요소로 이동
  newParent.appendChild(widgetDiv)
  newParent.appendChild(tooltipDiv)
  if (computeCanvas) {
    newParent.appendChild(computeCanvas)
  }
}

const clearBlockly = () => {
  if (workspace.value) {
    workspace.value.clear()
    workspace.value.dispose()
    workspace.value = null

    blocklyDiv.value.innerHTML = ''

    // Blockly가 추가한 DOM 요소들을 수동으로 제거합니다.
    const widgetDiv = document.querySelector('.blocklyWidgetDiv')
    const dropDownDiv = document.querySelector('.blocklyDropDownDiv')
    const tooltipDiv = document.querySelector('.blocklyTooltipDiv')

    const widgets = [widgetDiv, dropDownDiv, tooltipDiv]
    widgets.forEach((el) => {
      if (el) {
        el.remove()
      }
    })
  }
}

const initBlockly = () => {
  const toolbox = _.cloneDeep(toolboxConfig)
  if (props.isApc) {
    toolbox.contents = [...toolbox.contents, apcToolbox]
    registerApcBlocks()
    injectBlockly(toolbox)
    blocklyDiv.value.querySelector('.blocklyToolboxDiv').style.display = 'none'
    blocklyDiv.value.querySelector('.blocklyTrash').style.display = 'none'
  } else {
    toolbox.contents = [...toolbox.contents, sfpToolbox]
    registerCustomBlocks()
    injectBlockly(toolbox)
  }

  workspace.value.scrollbar.set(0, 0)
  moveBlocklyDomToDialog()
}

const injectBlockly = (toolbox) => {
  workspace.value = Blockly.inject(blocklyDiv.value, {
    toolbox,
  })
}

const validateRequiredParameters = (aiModelId) => {
  const foundModel = props.aiModelList.find((aiModel) => aiModel.id.toString() === aiModelId)

  const parameterIdSet = new Set(props.parameters.map((parameter) => parameter.id))

  // 필요한 파라미터 중 선택되지 않은 것 찾기
  const missingParameters = foundModel.parameterDetails.filter(
    (requiredParameter) => !parameterIdSet.has(requiredParameter.id),
  )
  return {
    isValid: missingParameters.length === 0,
    modelName: foundModel.name,
    missingParamNames: missingParameters.map((parameter) => parameter.name),
  }
}

const validateAiModelParameters = () => {
  const usedAiModelBlocks = workspace.value.getAllBlocks().filter((block) => block.type === AI_MODEL_PREDICTION_BLOCK)

  // AI 모델 블록이 없으면 검증 필요 없음
  if (usedAiModelBlocks.length === 0) {
    return {
      isValid: true,
    }
  }

  const existNoneBlock = usedAiModelBlocks.some(
    (block) => block.getFieldValue(AI_MODEL_BLOCK_FIELD) === NONE_SELECT_VALUE,
  )
  if (existNoneBlock) {
    return {
      isValid: false,
      message: t('AI 모델이 선택되지 않은 블록이 있습니다.'),
    }
  }

  const invalidResults = usedAiModelBlocks
    .map((block) => validateRequiredParameters(block.getFieldValue(AI_MODEL_BLOCK_FIELD)))
    .filter((validResult) => !validResult.isValid)

  if (invalidResults.length === 0) {
    return {
      isValid: true,
    }
  }

  return {
    isValid: false,
    message: `
      선택된 파라미터 목록에 모델에 필요한 파라미터가 없습니다:\n
      ${invalidResults.map((item) => `${item.modelName} 모델: ${item.missingParamNames.join(', ')}`).join('\n')}
    `,
  }
}

onMounted(async () => {
  initBlockly()
  if (props.workspaceJsonString) {
    loadBlocklyWorkspace(props.workspaceJsonString)
  }
})

onBeforeUnmount(() => {
  clearBlockly()
})

defineExpose({
  getWorkspaceData: () => {
    if (!workspace.value) {
      return {
        script: null,
        workspaceJson: null,
      }
    }

    const script = javascriptGenerator.workspaceToCode(workspace.value)
    const workspaceJson = Blockly.serialization.workspaces.save(workspace.value)
    return {
      script,
      workspaceJson,
    }
  },

  getUsingParameterIds: () => {
    if (!workspace.value) {
      return []
    }

    const isDropDownField = (field) => field instanceof Blockly.FieldDropdown
    const isCustomBlock = (block) => block.helpUrl === helpUrl
    //AiModel 블록의 경우 모델ID를 파라미터 ID에 담지 않도록 하는 검증용
    const isAiModelBlock = (block) => block.type === AI_MODEL_PREDICTION_BLOCK

    const allBlocks = workspace.value.getAllBlocks()
    const parameterIds = allBlocks
      .filter((block) => isCustomBlock(block) && !isAiModelBlock(block))
      .flatMap((block) => block.inputList)
      .flatMap((inputList) => inputList.fieldRow)
      .filter(isDropDownField)
      .map((field) => parseInt(field.getValue(), 10))

    const aiBlockModels = allBlocks.filter((block) => block.type === AI_MODEL_PREDICTION_BLOCK)
    //AI Model의 필요한 파라미터를 추가
    if (aiBlockModels.length > 0) {
      const aiModelParamIds = getRequiredAiModelParameterIds(aiBlockModels)
      parameterIds.push(...aiModelParamIds)
    }
    return [...new Set(parameterIds)]
  },
  activeEditMode: () => {
    blocklyDiv.value.querySelector('.blocklyToolboxDiv').style.display = 'block'
    blocklyDiv.value.querySelector('.blocklyTrash').style.display = 'block'
  },

  validateAiModelParameters,
})
</script>

<style scoped>
#blocklyDiv {
  height: 100%;
  width: 100%;
  margin: 0 auto;
  position: absolute;
}
</style>
