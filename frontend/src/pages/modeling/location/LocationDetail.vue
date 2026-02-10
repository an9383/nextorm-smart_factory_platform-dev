<template>
  <q-card-section>
    <div class="title_wrap">
      <h3>{{ $t('위치') }} {{ isCreateMode ? $t('등록') : $t('수정') }}</h3>
      <span class="field-required-indicate">{{ $t('필수 입력 필드') }}</span>
    </div>
    <q-form ref="form" lazy-validation>
      <div class="row">
        <cascader
          v-if="isCascaderShow"
          v-model="formData.parentId"
          item-value="id"
          item-text="name"
          :label="$t('위치')"
          :items="cascadeLocationTreeItems"
          :rules="[$rules.required]"
          :valid-depth="locationDepth.depth"
          :valid-depth-msg="locationDepth.msg"
          disable
          class="input-required col"
        />
      </div>
      <div class="row">
        <filterable-select
          v-model="formData.type"
          :label="$t('타입')"
          :options="locationTypes"
          option-value="value"
          option-label="text"
          :rules="[$rules.required]"
          disable
          use-chips
          class="input-required col-6 q-pr-sm"
        >
          <template #selected-item="props">
            {{ locationTypes.find((v) => v.value === props.opt).text }}
          </template>
        </filterable-select>

        <q-input
          v-model="formData.name"
          :label="$t('이름')"
          :rules="[$rules.required]"
          class="input-required col-6 q-pl-sm"
        />
      </div>
      <div class="row">
        <q-input v-model="formData.description" :label="$t('설명')" :rules="[$rules.maxLength(300)]" class="col" />
      </div>
      <div class="row items-center" v-if="formData.type === LOCATION_TYPE.LINE">
        <div class="col-4">
          <q-input v-model="formData.latitude" :label="$t('위도')" :rules="[$rules.realNumber]" />
        </div>
        <div class="col-4 q-ml-md">
          <q-input v-model="formData.longitude" :label="$t('경도')" :rules="[$rules.realNumber]" />
        </div>
        <div class="col-3 q-ml-md">
          <q-btn @click="showMapSearch" class="map_btn with_icon_btn sBtn">{{ $t('지도 검색') }}</q-btn>
          <map-search
            ref="mapSearch"
            v-if="isShowMapSearch"
            :zoom="16"
            :center="[formData.latitude ?? 37.49366, formData.longitude ?? 127.03229]"
            @close="onCloseMapSearch"
          />
        </div>
      </div>
    </q-form>
    <div class="row justify-end footer_btnWrap">
      <q-btn v-permission:location="isCreateMode ? 'create' : 'update'" class="sBtn" @click="saveLocation">
        {{ $t('저장') }}
      </q-btn>
    </div>
  </q-card-section>
</template>

<script setup>
import MapSearch from 'components/map/MapSearch.vue'
import Cascader from 'components/cascader/Cascader.vue'
import { getLocationTypes, LOCATION_TYPE } from 'pages/modeling/location/location'
import { computed, onBeforeMount, ref, toRefs, watch } from 'vue'
import LocationService from 'src/services/modeling/LocationService'
import _ from 'lodash'
import useUI from 'src/common/module/ui'
import { useI18n } from 'vue-i18n'

const props = defineProps({
  locationId: {
    type: Number,
    required: true,
    default: null,
  },
  /**
   * @description 생성할 Location의 타입
   */
  createLocationType: {
    type: String,
    require: false,
  },
  locationTreeItems: {
    type: Array,
    default: () => [],
  },
})

const emit = defineEmits(['persisted']) // 데이터 반영(저장 or 수정) 성공 이벤트

const { locationTreeItems: cascadeLocationTreeItems } = toRefs(props)

const ui = useUI()
const { t } = useI18n()

const form = ref(null)
const formData = ref({
  parentId: null,
  type: props.createLocationType,
  name: null,
  longitude: null,
  latitude: null,
  description: null,
})

const location = ref({})

const isShowMapSearch = ref(false)
const locationTypes = getLocationTypes(t)

const isCascaderShow = computed(() => formData.value?.type !== LOCATION_TYPE.SITE)
const isCreateMode = computed(() => props.createLocationType)

const saveLocation = async () => {
  const success = await form.value.validate()
  if (!success) {
    ui.notify.warning(t('유효하지 않은 입력값이 있습니다.'))
    return
  }
  try {
    ui.loading.show()
    isCreateMode.value
      ? await LocationService.createLocation(formData.value)
      : await LocationService.modifyLocation(location.value.id, {
          name: formData.value.name,
          description: formData.value.description,
          seq: formData.value.seq,
          latitude: formData.value.latitude,
          longitude: formData.value.longitude,
        })
    ui.notify.success(t('저장되었습니다.'))
  } finally {
    ui.loading.hide()
  }
  emit('persisted')
}

const locationDepth = computed(() => {
  let result = { depth: 3, msg: '' }
  switch (location.value.type) {
    case LOCATION_TYPE.FAB:
      result = {
        depth: 1,
        msg: t('공장은 사이트 하위에만 추가할 수 있습니다.'),
      }
      break
    case LOCATION_TYPE.LINE:
      result = {
        depth: 2,
        msg: t('라인은 공장 하위에만 추가할 수 있습니다.'),
      }
      break
  }
  return result
})

const onCloseMapSearch = (value) => {
  if (!_.isUndefined(value) && value) {
    formData.value = {
      ...formData.value,
      latitude: value[0],
      longitude: value[1],
    }
  }
  isShowMapSearch.value = false
}

const showMapSearch = () => {
  isShowMapSearch.value = true
}

const setCreateModeFormData = (location, createLocationType) => {
  const parentId = createLocationType !== LOCATION_TYPE.SITE ? location.id : null
  formData.value = {
    parentId,
    type: createLocationType,
    name: null,
    description: null,
    latitude: null,
    longitude: null,
  }
}

const fetchLocationAndSetFormData = async (locationId) => {
  location.value = await LocationService.getLocation(locationId)
  const { parent } = location.value

  if (isCreateMode.value) {
    setCreateModeFormData(location.value, props.createLocationType)
    return
  }

  formData.value = {
    parentId: parent?.id,
    type: location.value.type,
    name: location.value.name,
    description: location.value.description,
    latitude: location.value.latitude,
    longitude: location.value.longitude,
  }
}

watch(
  () => props.locationId,
  async (locationId) => {
    if (locationId) {
      await fetchLocationAndSetFormData(locationId)
    }
  },
)

watch(
  () => props.createLocationType,
  async (createLocationType) => {
    if (isCreateMode.value) {
      setCreateModeFormData(location.value, createLocationType)
    }
  },
)

onBeforeMount(async () => {
  if (props.locationId) {
    await fetchLocationAndSetFormData(props.locationId)
  }
})
</script>
