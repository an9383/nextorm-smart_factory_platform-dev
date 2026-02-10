<template>
  <!-- 데이터 수집 기준 시간 설정 -->
  <q-card flat bordered class="q-mt-md">
    <q-card-section class="bg-primary text-white">
      <div class="text-subtitle2">
        <q-icon name="refresh" color="white" class="q-mr-xs" size="20px" />
        {{ t('데이터 수집 기준') }}
      </div>
      <div class="text-caption">{{ t('데이터 수집 주기를 선택해 주세요.') }}</div>
    </q-card-section>
    <q-card-section class="row items-center q-my-sm">
      <q-input
        v-model.number="config.dataInterval"
        type="number"
        dense
        outlined
        style="width: 120px"
        :rules="[$rules.natural, $rules.required]"
      >
      </q-input>
      <div class="q-ml-sm">{{ t('초') }}</div>
    </q-card-section>
  </q-card>

  <!-- 파라미터 세트 목록 -->
  <div class="q-mt-md">
    <div class="text-subtitle2 q-mb-sm row items-center">
      {{ t('파라미터 선택') }}
      <q-btn flat round dense color="primary" icon="add" @click="addParameterSet" class="q-ml-sm" />
    </div>

    <div v-for="(parameter, index) in config.parameters" :key="parameter" class="parameter-set q-mb-lg">
      <div class="row q-col-gutter-sm items-center">
        <!-- 1. 숫자 영역 -->
        <div class="col-auto">
          <div class="text-subtitle2">#{{ index + 1 }}</div>
        </div>

        <!-- 2. Cascader -->
        <div class="col-5">
          <cascader
            :model-value="parameterIdToTreeIdMap.get(parameter.id)"
            @update:model-value="(treeId) => onParamChanged(treeId, index)"
            class="input-required"
            :label="$t('설비/파라미터')"
            :titles="[$t('설비'), $t('파라미터')]"
            :items="parameterToolTree"
            item-value="treeId"
            item-text="name"
            clearable
            :valid-depth="2"
            :rules="[$rules.required]"
          />
        </div>

        <!-- 3. 강조색 Input -->
        <div class="col-3">
          <q-input v-model="parameter.valueHighlightColor" :label="$t('값 강조색')" dense outlined />
        </div>

        <!-- 4. 삭제 버튼 -->
        <div class="col-auto">
          <q-btn flat round dense color="negative" icon="close" @click="removeParameterSet(index)" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, reactive } from 'vue'
import { useI18n } from 'vue-i18n'
import { widgetSettingProps, useWidgetSettingConfig } from 'src/common/module/widgetCommon'
import ParameterService from 'src/services/modeling/ParameterService'
import Cascader from 'components/cascader/Cascader.vue'
import { pt } from 'src/plugins/i18n'

defineProps(widgetSettingProps)

const { t } = useI18n()

const config = useWidgetSettingConfig({
  dataInterval: 30, // 기본값 30초
  parameters: [], // 파라미터 세트 배열
})

const parameterToolTree = ref([])
const parameterIdToTreeIdMap = reactive(new Map()) // key: set 객체, value: treeId

const flattenTree = computed(() => {
  const flatTree = (items) => {
    return items.flatMap((item) => [item, ...(item.children ? flatTree(item.children) : [])])
  }
  return parameterToolTree.value.flatMap((item) => [item, ...(item.children ? flatTree(item.children) : [])])
})

const loadTreeOfToolsAndParameters = async () => {
  const toolParameterTree = await ParameterService.getParameterToolTree()

  parameterToolTree.value = toolParameterTree.map((tool) => {
    return {
      ...tool,
      children: tool.children.map((parameter) => ({
        ...parameter,
        name: pt(parameter.name),
      })),
    }
  })
}

// 파라미터 세트 추가
const addParameterSet = () => {
  config.value.parameters = [...config.value.parameters, { id: null, valueHighlightColor: null }]
}

// 파라미터 정보 제거
// parameterIdToTreeIdMap, config.parameters 모두에서 제거해야함
const removeParameterSet = (index) => {
  const removeTarget = config.value.parameters[index]
  if (removeTarget?.id) {
    const sameIdCount = config.value.parameters.filter((param) => param.id === removeTarget.id).length
    // 같은 id를 가진 파라미터가 하나만 남아있다면 맵에서 제거
    if (sameIdCount === 1) {
      parameterIdToTreeIdMap.delete(removeTarget.id)
    }
  }
  config.value.parameters.splice(index, 1)
  config.value.parameters = [...config.value.parameters]
}

const onParamChanged = (treeId, index) => {
  const found = flattenTree.value.find((v) => v.treeId === treeId)
  if (found?.type.toUpperCase() !== 'PARAMETER') {
    return
  }
  parameterIdToTreeIdMap.set(found.id, treeId)
  config.value.parameters[index].id = found.id
}

onMounted(async () => {
  await loadTreeOfToolsAndParameters()
  config.value.parameters.forEach((parameter) => {
    const found = flattenTree.value.find((v) => v.id === parameter.id)
    if (found?.treeId) {
      parameterIdToTreeIdMap.set(parameter.id, found.treeId)
    }
  })
})
</script>

<style scoped>
.parameter-set {
  position: relative;
  border: 1px dashed rgba(0, 0, 0, 0.12);
  border-radius: 4px;
  padding: 26px 16px 10px 24px;
  margin-bottom: 8px;
}
</style>
