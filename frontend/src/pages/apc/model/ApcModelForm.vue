<template>
  <q-card>
    <q-card-section>
      <q-form class="full-width full-height" ref="form">
        <div class="row" v-if="modelVersionId || isEditMode">
          <div class="col-4 q-pr-sm">
            <q-input v-model="activeVersion.value" readonly :label="$t('Active 버전')" disable></q-input>
          </div>
          <div class="col-7 q-pl-xs">
            <div class="row">
              <q-item class="col-10 q-pa-none">
                <filterable-select
                  ref="versionSelect"
                  :disable="isEditMode"
                  v-model="formValue.versionId"
                  :label="$t('Current 버전')"
                  :options="modelVersions"
                  option-value="id"
                  option-label="version"
                  emit-value
                  map-options
                >
                  <template v-slot:option="props">
                    <q-item clickable @click="selectVersion(props.opt)">
                      <q-item-section>{{ props.label }}</q-item-section>
                      <description-tooltip :description="props.opt.description" />
                    </q-item>
                  </template>
                </filterable-select>
              </q-item>
              <q-item class="col q-pl-sm q-pt-md q-mt-xs q-pb-none">
                <q-checkbox
                  :disable="formValue.versionId === activeVersion.id"
                  v-model="formValue.isActive"
                  @update:model-value="onActive"
                >
                  {{ $t('Active') }}
                </q-checkbox>
              </q-item>
            </div>
          </div>
        </div>
        <q-input
          v-model="formValue.modelName"
          :label="$t('모델명')"
          :rules="[$rules.required]"
          class="input-required"
        />
        <div class="row" v-for="apcCode in apcConditions" :key="apcCode.key">
          <q-input
            v-model="conditions[apcCode.key]"
            :label="$t(apcCode.name)"
            :rules="formValue.isChecked[apcCode.key] ? '' : [$rules.required]"
            :disable="formValue.isChecked[apcCode.key] || modelVersionId !== null"
            class="col q-pr-sm input-required"
          />
          <q-checkbox
            class="col-2 q-mt-md"
            size="md"
            v-model="formValue.isChecked[apcCode.key]"
            :disable="modelVersionId !== null"
            @update:model-value="(value) => onCheck(value, apcCode.key)"
            >{{ $t('전체') }}
          </q-checkbox>
        </div>
      </q-form>
      <div class="q-py-lg">
        <q-input
          v-model="formValue.description"
          :label="$t('설명')"
          filled
          outlined
          type="textarea"
          class="full-height"
        />
      </div>
      <div class="row">
        <div class="col-6 q-pr-sm">
          <q-toggle v-model="formValue.isUse" :label="$t('사용 여부')" />
        </div>
        <div class="col-6 q-pr-sm">
          <q-toggle v-model="formValue.isUseNotify" :label="$t('실행결과 알림 여부')" />
        </div>
      </div>
    </q-card-section>
  </q-card>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import ApcModelingService from 'src/services/apc/ApcModelService'
import { useAPCStore } from 'stores/apc'
import useUI from 'src/common/module/ui'
import { storeToRefs } from 'pinia'
import { apcConditionDelimiterToObject, DELIMITER } from 'src/common/apc/apcUtil'
import { t } from '/src/plugins/i18n'
import DescriptionTooltip from 'components/common/DescriptionTooltip.vue'

const apcStore = useAPCStore()
const { apcConditions } = storeToRefs(apcStore)

const ui = useUI()
const { modelVersionId } = defineProps({
  modelVersionId: {
    type: Number,
    required: false,
    default: () => null,
  },
})
const conditions = ref({})
const isEditMode = ref(false)
const modelVersions = ref([])
const form = ref(null)
const versionSelect = ref(null)

const formValue = ref({
  modelName: null,
  isUse: false,
  description: null,
  isChecked: {},
  versionId: !modelVersionId ? 1 : '',
  isActive: true,
  isUseNotify: false,
})

onMounted(async () => {
  await loadBaseData()
})

watch(
  () => conditions.value,
  () => {
    formValue.value.combineValue = combineValues()
  },
  { deep: true },
)

const activeVersion = computed(() => {
  const activeModelVersion = modelVersions.value.find((v) => v.isActive)
  return { id: activeModelVersion?.id, value: activeModelVersion?.version }
})

const emit = defineEmits(['changeVersion'])
emit('changeVersion', formValue.value)

defineExpose({
  validate: async () => {
    const result = await form.value.validate()
    if (!result) {
      return { success: false, formValue: null }
    }
    return {
      success: true,
      formValue: formValue.value,
    }
  },
  createModelVersion: () => {
    formValue.value.versionId = t('신규 버전')
    formValue.value.isActive = false
    isEditMode.value = true
  },
})

const loadBaseData = () => {
  if (modelVersionId) {
    getApcModelData()
  } else {
    formValue.value.isChecked = apcConditions.value.reduce((prev, curr) => {
      prev[curr.key] = false
      return prev
    }, {})
  }
}

const getApcModelData = async () => {
  if (!modelVersionId) return
  const apcModel = await ApcModelingService.getApcModelVersion(modelVersionId)
  modifyMode(apcModel)
  await setBaseData(apcModel)
  await getVersions(apcModel.apcModelId)
}

const setBaseData = async (apcModel) => {
  formValue.value.versionId = modelVersionId
  formValue.value.isUse = apcModel.isUse

  const conditionObj = await apcConditionDelimiterToObject(apcModel.conditions)
  conditions.value = conditionObj
  formValue.value.isChecked = Object.keys(conditionObj).reduce((prev, curr) => {
    prev[curr] = conditionObj[curr] === '*'
    return prev
  }, {})
}

const modifyMode = (value) => {
  formValue.value.versionId = value.id
  formValue.value.apcModelId = value.apcModelId
  formValue.value.description = value.description
  formValue.value.modelName = value.modelName
  formValue.value.formulaWorkspace = value.formulaWorkspace
  formValue.value.formulaScript = value.formulaScript
  formValue.value.isUseNotify = value.isUseNotify
  formValue.value.isActive = value.isActive
}

const getVersions = async (apcModelId) => {
  modelVersions.value = await ApcModelingService.getModelVersionsByModelId(apcModelId)
}

const selectVersion = async (value) => {
  const onChangeValue = await ApcModelingService.getApcModelVersion(value.id)
  if (versionSelect.value && versionSelect.value.hidePopup) {
    versionSelect.value.hidePopup()
    await modifyMode(onChangeValue)
  }
}

const combineValues = () => {
  let combineValue = null
  Object.keys(conditions.value).forEach((k, i) => {
    let conditionValue = conditions.value[k] === '' ? '*' : conditions.value[k]
    if (i === 0) {
      combineValue = conditionValue
    } else {
      combineValue += DELIMITER + conditionValue
    }
  })
  return combineValue
}

const onActive = () => {
  if (!formValue.value.isActive) return
  const onCancel = () => {
    formValue.value.isActive = false
  }
  ui.confirm(t('Active'), t('선택 버전을 Active 하시겠습니까?')).onCancel(onCancel)
}

const onCheck = (isChecked, key) => {
  conditions.value[key] = isChecked ? '*' : ''
}
</script>

<style scoped></style>
