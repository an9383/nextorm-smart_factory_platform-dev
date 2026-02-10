<template>
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
        <div class="col-6">
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
        <div class="col-4">
          <q-input v-model="parameter.toolDisplayName" :label="$t('설비 표시이름')" dense outlined />
        </div>

        <!-- 4. 삭제 버튼 -->
        <div class="col-auto">
          <q-btn flat round dense color="negative" icon="close" @click="removeParameterSet(index)" />
        </div>
      </div>
    </div>
  </div>
  <q-card flat bordered class="q-mt-md">
    <q-card-section class="bg-primary text-white">
      <div class="text-subtitle2 field-required">
        <q-icon name="date_range" color="white" class="q-mr-xs" size="20px" />
        {{ $t('조회기간') }}
      </div>
      <div class="text-caption">{{ $t('조회기간을 선택해 주세요.') }}</div>
    </q-card-section>
    <q-card-section class="q-ma-sm flex" style="gap: 20px">
      <filterable-select
        v-model="config.period"
        :placeholder="$t('조회기간')"
        :options="[
          { label: $t('30분'), value: 30 },
          { label: $t('1시간'), value: 60 },
          { label: $t('6시간'), value: 360 },
          { label: $t('12시간'), value: 720 },
          { label: $t('24시간'), value: 1440 },
          { label: $t('기간선택'), value: 'PERIOD' },
        ]"
        option-value="value"
        option-label="label"
        emit-value
        map-options
        filled
        dense
        lazy-rules
        :rules="[$rules.required]"
      />
      <div class="row hasCustom-input" v-if="config.period === 'PERIOD'">
        <div class="col">
          <q-input filled v-model="from" lazy-rules :rules="[$rules.required]">
            <template v-slot:prepend>
              <q-icon name="event" class="cursor-pointer">
                <q-popup-proxy cover transition-show="scale" transition-hide="scale">
                  <q-date v-model="from" mask="YYYY-MM-DD HH:mm:ss">
                    <div class="row items-center justify-end">
                      <q-btn v-close-popup :label="$t('닫기')" color="primary" flat />
                    </div>
                  </q-date>
                </q-popup-proxy>
              </q-icon>
            </template>

            <template v-slot:append>
              <q-icon name="access_time" class="cursor-pointer">
                <q-popup-proxy cover transition-show="scale" transition-hide="scale">
                  <q-time v-model="from" mask="YYYY-MM-DD HH:mm:ss" with-seconds>
                    <div class="row items-center justify-end">
                      <q-btn v-close-popup :label="$t('닫기')" color="primary" flat />
                    </div>
                  </q-time>
                </q-popup-proxy>
              </q-icon>
            </template>
          </q-input>
        </div>
        <div class="q-mx-sm row items-center">~</div>
        <div class="col">
          <q-input filled v-model="to" lazy-rules :rules="[$rules.required]">
            <template v-slot:prepend>
              <q-icon name="event" class="cursor-pointer">
                <q-popup-proxy cover transition-show="scale" transition-hide="scale">
                  <q-date v-model="to" mask="YYYY-MM-DD HH:mm:ss">
                    <div class="row items-center justify-end">
                      <q-btn v-close-popup :label="$t('닫기')" color="primary" flat />
                    </div>
                  </q-date>
                </q-popup-proxy>
              </q-icon>
            </template>

            <template v-slot:append>
              <q-icon name="access_time" class="cursor-pointer">
                <q-popup-proxy cover transition-show="scale" transition-hide="scale">
                  <q-time v-model="to" mask="YYYY-MM-DD HH:mm:ss" with-seconds>
                    <div class="row items-center justify-end">
                      <q-btn v-close-popup :label="$t('닫기')" color="primary" flat />
                    </div>
                  </q-time>
                </q-popup-proxy>
              </q-icon>
            </template>
          </q-input>
        </div>
      </div>
    </q-card-section>
  </q-card>
</template>

<script setup>
import { ref, watch, computed, onMounted, reactive } from 'vue'
import { date } from 'quasar'
import { useI18n } from 'vue-i18n'
import { widgetSettingProps, useWidgetSettingConfig } from '/src/common/module/widgetCommon'
import useUI from 'src/common/module/ui'
import ParameterService from 'src/services/modeling/ParameterService'
import { pt } from 'src/plugins/i18n'

defineProps(widgetSettingProps)

const { t } = useI18n()
const ui = useUI()

const config = useWidgetSettingConfig({ period: 30, parameters: [] })

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
  config.value.parameters = [...config.value.parameters, { id: null, toolDisplayName: null }]
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

watch(
  () => config.value.period,
  () => {
    //기간 변경 시 from, to 값 초기화
    config.value.from = undefined
    config.value.to = undefined
  },
)

const validateFromTo = () => {
  const fromDate = new Date(config.value.from)
  const toDate = new Date(config.value.to)
  if (fromDate > toDate) {
    const temp = config.value.from
    config.value.from = config.value.to
    config.value.to = temp
    ui.notify.warning(t('조회기간 시작일시는 종료일시보다 작아야 합니다.'))
  }
}
watch(() => config.value.from, validateFromTo)
watch(() => config.value.to, validateFromTo)
const from = computed({
  get() {
    //화면 표시는 YYYY-MM-DD HH:mm:ss 포맷으로
    return config.value.from ? date.formatDate(new Date(config.value.from), 'YYYY-MM-DD HH:mm:ss') : ''
  },
  set(value) {
    //실제 위젯 config에는 ISO 포맷으로 저장
    config.value.from = value ? new Date(value).toISOString() : undefined
  },
})
const to = computed({
  get() {
    //화면 표시는 YYYY-MM-DD HH:mm:ss 포맷으로
    return config.value.to ? date.formatDate(new Date(config.value.to), 'YYYY-MM-DD HH:mm:ss') : ''
  },
  set(value) {
    //실제 위젯 config에는 ISO 포맷으로 저장
    config.value.to = value ? new Date(value).toISOString() : undefined
  },
})
</script>
