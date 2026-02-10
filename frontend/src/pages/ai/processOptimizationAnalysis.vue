<template>
  <q-page class="q-pa-sm">
    <q-card class="cust_subPage">
      <q-form ref="formRef" @submit.prevent="getData">
        <q-card-section class="row search_section_wrap">
          <q-item class="col-2 search_left">
            <q-item-section>
              <cascader
                v-model="selectModelTreeId"
                class="input-required col q-pr-md q-mt-md"
                :label="$t('장비/모델')"
                :titles="[$t('장비'), $t('모델')]"
                :items="options.models"
                item-value="treeId"
                item-text="name"
                clearable
                :valid-depth="2"
                :valid-depth-msg="$t('모델은 장비 하위에만 추가 할 수 있습니다.')"
                :rules="[$rules.required]"
                :disable="optimizeId != null"
              />
              <q-input
                v-model.number="editItem.targetValue"
                :label="$t('Target Value(Y)')"
                type="number"
                outlined
                :disable="isProcess"
                :rules="[(val) => val > 0 || '1보다 크거나 같아야 합니다']"
                class="required-field q-pr-md"
              />
              <q-input
                v-model="editItem.name"
                :label="$t('제목')"
                :rules="[
                  (val) => !!val || '필수 입력값입니다',
                  (val) => val.length <= 100 || '100자 이내로 입력하세요',
                ]"
                outlined
                clearable
                :disable="isProcess"
                class="required-field q-pr-md"
              />
            </q-item-section>
          </q-item>
          <q-item class="col-6 search_right">
            <q-item-section>
              <q-btn class="sBtn q-ml-sm" @click="getData" :disable="isProcess || isOptimizationComplete">
                <template #default>
                  <q-icon name="mdi-play" class="q-mr-xs" />
                  <span>{{ $t('공정최적화 실행') }}</span>
                </template>
              </q-btn>
            </q-item-section>
          </q-item>
        </q-card-section>

        <!-- 파라미터 및 분석 결과 섹션 -->
        <q-card-section>
          <div class="row q-col-gutter-md">
            <!-- 파라미터 테이블 -->
            <div class="col-5">
              <q-card flat bordered class="q-mb-md">
                <q-card-section>
                  <div class="text-h6 text-weight-bold">{{ $t('Target 파라미터') }}</div>
                </q-card-section>
                <q-separator style="border-style: dashed" />
                <q-card-section>
                  <div class="text-subtitle1">{{ editItem.yParameterName }}</div>
                </q-card-section>
              </q-card>
              <q-card flat bordered class="q-mb-md">
                <q-card-section>
                  <div class="row items-center justify-between">
                    <div class="text-h6 text-weight-bold">{{ $t('Recipe 대상 파라미터') }}</div>
                    <q-input
                      v-model.number="editItem.step"
                      :label="$t('Step')"
                      type="number"
                      dense
                      outlined
                      class="step-button"
                      :disable="isProcess"
                    />
                  </div>
                </q-card-section>
                <q-separator style="border-style: dashed" />
                <q-table
                  :rows="param.items"
                  :columns="param.paramHeader"
                  row-key="dpParamNoX"
                  :pagination="{ rowsPerPage: -1 }"
                  hide-bottom
                >
                  <template #body-cell-minScaleX="props">
                    <q-td :props="props">
                      <q-input
                        v-model.number="props.row.minScaleX"
                        type="number"
                        dense
                        outlined
                        :disable="isProcess"
                        :rules="getMinScaleRules(props.row)"
                      />
                    </q-td>
                  </template>

                  <template #body-cell-maxScaleX="props">
                    <q-td :props="props">
                      <q-input
                        v-model.number="props.row.maxScaleX"
                        type="number"
                        dense
                        outlined
                        :disable="isProcess"
                        :rules="getMaxScaleRules(props.row)"
                      />
                    </q-td>
                  </template>
                  <template #body-cell-step="props">
                    <q-td :props="props">
                      <q-input
                        v-model.number="props.row.step"
                        type="number"
                        dense
                        outlined
                        :disable="isProcess"
                        :rules="[
                          (val) => !!val || '필수 입력값입니다',
                          (val) => val > 0 || '1보다 크거나 같아야 합니다',
                        ]"
                      />
                    </q-td>
                  </template>
                </q-table>
              </q-card>
            </div>

            <!-- 분석 결과 영역 -->
            <div class="col-7">
              <NoDataNotice
                v-if="!isStartOptimization && !optimizeId"
                title="공정최적화"
                message="공정최적화 조건을 설정한 후 공정최적화 실행 버튼을 클릭해주세요."
              />
              <!-- 진행률 표시 -->
              <div v-else>
                <q-card>
                  <q-card-section>
                    <div class="text-h6 text-weight-bold">{{ $t('공정최적화 진행상황') }}</div>
                  </q-card-section>
                </q-card>
                <q-linear-progress :value="progressPercent / 100" size="35px" color="secondary" class="q-mb-md">
                  <div class="absolute-full flex flex-center justify-center progress">
                    <span class="progress-text">
                      {{ progressPercent }}% ({{ progress.completeCount }} / {{ progress.totalCount }})
                    </span>
                  </div>
                </q-linear-progress>

                <!-- 분석 결과 테이블 -->
                <q-table
                  :rows="analysis.items"
                  :columns="analysis.headers"
                  row-key="id"
                  :pagination="{ rowsPerPage: -1 }"
                  flat
                  bordered
                  hide-bottom
                >
                  <!-- y 컬럼 헤더 커스터마이징 -->
                  <template #header-cell-y="props">
                    <q-th :props="props" style="color: rgb(38, 166, 154)">
                      {{ props.col.label }}
                    </q-th>
                  </template>

                  <!-- y 컬럼 셀 커스터마이징 -->
                  <template #body-cell-y="props">
                    <q-td :props="props" style="color: rgb(38, 166, 154)">
                      {{ props.value }}
                    </q-td>
                  </template>
                </q-table>
              </div>
            </div>
          </div>
        </q-card-section>
      </q-form>
    </q-card>
  </q-page>
</template>

<script setup>
import ProcessOptimizationService from 'src/services/processOptimization/ProcessOptimizationService'
import ToolService from 'src/services/modeling/ToolService'
import { computed, onMounted, onBeforeUnmount, ref, watch } from 'vue'
import { useQuasar } from 'quasar'
import { t } from 'src/plugins/i18n'
import _ from 'lodash'
import ParameterService from 'src/services/modeling/ParameterService'
import NoDataNotice from 'components/common/NoDataNotice.vue'

const formRef = ref(null)
const $q = useQuasar()
const isStartOptimization = ref(false)
const isOptimizationComplete = ref(false)

const props = defineProps({
  id: {
    type: Number,
    required: false,
  },
})

const optimizeId = props.id

console.log('props', props, optimizeId)

// 입력 필드 데이터
const editItem = ref({
  name: '',
  targetValue: undefined,
  object: undefined,
  step: undefined,
  origin: null,
})

// 모델과 도구 옵션
const options = ref({
  tools: [],
  models: [],
})

const param = ref({
  paramHeader: [
    {
      name: 'dpParamNoName',
      label: t('Parameter'),
      field: 'dpParamNoName',
      align: 'left',
      class: 'bg-primary text-white',
    },
    { name: 'minScaleX', label: t('Min'), field: 'minScaleX', align: 'left', class: 'bg-primary text-white' },
    { name: 'maxScaleX', label: t('Max'), field: 'maxScaleX', align: 'left', class: 'bg-primary text-white' },
    { name: 'step', label: t('Step'), field: 'step', align: 'left', class: 'bg-primary text-white' },
  ],
  items: [],
})

// 분석 결과
const analysis = ref({
  headers: [],
  items: [],
})

// 진행률 데이터
const progress = ref({
  totalCount: 0,
  completeCount: 0,
})

// 상태 변수
const selectModelTreeId = ref(undefined)
const isProcess = ref(false)

const checkProgressInterval = ref(null)

// Computed
const progressPercent = computed(() => {
  if (progress.value.totalCount === 0) return 0
  return Math.round((progress.value.completeCount / progress.value.totalCount) * 100)
})

// 컴포넌트 마운트 시 데이터 로드
const getCode = async () => {
  try {
    const [tools, models] = await Promise.all([ToolService.getTools(), ProcessOptimizationService.getAiModelTree()])
    options.value.tools = tools
    options.value.models = models
  } catch (error) {
    $q.notify({
      type: 'negative',
      message: t('데이터 로드 중 오류가 발생했습니다.'),
    })
  }
}

const loadOptimizationAnalysis = async (id) => {
  try {
    const result = await ProcessOptimizationService.getOptimization(id)
    if (result) {
      isProcess.value = true
      editItem.value = {
        ..._.cloneDeep(editItem.value),
        origin: result,
        name: result.name,
        targetValue: result.targetValue,
        aiModel: result.aiModel,
      }
      selectModelTreeId.value = `MODEL_${result.aiModelId}`
      progress.value = {
        completeCount: result.completeCount,
        totalCount: result.totalCount,
      }
    }
  } catch (error) {
    console.error(error)
    $q.notify({
      type: 'negative',
      message: t('최적화 분석 데이터 로드 중 오류가 발생했습니다.'),
    })
  }
}

const findTreeItem = (treeId) => {
  const expandTreeData = (items) => {
    const data = []
    items.forEach((item) => {
      data.push(item)
      if (item.children) {
        data.push(...expandTreeData(item.children))
      }
    })
    return data
  }
  return expandTreeData(options.value.models).find((v) => v.treeId === treeId)
}

const getData = async () => {
  const isValid = await formRef.value?.validate()

  let isStepValid = true

  if (editItem.value.step == null) {
    isStepValid = param.value.items.every((item) => item.step != null && item.step > 0)
  }

  if (!isValid || editItem.value.targetValue == null || !editItem.value.name || !isStepValid) {
    $q.notify({
      type: 'warning',
      message: t('1필수값이 입력되지 않았습니다.'),
    })
    return
  }

  if (param.value.items.length === 0) {
    $q.notify({
      type: 'warning',
      message: t('2필수값이 입력되지 않았습니다.'),
    })
    return
  }
  const optimization = {
    name: editItem.value.name,
    targetValue: editItem.value.targetValue,
    aiModelId: editItem.value.aiModel.id,
    toolId: editItem.value.object.toolId,
    optimizationParameters: param.value.items.map((item) => ({
      parameterId: item.dpParamNoX,
      minScaleX: item.minScaleX,
      maxScaleX: item.maxScaleX,
      step: item.step,
    })),
  }
  progress.value = {
    completeCount: 0,
    totalCount: 0,
  }
  //min, max step 값
  analysis.value = {
    headers: [],
    items: [],
  }
  isStartOptimization.value = true
  await saveProcess(optimization)
}

const saveProcess = async (item) => {
  try {
    $q.loading.show()
    const result = await ProcessOptimizationService.saveProcess(item)
    if (result) {
      isProcess.value = true
      analysis.value.headers = [
        ...param.value.items.map((v) => ({
          name: v.dpParamNoX,
          label: v.dpParamNoName,
          field: v.dpParamNoX,
          align: 'left',
          class: 'bg-purple-2',
        })),
        { name: 'y', label: editItem.value.yParmeterName, field: 'y', align: 'left', class: 'bg-purple-2' },
      ]
      startCheckProgress(result)
    }
  } catch (error) {
    console.error(error)
    $q.notify({
      type: 'negative',
      message: t('저장 중 오류가 발생했습니다.'),
    })
  } finally {
    $q.loading.hide()
  }
}

const getOptimalYValueList = async (id) => {
  try {
    $q.loading.show()
    const result = await ProcessOptimizationService.getOptimalYValueList(id)
    if (result) {
      analysis.value.headers = [
        ...param.value.items.map((v) => ({
          name: v.dpParamNoX,
          label: v.dpParamNoName,
          field: v.dpParamNoX,
          align: 'left',
          class: 'bg-purple-2',
        })),
        {
          name: 'y',
          label: editItem.value.yParameterName,
          field: 'y',
          align: 'left',
          class: 'bg-purple-2',
        },
      ]
      analysis.value.items = result
    }
  } catch (error) {
    console.error(error)
    $q.notify({
      type: 'negative',
      message: t('최적값 조회 중 오류가 발생했습니다.'),
    })
  } finally {
    $q.loading.hide()
  }
}

const startCheckProgress = (item) => {
  progress.value = {
    totalCount: item.totalCount,
    completeCount: item.completeCount,
  }

  checkProgressInterval.value = setInterval(async () => {
    try {
      const result = await ProcessOptimizationService.getOptimization(item.id)
      if (result) {
        progress.value = {
          totalCount: result.totalCount,
          completeCount: result.completeCount,
        }

        if (result.status === 'SUCCESS') {
          clearCheckProgressInterval()
          await getOptimalYValueList(item.id)
          isProcess.value = false
          isOptimizationComplete.value = true
        }
      }
    } catch (error) {
      console.error(error)
      isProcess.value = false
      clearCheckProgressInterval()
      $q.notify({
        type: 'negative',
        message: t('공정 최적화 분석 확인 중 오류가 발생했습니다.'),
      })
    }
  }, 3000)
}

const clearCheckProgressInterval = () => {
  if (checkProgressInterval.value) {
    clearInterval(checkProgressInterval.value)
    checkProgressInterval.value = null
  }
}

const handleRulesCheck = (item) => {
  if (!item || item.minScaleX === undefined || item.maxScaleX === undefined) return false
  return Number(item.minScaleX) > Number(item.maxScaleX)
}

const getMinScaleRules = (item) => {
  const baseRules = [
    (val) => (val !== null && val !== undefined) || t('필수 입력값입니다'),
    (val) => !isNaN(val) || t('숫자를 입력하세요'),
  ]

  return handleRulesCheck(item)
    ? [...baseRules, (val) => val <= item.maxScaleX || t('최대값보다 작아야 합니다')]
    : baseRules
}

const getMaxScaleRules = (item) => {
  const baseRules = [
    (val) => (val !== null && val !== undefined) || t('필수 입력값입니다'),
    (val) => !isNaN(val) || t('숫자를 입력하세요'),
  ]

  return handleRulesCheck(item)
    ? [...baseRules, (val) => val >= item.minScaleX || t('최소값보다 커야 합니다')]
    : baseRules
}

watch(
  () => selectModelTreeId.value,
  async () => {
    isOptimizationComplete.value = false
    if (optimizeId != undefined && optimizeId != '') {
      if (selectModelTreeId.value != undefined) {
        let selectedModel = findTreeItem(selectModelTreeId.value)
        const yParameter = await ParameterService.getParameterById(selectedModel.object.yparameterId)
        if (selectedModel) {
          editItem.value = Object.assign(_.cloneDeep(editItem.value), {
            object: selectedModel.object,
            dpParamNoX: editItem.value?.origin?.optimizationParameterList.parameterId,
            minScaleX: editItem.value?.origin?.optimizationParameterList.minScaleX,
            maxScaleX: editItem.value?.origin?.optimizationParameterList.maxScaleX,
            yParameterName: yParameter.name,
          })
          const parmaeterNameMap = editItem.value.object.parameterDetails.reduce((acc, cur) => {
            acc[cur.id] = cur.name
            return acc
          }, {})
          param.value.items = editItem.value.origin.optimizationParameterList.map((parameter) => {
            return {
              dpParamNoX: parameter.parameterId,
              dpParamNoName: parmaeterNameMap[parameter.parameterId],
              minScaleX: parameter.minScaleX,
              maxScaleX: parameter.maxScaleX,
              step: parameter.step,
            }
          })
        }
      }
    } else {
      if (selectModelTreeId.value != undefined && selectModelTreeId.value.startsWith('MODEL_')) {
        let selectedModel = findTreeItem(selectModelTreeId.value)
        const yParameter = await ParameterService.getParameterById(selectedModel.object.yparameterId)
        if (selectedModel) {
          editItem.value = Object.assign(_.cloneDeep(editItem.value), {
            object: selectedModel.object,
            dpParamNoX: selectedModel.object?.parameterDetails.map((param) => param.id) || [],
            dpParamNoName: selectedModel.object?.parameterDetails.map((param) => param.name) || [],
            minScaleX: selectedModel.object?.parameterDetails.map((param) => param.min) || [],
            maxScaleX: selectedModel.object?.parameterDetails.map((param) => param.max) || [],
            aiModel: selectedModel.object,
            yParameterName: yParameter.name,
          })
          param.value.items = editItem.value.dpParamNoX.map((v, index) => ({
            dpParamNoX: v,
            dpParamNoName: editItem.value.dpParamNoName[index],
            minScaleX:
              editItem.value.minScaleX != undefined && editItem.value.minScaleX[index]
                ? editItem.value.minScaleX[index]
                : 0,
            maxScaleX:
              editItem.value.maxScaleX != undefined && editItem.value.maxScaleX[index]
                ? editItem.value.maxScaleX[index]
                : 0,
            step: editItem.value.step,
            aiModel: editItem.value.aiModel,
          }))
        }
      }
    }
  },
)
watch(
  () => param.value.items,
  () => {
    isOptimizationComplete.value = false
  },
  { deep: true },
)
watch(
  () => editItem.value.step,
  (newVal) => {
    if (newVal != null && param.value.items.length > 0) {
      param.value.items.forEach((item) => {
        item.step = newVal
      })
    }
  },
)
// 초기화
onMounted(async () => {
  await getCode()
  const optimizationId = props.id
  if (optimizationId) {
    await loadOptimizationAnalysis(optimizationId)
    await getOptimalYValueList(optimizationId)
  }
})

onBeforeUnmount(() => {
  clearCheckProgressInterval()
})
</script>
<style lang="scss" scoped>
.progress-text {
  font-size: 14px; // 텍스트 크기 조정
  line-height: 30px; // progress bar의 height와 동일하게
  white-space: nowrap; // 텍스트가 줄바꿈되지 않도록
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5); // 텍스트 가독성 향상
}

.parameter-header {
  padding: 8px 12px;
  background-color: rgba(0, 0, 0, 0.02);
  border-radius: 4px;
}

.parameter-divider {
  border-width: 1px;
  opacity: 0.7;
}

.parameter-value {
  font-size: 18px;
  font-weight: 500;
  padding: 4px 0;
}

.step-button {
  width: 130px;
  margin-top: 10px;
}

.progress {
  display: flex;
  justify-content: center;
}

.progress-text {
  height: 85%;
  font-size: 15px;
  background-color: white;
  border-radius: 5px;
  color: rgb(38, 166, 154);
  box-shadow: none;
  padding: 0 8px;
}
</style>
