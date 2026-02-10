<template>
  <q-card flat bordered>
    <q-card-section class="bg-primary text-white">
      <div class="text-subtitle2">{{ $t('생산율 위젯 설정') }}</div>
      <div class="text-caption">{{ $t('현재값 파라미터와 최대값을 설정해 주세요.') }}</div>
    </q-card-section>

    <q-card-section class="q-pt-md">
      <!-- 데이터 수집 주기 -->
      <div class="q-mb-md">
        <div class="field-label q-mb-sm">
          <q-icon name="refresh" color="primary" class="q-mr-xs" size="18px" />
          {{ $t('데이터 수집 주기') }}
        </div>
        <div class="row items-center">
          <q-input
            v-model.number="config.dataInterval"
            type="number"
            dense
            outlined
            style="width: 120px"
            :rules="[$rules.natural, $rules.required]"
          />
          <div class="q-ml-sm">{{ $t('초') }}</div>
        </div>
      </div>

      <!-- 현재값 파라미터 선택 -->
      <div class="q-mb-md">
        <div class="field-label q-mb-sm">
          <q-icon name="format_list_numbered" color="primary" class="q-mr-xs" size="18px" />
          {{ $t('현재값 (파라미터 선택)') }}
        </div>
        <cascader
          :model-value="parameterIdToTreeIdMap.get(config.parameterId)"
          @update:model-value="onParamChanged"
          :titles="[$t('설비'), $t('파라미터')]"
          :items="parameterToolTree"
          item-value="treeId"
          item-text="name"
          clearable
          :valid-depth="2"
          :rules="[$rules.required]"
        />
      </div>

      <!-- 최대값 입력 -->
      <div class="q-mb-md">
        <div class="field-label q-mb-sm">
          <q-icon name="trending_up" color="primary" class="q-mr-xs" size="18px" />
          {{ $t('최대값') }}
        </div>
        <q-input v-model.number="config.maxValue" type="number" outlined :rules="[$rules.required, $rules.natural]" />
      </div>
    </q-card-section>
  </q-card>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useWidgetSettingConfig, widgetSettingProps } from 'src/common/module/widgetCommon'
import ParameterService from 'src/services/modeling/ParameterService'
import Cascader from 'components/cascader/Cascader.vue'
import { pt } from 'src/plugins/i18n'

defineProps(widgetSettingProps)

const config = useWidgetSettingConfig({
  dataInterval: 30,
  parameterId: null,
  maxValue: null,
})

const parameterToolTree = ref([])
const parameterIdToTreeIdMap = reactive(new Map())

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

// 파라미터 변경 시 호출
const onParamChanged = (treeId) => {
  const found = flattenTree.value.find((v) => v.treeId === treeId)
  if (found?.type.toUpperCase() !== 'PARAMETER') {
    return
  }
  parameterIdToTreeIdMap.set(found.id, treeId)
  config.value.parameterId = found.id
}

onMounted(async () => {
  await loadTreeOfToolsAndParameters()

  // 기존 파라미터 매핑 복원
  if (config.value.parameterId) {
    const found = flattenTree.value.find((v) => v.id === config.value.parameterId)
    if (found?.treeId) {
      parameterIdToTreeIdMap.set(config.value.parameterId, found.treeId)
    }
  }
})
</script>

<style scoped>
.field-label {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  display: flex;
  align-items: center;
}
</style>
