import Blockly from 'blockly'

const AI_MODEL_PREDICTION_BLOCK = 'getAiModelPrediction'
const AI_MODEL_BLOCK_FIELD = 'aiModel'
const NONE_SELECT_KEY = '선택해주세요'
const NONE_SELECT_VALUE = 'none'

const helpUrl = 'https://www.nextorm.com'
const customBlocks = [
  {
    name: AI_MODEL_PREDICTION_BLOCK,
    define(dropdowns, properties) {
      const { aiModelDropDown } = dropdowns
      const { aiModelCount } = properties
      return {
        init: function () {
          this.appendDummyInput()
            .appendField('Double getAiModelPrediction(AiModel: ')
            .appendField(aiModelDropDown(), AI_MODEL_BLOCK_FIELD)
            .appendField(')')
          this.setOutput(true, 'Double')
          this.setColour(230)
          this.setTooltip('')
          this.setHelpUrl(helpUrl)
          this.setEnabled(aiModelCount !== 0)
        },
      }
    },
    generateFunction: (javascriptGenerator) => (block) => {
      const aiModel = block.getFieldValue(AI_MODEL_BLOCK_FIELD)
      const code = `getAiModelPrediction('${aiModel}')`
      return [code, javascriptGenerator.ORDER_FUNCTION_CALL]
    },
  },
  {
    name: 'getValueByParameter',
    define: (dropdowns) => {
      const { parameterDropDown } = dropdowns
      return {
        init: function () {
          this.appendDummyInput()
            .appendField('Double getValueByParameter(parameter: ')
            .appendField(parameterDropDown(), 'parameter')
            .appendField(')')
          this.setOutput(true, 'Number')
          this.setColour(230)
          this.setTooltip('')
          this.setHelpUrl(helpUrl)
        },
      }
    },
    generateFunction: (javascriptGenerator) => (block) => {
      const param = block.getFieldValue('parameter')
      const code = `getValueByParameter('${param}')`

      return [code, javascriptGenerator.ORDER_FUNCTION_CALL] // 정의해둔 함수는 ORDER_FUNCTION_CALL을 반환
    },
  },
  {
    name: 'getObjectValueByParameter',
    define: (dropdowns) => {
      const { parameterDropDown } = dropdowns
      return {
        init: function () {
          this.appendDummyInput()
            .appendField('Object getObjectValueByParameter(parameter: ')
            .appendField(parameterDropDown(), 'parameter')
            .appendField(')')
          this.setOutput(true, null)
          this.setColour(230)
          this.setTooltip('')
          this.setHelpUrl(helpUrl)
        },
      }
    },
    generateFunction: (javascriptGenerator) => (block) => {
      const param = block.getFieldValue('parameter')
      const code = `getObjectValueByParameter('${param}')`

      return [code, javascriptGenerator.ORDER_FUNCTION_CALL] // 정의해둔 함수는 ORDER_FUNCTION_CALL을 반환
    },
  },
  {
    name: 'getSpecValueByParameter',
    define: (dropdowns) => {
      const { parameterDropDown, specTypeDropDown } = dropdowns
      return {
        init: function () {
          this.appendDummyInput()
            .appendField('Double getSpecValueByParameter(parameter: ')
            .appendField(parameterDropDown(), 'parameter')
            .appendField(', specType: ')
            .appendField(specTypeDropDown(), 'specType')
            .appendField(')')
          this.setOutput(true, 'Number')
          this.setColour(230)
          this.setTooltip('')
          this.setHelpUrl(helpUrl)
        },
      }
    },
    generateFunction: (javascriptGenerator) => (block) => {
      const param = block.getFieldValue('parameter')
      const specType = block.getFieldValue('specType')
      const code = `getSpecValueByParameter('${param}', '${specType}')`

      return [code, javascriptGenerator.ORDER_FUNCTION_CALL] // 정의해둔 함수는 ORDER_FUNCTION_CALL을 반환
    },
  },

  {
    name: 'getHealthGradeByParameter',
    define: (dropdowns) => {
      const { parameterDropDown } = dropdowns
      return {
        init: function () {
          this.appendDummyInput()
            .appendField('Integer getHealthGradeByParameter(parameter : ')
            .appendField(parameterDropDown(), 'parameter')
            .appendField(')')
          this.setOutput(true, 'Number')
          this.setColour(230)
          this.setTooltip('')
          this.setHelpUrl(helpUrl)
        },
      }
    },
    generateFunction: (javascriptGenerator) => (block) => {
      const param = block.getFieldValue('parameter')
      const code = `getHealthGradeByParameter('${param}')`

      return [code, javascriptGenerator.ORDER_FUNCTION_CALL] // 정의해둔 함수는 ORDER_FUNCTION_CALL을 반환
    },
  },

  {
    name: 'returnBlock',
    define: () => {
      return {
        init: function () {
          this.appendValueInput('param').setCheck(null).appendField('Return')
          this.setPreviousStatement(true, null)
          this.setColour(230)
          this.setTooltip('')
          this.setHelpUrl(helpUrl)
        },
      }
    },
    generateFunction: (javascriptGenerator) => (block) => {
      const param = javascriptGenerator.valueToCode(block, 'param', javascriptGenerator.ORDER_NONE)
      return 'return ' + param + ';\n'
    },
  },
  {
    name: 'getSelfRecentlyValue',
    define: () => {
      return {
        init: function () {
          this.appendDummyInput().appendField('Double getSelfRecentlyValue()')
          this.setOutput(true, 'Number')
          this.setColour(230)
          this.setTooltip('')
          this.setHelpUrl(helpUrl)
        },
      }
    },
    // eslint-disable-next-line no-unused-vars
    generateFunction: (javascriptGenerator) => (block) => {
      return [`getSelfRecentlyValue()`, javascriptGenerator.ORDER_ATOMIC]
    },
  },
  {
    name: 'getPreviousValueByParameter',
    define: (dropdowns) => {
      const { parameterDropDown } = dropdowns
      return {
        init: function () {
          this.appendDummyInput()
            .appendField('Object getPreviousValueByParameter(parameter: ')
            .appendField(parameterDropDown(), 'parameter')
            .appendField(')')
          this.setOutput(true, null)
          this.setColour(230)
          this.setTooltip('')
          this.setHelpUrl(helpUrl)
        },
      }
    },
    generateFunction: (javascriptGenerator) => (block) => {
      const param = block.getFieldValue('parameter')
      const code = `getPreviousValueByParameter('${param}')`

      return [code, javascriptGenerator.ORDER_FUNCTION_CALL] // 정의해둔 함수는 ORDER_FUNCTION_CALL을 반환
    },
  },
  {
    name: 'getCurrentHours',
    define: () => {
      return {
        init: function () {
          this.appendDummyInput().appendField('Integer getCurrentHours()')
          this.setOutput(true, null)
          this.setColour(230)
          this.setTooltip('현재 시간의 시(hour)를 0~23 사이의 정수로 반환합니다.')
          this.setHelpUrl(helpUrl)
        },
      }
    },
    generateFunction: (javascriptGenerator) => () => {
      // provideFunction_으로 최상위에 함수 정의 추가
      const functionName = javascriptGenerator.provideFunction_('getCurrentHours', [
        'function ' + javascriptGenerator.FUNCTION_NAME_PLACEHOLDER_ + '() {',
        '  return parseInt(new Date().getHours())',
        '}',
      ])
      // 블록에서 생성되는 코드
      return [functionName + '()', javascriptGenerator.ORDER_FUNCTION_CALL]
    },
  },
  {
    name: 'calculateProductionLeadTime',
    define: (dropdowns) => {
      const { parameterDropDown } = dropdowns
      return {
        init: function () {
          this.appendDummyInput()
            .appendField('Integer 생산_리드타임_계산(생산량 Parameter: ')
            .appendField(parameterDropDown(), 'parameter')
            .appendField(', raw조회범위(초): ')
            .appendField(new Blockly.FieldNumber(300), 'duration')
            .appendField(')')
          this.setOutput(true, null)
          this.setColour(230)
          this.setTooltip('')
          this.setHelpUrl(helpUrl)
        },
      }
    },
    generateFunction: (javascriptGenerator) => (block) => {
      const param = block.getFieldValue('parameter')
      const duration = block.getFieldValue('duration')
      const code = `calculateProductionLeadTime('${param}', '${duration}')`

      return [code, javascriptGenerator.ORDER_FUNCTION_CALL] // 정의해둔 함수는 ORDER_FUNCTION_CALL을 반환
    },
  },
]

const apcBlocks = [
  {
    name: 'returnJsonBlocks',
    define: () => {
      return {
        init: function () {
          this.appendValueInput('items').setCheck('Array').appendField('ReturnValues')
          this.setPreviousStatement(true, null)
          this.setColour(230)
          this.setTooltip('')
          this.setHelpUrl(helpUrl)
        },
      }
    },
    generateFunction: (javascriptGenerator) => (block) => {
      const items = javascriptGenerator.valueToCode(block, 'items', javascriptGenerator.ORDER_ATOMIC)
      return `
        return {
          ${JSON.parse(items)
            .map((item) => `'${item.key}': ${item.value || null}`)
            .join(',\n')}
        }
      `
    },
  },
  {
    name: 'returnItem',
    define: () => {
      return {
        init: function () {
          this.appendValueInput('value')
            .setCheck('Number') // 'math_number' 타입의 블록을 받도록 설정
            .appendField('key:')
            .appendField(new Blockly.FieldTextInput(''), 'key')
            .appendField('value:')
          this.setOutput(true, null)
          this.setColour(230)
          this.setTooltip('')
          this.setHelpUrl(helpUrl)
        },
      }
    },
    generateFunction: (javascriptGenerator) => (block) => {
      const key = block.getFieldValue('key')
      const value = javascriptGenerator.valueToCode(block, 'value', javascriptGenerator.ORDER_ATOMIC)
      return [
        JSON.stringify({
          key: key,
          value: value,
        }),
        javascriptGenerator.ORDER_ATOMIC,
      ]
    },
  },
  {
    name: 'getSourceValueByKey',
    define: () => {
      return {
        init: function () {
          this.appendDummyInput()
            .appendField('Double getSourceValue(key : ')
            .appendField(new Blockly.FieldTextInput('source_key'), 'key')
            .appendField(')')
          this.setOutput(true, 'Number')
          this.setColour(230)
          this.setTooltip('')
          this.setHelpUrl(helpUrl)
        },
      }
    },
    generateFunction: (javascriptGenerator) => (block) => {
      const key = block.getFieldValue('key')
      return [`getSourceValue("${key}")`, javascriptGenerator.ORDER_ATOMIC]
    },
  },
  {
    name: 'isSimulation',
    define: () => {
      return {
        init: function () {
          this.appendDummyInput().appendField('boolean isSimulation()')
          this.setOutput(true, 'Boolean')
          this.setColour(230)
          this.setTooltip('')
          this.setHelpUrl(helpUrl)
        },
      }
    },
    // eslint-disable-next-line no-unused-vars
    generateFunction: (javascriptGenerator) => (block) => {
      return [`isSimulation()`, javascriptGenerator.ORDER_ATOMIC]
    },
  },
]

export {
  helpUrl,
  customBlocks,
  apcBlocks,
  AI_MODEL_PREDICTION_BLOCK,
  AI_MODEL_BLOCK_FIELD,
  NONE_SELECT_KEY,
  NONE_SELECT_VALUE,
}
