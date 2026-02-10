<template>
  <q-page class="q-pa-sm">
    <q-stepper keep-alive style="height: 100%" v-model="step" header-nav ref="stepper" color="primary" animated>
      <q-step
        :name="1"
        title="파라미터 상관관계 분석"
        icon="settings"
        :done="step > 1"
        :header-nav="step > 1"
        :disable="isModelingBuilding"
      >
        <step-parameter-select ref="step1Ref" style="height: calc(100% - 40px)" />
        <q-stepper-navigation style="height: 40px; margin-top: -20px; margin-right: 20px" class="step-navigation">
          <q-btn
            @click="
              () => {
                if (step1Ref.getData() === null) {
                  ui.notify.warning(t('Y 파라미터, 상관 관계 파라미터, 조회기간 정보를 확인해주세요'))
                  return false
                }
                step1Data = step1Ref.getData()
                step = 2
              }
            "
            class="sBtn q-ml-sm"
            :label="t('> 다음')"
          />
        </q-stepper-navigation>
      </q-step>

      <q-step
        style="height: calc(100% - 70px)"
        :name="2"
        title="모델 빌드"
        icon="mdi-robot"
        :done="step > 2"
        :header-nav="step > 2"
      >
        <step-model-build
          ref="step2Ref"
          v-if="step1Data && step === 2"
          style="height: calc(100% - 40px)"
          :selected-x-parameter-ids="step1Data.selectedXParameter.map((param) => param.id)"
          :selected-y-parameter-id="step1Data.selectedYParameterId"
          :from-date-time="step1Data.fromDateTime"
          :to-date-time="step1Data.toDateTime"
        />

        <q-stepper-navigation style="height: 30px" class="step-navigation">
          <q-btn @click="step = 1" label="< 이전" class="sBtn negative q-ml-sm" :disable="isModelingBuilding" />
          <q-btn
            @click="modelBuildButtonClick"
            :loading="isModelingBuilding"
            :fail="isModelBuildFail"
            class="sBtn q-ml-sm"
            :style="{
              width: isModelBuildFail ? '150px' : '130px',
            }"
          >
            <template #loading>
              <q-spinner size="16px" />
              <div style="margin-left: 5px">{{ step2NextButtonLabel }}</div>
            </template>
            <template #default>
              <div class="row items-center no-wrap">
                <q-icon v-if="!isModelBuildFail && !isModelingBuilding && !modelId" name="mdi-robot" class="q-mr-xs" />
                <q-icon v-if="isModelBuildFail" name="mdi-exclamation-thick" class="q-mr-xs" />
                <div style="margin-left: 5px">{{ step2NextButtonLabel }}</div>
              </div>
            </template>
          </q-btn>
        </q-stepper-navigation>
      </q-step>

      <q-step :name="3" title="모델 확인" icon="add_comment" :header-nav="step > 3">
        <step-model-validate :modelId="modelId" />
        <q-stepper-navigation style="height: 30px" class="step-navigation">
          <q-btn v-if="!isFromModelListPage" @click="step = 2" label="< 이전" class="sBtn negative q-ml-sm" />
          <q-btn @click="goToModelList" label="모델 목록" class="sBtn q-ml-sm" />
        </q-stepper-navigation>
      </q-step>
    </q-stepper>
  </q-page>

  <q-dialog v-model="visibleModelBuildDialog" persistent>
    <q-card class="dialog-container" style="width: 700px">
      <q-card-section class="bg-secondary text-white">
        <div class="text-h6">{{ $t('가상센서 모델 빌드') }}</div>
      </q-card-section>
      <q-separator />
      <div class="row">
        <div class="col">
          <q-card-section>
            <q-form ref="modelBuildForm">
              <div class="row">
                <q-input
                  v-model="modelBuildFormData.name"
                  :label="$t('모델명')"
                  class="input-required col"
                  :rules="[$rules.required]"
                />
              </div>
              <div class="row">
                <q-input v-model="modelBuildFormData.description" :label="$t('설명')" class="col" />
              </div>
              <div class="row">
                <filterable-select
                  v-model="modelBuildFormData.algorithm"
                  :options="algorithmList"
                  :label="$t('알고리즘')"
                  class="input-required col"
                  :rules="[$rules.required]"
                />
              </div>
            </q-form>
          </q-card-section>
        </div>
      </div>

      <q-separator />

      <q-card-actions class="justify-between">
        <span class="field-required-indicate">{{ $t('필수 입력 필드') }}</span>
        <div>
          <q-btn flat color="negative" :label="$t('닫기')" @click="visibleModelBuildDialog = false" />
          <q-btn flat color="primary" :label="modelBuildButtonLabel" @click="modelBuildActionButtonClick" />
        </div>
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup>
import { computed, onBeforeUnmount, ref } from 'vue'
import { t } from 'src/plugins/i18n'
import StepParameterSelect from 'pages/ai/modeling/parameterSelectStep/StepParameterSelect.vue'
import StepModelBuild from 'pages/ai/modeling/StepModelBuild.vue'
import useUI from 'src/common/module/ui'
import AiService from 'src/services/ai/AiService'
import StepModelValidate from 'pages/ai/modeling/StepModelValidate.vue'
import router from 'src/router'

const algorithmList = ['Linear Regression']

const props = defineProps({
  modelId: {
    type: Number,
    required: false,
  },
})

const startStep = props.modelId ? 3 : 1

const ui = useUI()
const step = ref(startStep)

const step1Ref = ref(null)
const step2Ref = ref(null)
const step1Data = ref(null)

const visibleModelBuildDialog = ref(false)
const modelBuildForm = ref(null)
const modelBuildFormData = ref({
  name: null,
  description: null,
  algorithm: null,
})

const isModelingBuilding = ref(false)
const isModelBuildFail = ref(false)
const step2NextButtonLabel = computed(() => {
  if (isModelBuildFail.value) {
    return t('모델 빌드 실패')
  }
  if (isModelingBuilding.value) {
    return t('학습중...')
  }
  if (modelId.value) {
    return t('> 다음')
  }
  return t('모델 빌드')
})
const modelBuildButtonLabel = computed(() => {
  if (isModelingBuilding.value) {
    return t('학습중...')
  } else {
    return t('모델 빌드')
  }
})
const isFromModelListPage = computed(() => {
  return !!router.currentRoute.value.query.id
})
const modelBuildCheckInterval = ref(null)
const buildCompleteModel = ref(null)

const modelId = computed(() => {
  if (props.modelId) {
    return parseInt(props.modelId, 10)
  }
  if (buildCompleteModel.value) {
    return buildCompleteModel.value.id
  }
  return null
})

const modelBuildButtonClick = async () => {
  if (isModelBuildFail.value) {
    step.value = 2
    ui.notify.error(t('모델 빌드에 실패했습니다. 다시 모델을 만들어주세요.'))
    return
  }

  if (buildCompleteModel.value) {
    step.value = 3
    return
  }

  const dataRange = step2Ref.value.getDataRange()
  if (!dataRange) {
    ui.notify.warning(t('학습할 범위를 선택해주세요'))
    return false
  }

  modelBuildFormData.value = {
    name: null,
    description: null,
    algorithm: null,
  }
  visibleModelBuildDialog.value = true
}

const modelBuildActionButtonClick = async () => {
  const isValid = await modelBuildForm.value.validate()
  if (!isValid) {
    ui.notify.warning(t('필수 입력값을 입력해주세요'))
    return false
  }
  const dataRange = step2Ref.value.getDataRange()
  //X파라미터 ID와 각 파라미터의 최소 값, 최대 값을 가지고 있는 배결 객체 생성
  const xParameters = step1Data.value.selectedXParameter.map((value) => {
    const paramData = dataRange.data.find((param) => param.name === value.name)

    const { min, max } = paramData.data.reduce(
      (result, item) => {
        return {
          min: item.value < result.min ? item.value : result.min,
          max: item.value > result.max ? item.value : result.max,
        }
      },
      { min: paramData.data[0].value, max: paramData.data[0].value },
    )

    return { id: value.id, min, max }
  })

  try {
    const response = await AiService.buildModel({
      name: modelBuildFormData.value.name,
      description: modelBuildFormData.value.description,
      algorithm: modelBuildFormData.value.algorithm,
      xParameters: xParameters,
      yParameterId: step1Data.value.selectedYParameterId,
      from: dataRange.from,
      to: dataRange.to,
    })

    isModelingBuilding.value = true
    visibleModelBuildDialog.value = false
    ui.notify.success(t('모델 빌드 요청에 성공하였습니다'))
    modelBuildCheckInterval.value = setInterval(async () => {
      const model = await AiService.getModeById(response.id)
      if (model.status === 'COMPLETE' || model.status === '완료') {
        clearInterval(modelBuildCheckInterval.value)
        modelBuildCheckInterval.value = null
        isModelingBuilding.value = false
        buildCompleteModel.value = model
        ui.notify.success(t('모델 빌드가 완료되었습니다'))
      }
    }, 1500)
  } catch (error) {
    ui.notify.error(t('모델 빌드 중 에러가 발생했습니다.'))
    isModelBuildFail.value = true
  }
}
const goToModelList = async () => {
  await router.push({ path: `/ai/model-list` })
}
onBeforeUnmount(() => {
  if (modelBuildCheckInterval.value !== null) {
    clearInterval(modelBuildCheckInterval.value)
  }
})
</script>

<style lang="scss" scoped>
:deep(.q-stepper__content) {
  height: calc(100% - 70px) !important;
}

:deep(.q-stepper__step-content) {
  height: 100%;
}

:deep(.q-stepper__step-inner) {
  height: 100%;
}

.step-navigation {
  display: flex;
  justify-content: flex-end;
}

.step-navigation .q-btn {
  width: 130px;
}
</style>
