<template>
  <q-dialog v-model="show" persistent>
    <q-card class="dialog-container">
      <q-card-section class="dialog-title text-white">
        <div class="title">{{ $t('저수량') }} {{ $t(status) }}</div>
      </q-card-section>

      <q-card-section class="dialog-contents">
        <q-form ref="form">
          <!-- 위치 선택 -->
          <div class="row">
            <cascader
              v-model="selectedLocation"
              :label="$t('위치')"
              :titles="[$t('사업장'), $t('공장'), $t('라인')]"
              :items="locations"
              item-value="id"
              item-text="name"
              :valid-depth="3"
              class="input-required col-12"
              :disable="modify.id != null"
            />
          </div>

          <!-- 저수량 입력 -->
          <div class="row">
            <q-input
              v-model="modify.reservoirCapacity"
              :label="$t('저수량(백만㎥)')"
              :rules="[$rules.required, $rules.decimal(5), $rules.min(0), $rules.realNumber]"
              type="number"
              class="input-required col-3 q-pr-sm inputNum"
            />
            <q-input
              v-model="modify.rainFall"
              :label="$t('강수량(mm)')"
              :rules="[$rules.required, $rules.decimal(1), $rules.min(0), $rules.realNumber]"
              type="number"
              class="input-required col-3 q-pr-sm inputNum"
            />
            <!-- 일시 선택 -->
            <q-input
              v-model="selectedDate"
              lazy-rules
              :rules="[$rules.required]"
              :label="$t('일')"
              class="input-required col-6"
            >
              <template v-slot:prepend>
                <q-icon name="event" class="cursor-pointer">
                  <q-popup-proxy cover transition-show="scale" transition-hide="scale">
                    <q-date v-model="selectedDate" mask="YYYY-MM-DD">
                      <div class="row items-center justify-end">
                        <q-btn v-close-popup :label="$t('닫기')" color="primary" flat />
                      </div>
                    </q-date>
                  </q-popup-proxy>
                </q-icon>
              </template>
              <template v-slot:after>
                <filterable-select
                  v-model="selectedHour"
                  :label="$t('시')"
                  :options="hourOptions"
                  option-value="value"
                  option-label="text"
                  class="input-required"
                />
              </template>
            </q-input>
          </div>

          <!-- 설명 입력 -->
          <div class="row">
            <q-input v-model="modify.description" :label="$t('설명')" class="col-12" />
          </div>
        </q-form>
      </q-card-section>

      <!-- <q-separator /> -->

      <!-- 닫기 및 저장 버튼 -->
      <q-card-actions class="justify-between">
        <span class="field-required-indicate">{{ $t('필수 입력 필드') }}</span>
        <div>
          <q-btn color="negative" :label="$t('닫기')" @click="onCancel" />
          <q-btn color="primary" :label="$t('저장')" @click="onOk" />
        </div>
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup>
import { defineEmits, defineExpose, ref } from 'vue'
import useUI from 'src/common/module/ui'
import { t } from '/src/plugins/i18n'
import { date } from 'quasar'
import { filterTreeItem } from 'src/common/treeUtil'
import Cascader from 'components/cascader/Cascader.vue'
import ReservoirCapacityService from 'src/services/waterQuality/ReservoirCapacityService'
import LocationService from 'src/services/modeling/LocationService'

const hourOptions = Array.from({ length: 24 }, (_, i) => ({
  text: i > 9 ? `${i}시` : `0${i}시`,
  value: i > 9 ? `${i}:00:00` : `0${i}:00:00`,
}))

const selectedDate = ref('')
const selectedHour = ref(hourOptions[0])

const ui = useUI()

const emit = defineEmits(['close'])
const show = ref(false)

const modify = ref({})

const status = ref(t('추가'))
const form = ref(null)

const locations = ref([])
const selectedLocation = ref(null)

const resetForm = async () => {
  modify.value = {
    id: null,
    locationId: null,
    reservoirCapacity: null,
    rainFall: null,
    date: null,
    description: null,
  }
  selectedDate.value = ''
  selectedHour.value = hourOptions[0]
  selectedLocation.value = null
}

const open = async (reservoirCapacityId) => {
  resetForm()
  if (reservoirCapacityId == null) {
    status.value = t('추가')
  } else {
    status.value = t('수정')
    await getData(reservoirCapacityId)
  }
  await loadLocations()
  show.value = true
}

defineExpose({ open })

const loadLocations = async () => {
  ui.loading.show()
  locations.value = await LocationService.getLocationsTree()
  ui.loading.hide()

  if (!modify.value.id) {
    const filterTreeItemByLine = filterTreeItem(locations.value, 'LINE', { id: 'type' })
    if (filterTreeItemByLine.length === 1) {
      selectedLocation.value = filterTreeItemByLine[0].id
    }
  }
}

const onOk = async () => {
  const success = await form.value.validate()
  if (success) {
    modify.value.locationId = selectedLocation.value
    let tempDate = `${selectedDate.value} ${selectedHour.value.value}`
    modify.value.date = new Date(tempDate).toISOString()
    try {
      status.value === t('추가')
        ? await ReservoirCapacityService.createReservoirCapacity(modify.value)
        : await ReservoirCapacityService.modifyReservoirCapacity(modify.value.id, modify.value)
      ui.notify.success(t('저장 되었습니다.'))
      show.value = false
      emit('close', 'ok')
    } finally {
      ui.loading.hide()
    }
  } else {
    ui.notify.warning(t('유효하지 않은 입력값이 있습니다.'))
  }
}
const onCancel = () => {
  show.value = false
  resetForm()
  emit('close', 'cancel')
}

const getData = async (id) => {
  const result = await ReservoirCapacityService.getReservoirCapacity(id)
  if (result) {
    modify.value = result
    selectedLocation.value = modify.value.locationId
    let tempDate = date.formatDate(modify.value.date, 'YYYY-MM-DD HH:mm:ss')
    // eslint-disable-next-line no-unused-vars
    let formattedDate = date.formatDate(tempDate, 'YYYY-MM-DD')
    let formattedTime = date.formatDate(tempDate, 'HH:mm:ss')
    selectedHour.value = hourOptions.find((v) => v.value === formattedTime)
    selectedDate.value = formattedDate
  }
}

loadLocations()
</script>

<style scoped>
.dialog-container {
  width: 600px;
  max-width: 600px;
}
</style>
