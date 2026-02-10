<template>
  <q-card class="column full-height blocklySectionWrap">
    <q-card-section class="col bg-grey-8 q-pa-none flex justify-center items-center">
      <Blockly ref="blocklyRef" :is-apc="true" :workspace-json-string="workspaceJsonString" />
    </q-card-section>
  </q-card>

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
import { computed, ref } from 'vue'
import useUI from 'src/common/module/ui'
import { t } from '/src/plugins/i18n'
import Blockly from 'components/blockly/Blockly.vue'

const { notify } = useUI()

const props = defineProps({
  loadWorkspaceJson: {
    type: String,
    required: false,
  },
})
const blocklyRef = ref(null)

const workspaceJsonString = computed(() => {
  return props.loadWorkspaceJson
})

// Blockly 디버깅용
const blocklyResult = ref({ open: false })

defineExpose({
  getSectionData: () => {
    if (!blocklyRef.value) {
      return {
        script: null,
        workspaceJson: null,
      }
    }

    const { script, workspaceJson } = blocklyRef.value.getWorkspaceData()

    return {
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
  activeEditMode: () => {
    if (!blocklyRef.value) return
    blocklyRef.value.activeEditMode()
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
