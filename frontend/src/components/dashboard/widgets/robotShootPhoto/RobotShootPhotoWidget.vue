<template>
  <q-card v-if="isExistImage" class="column col" bordered flat>
    <q-img :src="`/img/ext/photo/${latestImage.imageValue}`" class="col" fit="contain" @error="onImageError" />
    <q-separator />
    <q-card-actions align="center">
      <q-icon name="event" size="sm" class="q-mr-md"></q-icon>
      <span class="text-subtitle2">{{ latestImage && formatDateTime(latestImage.traceAt) }}</span>
    </q-card-actions>
  </q-card>
  <div v-else class="col no-data">
    <div style="text-align: center" class="col">
      <q-icon class="flex-center" name="hide_image" size="50px" color="primary" />
      <div class="text-body-2 q-my-sm">
        {{ $t('조회된 사진이 없습니다.') }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useWidgetRefresh, widgetProps } from 'src/common/module/widgetCommon'
import ParameterDataService from 'src/services/parameterData/ParameterDataService'
import { formatDateTime } from 'src/common/utils'

defineProps(widgetProps)

const latestImage = ref()
const isExistImage = ref(false)
const refresh = async (config) => {
  const { toolId } = config
  await onSearch(toolId)
}

const onSearch = async (toolId) => {
  latestImage.value = await loadParameterPhotoData(toolId)
  isExistImage.value = latestImage.value.imageValue !== null
}

const loadParameterPhotoData = (toolId) => {
  return ParameterDataService.getImageParameterData(toolId, new Date())
}

const onImageError = () => {
  isExistImage.value = false
}

useWidgetRefresh(refresh)
</script>

<style scoped>
.q-img :deep(.q-img__container) {
  padding-left: 10px;
  padding-right: 10px;
}

.no-data {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}
</style>
