<template>
  <div v-if="imageData" class="col column">
    <div class="col column">
      <q-img class="col" :src="imageURL" fit="contain" @click="openImageInWindow">
        <div class="absolute-bottom text-subtitle1 text-center img-caption">{{ imageTraceAt }}</div>
      </q-img>
    </div>
    <LeafletMap class="q-py-md col" ref="map" :zoom="zoom" :center="imageLocation">
      <l-marker :lat-lng="imageLocation" />
    </LeafletMap>
  </div>
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
import { ref, defineProps, computed, onMounted } from 'vue'
import { formatDateTime } from 'src/common/utils'
import { LMarker } from '@vue-leaflet/vue-leaflet'
import LeafletMap from 'components/map/LeafletMap.vue'
import ParameterDataService from 'src/services/parameterData/ParameterDataService'

const { toolId, datetime } = defineProps({
  toolId: { type: Number || String, required: true },
  datetime: { type: Date, required: true, default: new Date() },
})

const openImageInWindow = () => {
  window.open(imageURL.value, '_blank')
}

const imageData = ref()

const imageURL = computed(() => {
  return `/img/ext/photo/${imageData.value.imageValue}`
})
const imageTraceAt = computed(() => {
  return imageData.value?.traceAt ? formatDateTime(new Date(imageData.value.traceAt)) : ''
})
const imageLocation = computed(() => {
  return imageData.value?.latitudeValue ? [imageData.value.latitudeValue, imageData.value.longitudeValue] : undefined
})

const loadImageData = async () => {
  const searchDateTime = new Date(datetime).toISOString()
  const result = await ParameterDataService.getImageParameterData(toolId, searchDateTime)
  if (result) {
    imageData.value = result
  } else {
    imageData.value = null
  }
}

onMounted(() => {
  loadImageData()
})
</script>
<style lang="scss" scoped>
.img-caption {
  padding: 5px 0px;
  border-top: 1px solid var(--textColor);
  background-color: rgba(1, 29, 73, 0.4);
}
</style>
