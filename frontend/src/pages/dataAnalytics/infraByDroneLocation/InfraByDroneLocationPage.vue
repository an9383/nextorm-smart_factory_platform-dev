<template>
  <q-page class="q-pa-sm">
    <q-card class="cust_subPage">
      <q-card-section class="row search_section_wrap">
        <q-form ref="form">
          <q-item class="col-2 search_left">
            <q-item-section>
              <filterable-select
                outlined
                v-model="tool"
                :options="toolOptions"
                option-value="id"
                option-label="name"
                :label="$t('설비')"
              >
                <template v-slot:prepend>
                  <q-icon name="construction" />
                </template>
              </filterable-select>
            </q-item-section>
          </q-item>
        </q-form>
        <q-item class="col-2 search_right">
          <q-item-section>
            <q-btn @click="onSearch" class="search_btn with_icon_btn sBtn">{{ $t('조회') }}</q-btn>
          </q-item-section>
        </q-item>
      </q-card-section>
      <q-card-section class="column col">
        <infra-by-location-map :latitude="latitude" :longitude="longitude" />
      </q-card-section>
    </q-card>
  </q-page>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import useUI from 'src/common/module/ui'
import InfraByLocationMap from './InfraByLocationMap.vue'
import toolService from 'src/services/modeling/ToolService'

const { t } = useI18n()
const ui = useUI()

const tool = ref(null)
const toolOptions = ref([])
const form = ref(null)
const latitude = ref(35.84644)
const longitude = ref(128.43996)

onMounted(async () => {
  await getTools()
  if (toolOptions.value.length > 0) {
    //설비 목록이 있는 경우 첫번째 설비로 default 조회
    tool.value = toolOptions.value[0]
    await onSearch()
  }
})

const getTools = async () => {
  toolOptions.value = await toolService.getTools()
}

const onSearch = async () => {
  const success = await form.value.validate()
  if (success) {
    // removeLayer()
    latitude.value = tool.value.location.latitude
    longitude.value = tool.value.location.longitude
  } else {
    ui.notify.warning(t('유효하지 않은 입력값이 있습니다.'))
  }
}
</script>

<style scoped></style>
