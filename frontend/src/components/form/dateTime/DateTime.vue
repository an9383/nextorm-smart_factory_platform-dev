<template>
  <q-input filled v-model="dateTimeString" stack-label :label="label" @change="handleInputDateChange">
    <template v-slot:prepend>
      <q-icon name="event" class="cursor-pointer">
        <q-popup-proxy cover transition-show="scale" transition-hide="scale">
          <q-date
            v-model="dateTimeString"
            @update:model-value="handleDateTimeChange"
            :mask="DATE_MINUTES_FORMAT"
            today-btn
          >
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
          <q-time
            v-model="dateTimeString"
            @update:model-value="handleDateTimeChange"
            :mask="DATE_MINUTES_FORMAT"
            today-btn
          >
            <div class="row items-center justify-end">
              <q-btn v-close-popup :label="$t('닫기')" color="primary" flat />
            </div>
          </q-time>
        </q-popup-proxy>
      </q-icon>
    </template>
  </q-input>
</template>

<script setup>
import { DATE_MINUTES_FORMAT } from 'src/common/constant/format'
import { formatDateMinutes } from 'src/common/utils'
import { ref, watch } from 'vue'

defineProps({
  label: {
    type: String,
    required: false,
  },
})

const dateTime = defineModel({
  type: Date,
  required: false,
})

if (!dateTime.value) {
  const currentDate = new Date()
  currentDate.setSeconds(0)
  currentDate.setMilliseconds(0)
  dateTime.value = currentDate
}

if (dateTime.value) {
  const copyDate = new Date(dateTime.value)
  copyDate.setSeconds(0)
  copyDate.setMilliseconds(0)
  dateTime.value = copyDate
}

const dateTimeString = ref(formatDateMinutes(dateTime.value))
const inputDateString = ref(null)

watch(dateTime, () => {
  dateTimeString.value = formatDateMinutes(dateTime.value)
})

const handleInputDateChange = (value) => {
  const date = new Date(value)
  if (date.toString() !== 'Invalid Date') {
    dateTime.value = date
  } else {
    dateTimeString.value = formatDateMinutes(dateTime.value)
  }
  inputDateString.value = value
}

const handleDateTimeChange = (value) => {
  if (!value) {
    dateTimeString.value = formatDateMinutes(dateTime.value)
    return false
  }
  dateTime.value = new Date(value)
}
</script>

<style scoped></style>
