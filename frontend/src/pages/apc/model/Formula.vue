<template>
  <div ref="blocklyDiv" id="blocklyDiv" />
</template>
<script setup>
import * as Blockly from 'blockly'
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { apcBlocks } from 'components/blockly/customBlocks'
import { javascriptGenerator } from 'blockly/javascript'

const props = defineProps({
  workspaceJsonString: {
    type: String,
  },
})
let workspace = ref(null)
let blocklyDiv = ref(null)

onMounted(() => {
  initBlockly()
  if (props.workspaceJsonString) {
    loadBlocklyWorkspace(props.workspaceJsonString)
    updateSize()
  }
})
onBeforeUnmount(() => {
  clearBlockly()
})

const initBlockly = () => {
  registerCustomBlocks()
  workspace.value = Blockly.inject(blocklyDiv.value, {
    toolbox: '<xml></xml>',
    readOnly: true,
    scrollbars: false,
  })
}

const loadBlocklyWorkspace = (workspaceJson) => {
  const json = JSON.parse(workspaceJson)
  const offsetX = Math.abs(Math.min(...json.blocks.blocks.map((b) => b.x)))
  const offsetY = Math.abs(Math.min(...json.blocks.blocks.map((b) => b.y)))
  json.blocks.blocks.forEach((block) => {
    block.x += offsetX
    block.y += offsetY
  })
  Blockly.serialization.workspaces.load(json, Blockly.getMainWorkspace())
}
const registerCustomBlocks = () => {
  apcBlocks.forEach((customBlock) => {
    Blockly.Blocks[customBlock.name] = customBlock.define()
    Object.assign(javascriptGenerator.forBlock, {
      [customBlock.name]: customBlock.generateFunction(javascriptGenerator),
    })
  })
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

const updateSize = () => {
  if (workspace.value) {
    const metrics = Blockly.getMainWorkspace().getMetrics()
    const width = metrics.contentWidth
    const height = metrics.contentHeight
    const svgs = blocklyDiv.value.querySelectorAll('svg')

    svgs.forEach((svg) => {
      svg.setAttribute('width', width + 20)
      svg.setAttribute('height', height + 20)
      svg.style.padding = 10
      svg.style.margin = 10
    })
  }
}
</script>

<style scoped lang="scss">
:deep(.injectionDiv) {
  overflow: visible;

  .blocklySvg {
    position: relative;
  }

  .blocklyMainBackground {
    display: none;
  }

  g.blocklyBlockCanvas {
    position: relative;

    > g:nth-of-type(2) {
      transform: none !important;
    }
  }
}
</style>
