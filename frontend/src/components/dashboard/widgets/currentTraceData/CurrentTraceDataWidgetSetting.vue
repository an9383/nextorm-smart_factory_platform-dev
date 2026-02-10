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

    <!-- <div v-for="(set, index) in config.parameterSets" :key="index" class="parameter-set q-mb-lg"> -->
    <div v-for="(set, index) in config.parameterSets" :key="set" class="parameter-set q-mb-lg">
      <div class="row justify-between items-center q-mb-sm">
        <div class="text-subtitle2">#{{ index + 1 }}</div>
        <q-btn flat round dense color="negative" icon="close" @click="removeParameterSet(index)" />
      </div>
      <div class="row q-col-gutter-sm">
        <cascader
          :model-value="treeIdMap.get(set)"
          @update:model-value="(val) => onParamChanged(val, set)"
          class="input-required col-8 q-pr-md q-mt-md"
          :label="$t('설비/파라미터')"
          :titles="[$t('설비'), $t('파라미터')]"
          :items="parameterToolTree"
          item-value="treeId"
          item-text="name"
          clearable
          :valid-depth="2"
          :rules="[$rules.required]"
        />
        <q-input
          v-model="set.icon"
          :label="$t('아이콘')"
          dense
          outlined
          class="input-required col-4 q-pr-md q-mt-md"
          :rules="[$rules.required]"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { widgetSettingProps, useWidgetSettingConfig } from '/src/common/module/widgetCommon'
import ParameterService from 'src/services/modeling/ParameterService'
import Cascader from 'components/cascader/Cascader.vue'
import { pt } from 'src/plugins/i18n'

defineProps(widgetSettingProps)

const { t } = useI18n()

const config = useWidgetSettingConfig({
  dataInterval: 30, // 기본값 30초
  parameterSets: [], // 파라미터 세트 배열
})

const parameterToolTree = ref([])
const treeIdMap = ref(new Map()) // key: set 객체, value: treeId

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
  const newSet = { id: null, icon: '' }
  config.value.parameterSets.push(newSet)
  treeIdMap.value.set(newSet, null)
}

// 파라미터 세트 제거
const removeParameterSet = (index) => {
  const removed = config.value.parameterSets[index]
  treeIdMap.value.delete(removed)
  config.value.parameterSets.splice(index, 1)
}

const flattenTree = (items) => {
  return items.flatMap((item) => [item, ...(item.children ? flattenTree(item.children) : [])])
}

const onParamChanged = (treeId, param) => {
  const flatTree = flattenTree(parameterToolTree.value)
  const found = flatTree.find((v) => v.treeId === treeId)

  param.id = found?.id
  treeIdMap.value.set(param, treeId)
}

onMounted(async () => {
  await loadTreeOfToolsAndParameters()
  const flatTree = flattenTree(parameterToolTree.value)
  config.value.parameterSets.forEach((set) => {
    const found = flatTree.find((v) => v.id === set.id)
    if (found?.treeId) {
      treeIdMap.value.set(set, found.treeId)
    }
  })
})
</script>

<style scoped>
.parameter-set {
  position: relative;
  padding: 1rem;
  border: 1px dashed rgba(0, 0, 0, 0.12);
  border-radius: 4px;
}
</style>
