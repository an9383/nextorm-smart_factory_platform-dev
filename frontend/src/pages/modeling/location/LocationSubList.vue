<template>
  <q-card-section>
    <div class="title_wrap">
      <h3>
        {{ subLocationListTitle }}
      </h3>
      <div class="btn_wrap">
        <q-btn v-permission:location.create @click="handleAddLocationButtonClick" class="add_btn with_icon_btn sBtn">{{
          $t('추가')
        }}</q-btn>
        <q-btn
          v-permission:location.delete
          :disable="locationGrid.selected.length === 0"
          class="delete_btn with_icon_btn sBtn"
          @click="handleDeleteLocationButtonClick"
          >{{ $t('삭제') }}</q-btn
        >
      </div>
    </div>
    <simple-table
      v-model:selected="locationGrid.selected"
      :rows="locationGrid.items"
      :columns="locationGrid.columns"
      :loading="locationGrid.loading"
      :loading-label="$t('조회중 입니다...')"
      flat
      row-key="id"
      selection="multiple"
    >
      <template v-slot:body="props">
        <q-tr class="cursor-pointer" :props="props" @click="props.selected = !props.selected">
          <q-td v-for="col in props.cols" :key="col.name" :props="props">
            {{ col.name == 'type' ? locationTypes.find((v) => v.value === col.value).text : col.value }}
          </q-td>
        </q-tr>
      </template>
    </simple-table>
  </q-card-section>
</template>

<script setup>
import { computed, onBeforeMount, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import useUI from 'src/common/module/ui'
import LocationService from 'src/services/modeling/LocationService'
import { getLocationTypes, LOCATION_TYPE } from 'pages/modeling/location/location'

const { t } = useI18n()
const ui = useUI()

const props = defineProps({
  locationId: {
    type: Number,
    required: true,
  },
})

const emit = defineEmits({
  'delete-button-click': (locationIds) => locationIds,
  'add-button-click': () => true,
})

const location = ref()
const locationTypes = getLocationTypes(t)
const locationGrid = ref({
  columns: [
    { name: 'name', align: 'left', label: t('이름'), field: 'name', sortable: false },
    { name: 'type', align: 'left', label: t('타입'), field: 'type', sortable: false },
    { name: 'description', align: 'left', label: t('설명'), field: 'description', sortable: false },
  ],
  items: [],
  selected: [],
  loading: false,
})

const getLocationSubList = async (locationId) => {
  ui.loading.show()
  const [parent, children] = await Promise.all([
    LocationService.getLocation(locationId),
    LocationService.getLocationChildren(locationId),
  ])
  location.value = parent
  locationGrid.value.items = children
  locationGrid.value.selected = []
  ui.loading.hide()
}

const subLocationListTitle = computed(() => {
  if (!location.value) {
    return ''
  }

  let title = ''
  switch (location.value.type) {
    case LOCATION_TYPE.SITE:
      title = t('하위 공장 목록')
      break
    case LOCATION_TYPE.FAB:
      title = t('하위 라인 목록')
      break
  }
  return title
})

const handleAddLocationButtonClick = () => {
  emit('add-button-click')
}

const handleDeleteLocationButtonClick = () => {
  const locationIds = locationGrid.value.selected.map((v) => v.id)
  emit('delete-button-click', locationIds)
}

watch(
  () => props.locationId,
  async (locationId) => {
    await getLocationSubList(locationId)
  },
)

onBeforeMount(async () => {
  await getLocationSubList(props.locationId)
})
</script>
