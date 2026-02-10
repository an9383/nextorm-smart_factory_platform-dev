<template>
  <q-page class="q-pa-sm">
    <div class="row full-height">
      <q-card class="col-4">
        <location-tree
          :tree-item="locationTreeItems"
          @nodeSelect="handleNodeSelect"
          @contextMenuClick="handleContextMenuClick"
        />
      </q-card>
      <div class="col-8 q-pl-md">
        <q-card>
          <location-detail
            v-if="selectedLocation"
            :location-id="selectedLocation.id"
            :create-location-type="createLocationType"
            :location-tree-items="locationTreeItems"
            @persisted="handleLocationPersisted"
          />
        </q-card>
        <q-card class="q-mt-sm">
          <location-sub-list
            v-if="isSubListShow"
            :location-id="selectedLocation.id"
            @add-button-click="handleSubListAddButtonClick"
            @delete-button-click="handleSubListDeleteButtonClick"
          />
        </q-card>
      </div>
    </div>
  </q-page>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import _ from 'lodash'
import useUI from 'src/common/module/ui'
import LocationService from 'src/services/modeling/LocationService.js'
import { CONTEXT_MENU_ACTION_TYPE, LOCATION_TYPE } from 'pages/modeling/location/location'
import LocationSubList from 'pages/modeling/location/LocationSubList.vue'
import LocationTree from 'pages/modeling/location/LocationTree.vue'
import LocationDetail from 'pages/modeling/location/LocationDetail.vue'

const ui = useUI()
const { t } = useI18n()

const locationTreeItems = ref([])

const selectedLocation = ref(null)
const createLocationType = ref(null)

const isSubListShow = computed(() => {
  const { type } = selectedLocation.value || {}
  if (!type) {
    return false
  }
  const isModifyMode = !createLocationType.value
  return isModifyMode && type !== LOCATION_TYPE.LINE
})

const loadAndAssignLocationTree = async () => {
  const locationTree = await LocationService.getLocationsTree()
  locationTreeItems.value = _.cloneDeep(locationTree)
}

const initPageData = async () => {
  await loadAndAssignLocationTree()
  selectedLocation.value = null
  createLocationType.value = null
}

const handleLocationPersisted = async () => {
  await initPageData()
}

const deleteLocations = async (locationIds) => {
  ui.loading.show()
  try {
    if (locationIds.length === 1) {
      await LocationService.deleteLocation(locationIds[0])
    } else {
      await LocationService.bulkDelete(locationIds)
    }
    ui.notify.success(t('삭제 되었습니다.'))
  } catch (error) {
    error.response.data.extraData.datas.forEach((message) => {
      if (error.response.data.code === 'ERROR_CONSTRAINT_VIOLATION') {
        ui.notify.error(message.path)
      }
    })
  }
  await initPageData()
  ui.loading.hide()
}

const deleteLocationsProcess = async (locationIds) => {
  ui.confirm(
    t('위치 삭제'),
    t('하위의 모든 위치가 삭제됩니다. 해당 위치를 삭제하시겠습니까?'),
    t('삭제'),
    t('취소'),
  ).onOk(async () => await deleteLocations(locationIds))
}

const handleSubListDeleteButtonClick = async (locationIds) => {
  await deleteLocationsProcess(locationIds)
}

const handleSubListAddButtonClick = () => {
  const { type } = selectedLocation.value
  createLocationType.value = type === LOCATION_TYPE.SITE ? LOCATION_TYPE.FAB : LOCATION_TYPE.LINE
}

const handleNodeSelect = async (node) => {
  selectedLocation.value = node
  createLocationType.value = null
}

const handleContextMenuClick = async (event) => {
  const { actionType: contextActionType, target } = event.context
  const { node } = event

  if (contextActionType === CONTEXT_MENU_ACTION_TYPE.DELETE) {
    await deleteLocationsProcess([node.id])
    return
  }
  selectedLocation.value = node
  createLocationType.value = target
}

onMounted(async () => {
  await initPageData()
})
</script>
