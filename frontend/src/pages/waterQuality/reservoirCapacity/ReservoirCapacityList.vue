<template>
  <q-page class="q-pa-sm">
    <q-card class="cust_subPage">
      <q-card-section class="row search_section_wrap">
        <q-item class="col-2 search_left">
          <q-item-section>
            <q-input v-model="selectedRangeDateInput" lazy-rules :label="$t('조회 기간')" class="q-mr-sm" readonly>
              <template v-slot:prepend>
                <q-icon name="event" class="cursor-pointer">
                  <q-popup-proxy cover transition-show="scale" transition-hide="scale">
                    <q-date v-model="selectedRangeDate" mask="YYYY-MM-DD" range>
                      <div class="row items-center justify-end">
                        <q-btn v-close-popup :label="$t('닫기')" color="primary" flat />
                        <q-btn v-close-popup :label="$t('확인')" color="primary" flat @click="clickPeriodConfirm" />
                      </div>
                    </q-date>
                  </q-popup-proxy>
                </q-icon>
              </template>
            </q-input>
          </q-item-section>
        </q-item>
        <q-item class="col-4 search_right">
          <q-item-section>
            <q-btn v-permission:role.create @click="clickCreate" class="add_btn with_icon_btn sBtn">{{
              $t('추가')
            }}</q-btn>
            <q-btn
              v-permission:role.update
              class="edit_btn with_icon_btn sBtn"
              :disabled="selected == null || selected.length != 1"
              @click="clickModify"
              >{{ $t('수정') }}</q-btn
            >
            <q-btn
              v-permission:role.delete
              class="delete_btn with_icon_btn sBtn"
              :disabled="selected == null || selected.length == 0"
              @click="clickDelete"
              >{{ $t('삭제') }}</q-btn
            >
          </q-item-section>
        </q-item>
      </q-card-section>
      <q-table
        bordered
        :rows="rows"
        :columns="columns"
        row-key="id"
        selection="multiple"
        v-model:selected="selected"
        @row-click="clickRow"
        virtual-scroll
        :virtual-scroll-item-size="48"
        :virtual-scroll-sticky-size-start="48"
        class="sticky-dynamic"
        :pagination="{ rowsPerPage: 0 }"
        :rows-per-page-options="[0]"
      >
        <template v-slot:body-cell-date="{ value }">
          <td>{{ date.formatDate(value, 'YYYY-MM-DD HH:mm:ss') }}</td>
        </template>
      </q-table>
      <ReservoirCapacityModify ref="reservoirCapacityModify" @close="onCloseModify"></ReservoirCapacityModify>
    </q-card>
  </q-page>
</template>

<script setup>
import { computed, ref } from 'vue'
import { date } from 'quasar'
import _ from 'lodash'
import useUI from 'src/common/module/ui'
import { t } from '/src/plugins/i18n'
import ReservoirCapacityModify from './ReservoirCapacityModify.vue'
import ReservoirCapacityService from 'src/services/waterQuality/ReservoirCapacityService'

const reservoirCapacityModify = ref(null, null)
const ui = useUI()
const rows = ref([])
const selected = ref([])

const columns = ref([
  { name: 'id', align: 'left', label: t('ID'), field: 'id', sortable: true },
  { name: 'locationName', align: 'left', label: t('위치'), field: 'locationName', sortable: true },
  { name: 'reservoirCapacity', align: 'left', label: t('저수량(백만㎥)'), field: 'reservoirCapacity', sortable: true },
  { name: 'rainFall', align: 'left', label: t('강수량(mm)'), field: 'rainFall', sortable: true },
  { name: 'date', align: 'left', label: t('일시'), field: 'date', sortable: true },
  {
    name: 'description',
    align: 'left',
    label: t('설명'),
    field: 'description',
  },
])

const selectedRangeDate = ref({
  from: date.formatDate(date.subtractFromDate(new Date(), { days: 14 }), 'YYYY-MM-DD'),
  to: date.formatDate(new Date(), 'YYYY-MM-DD'),
})

const selectedRangeDateInput = computed(() => {
  if (selectedRangeDate.value == null) {
    return ''
  } else if (!_.isEmpty(selectedRangeDate.value.from) && !_.isEmpty(selectedRangeDate.value.to)) {
    return `${selectedRangeDate.value.from} ~ ${selectedRangeDate.value.to}`
  }
  return ''
})

const clickRow = (evt, row) => {
  if (selected.value.find((selectedRow) => selectedRow === row)) {
    selected.value = selected.value.filter((selectedRow) => selectedRow != row)
  } else {
    selected.value = [...selected.value, row]
  }
}
const clickPeriodConfirm = () => {
  if (selectedRangeDate.value.from && selectedRangeDate.value.to) {
    getData()
  }
}

const clickModify = () => {
  let data = null
  if (selected.value.length > 0) {
    data = selected.value[0]
  }
  selected.value = []
  reservoirCapacityModify.value.open(data.id)
}
const clickCreate = () => {
  reservoirCapacityModify.value.open(null)
  selected.value = []
}
const clickDelete = () => {
  ui.confirm(t('삭제'), t('선택된 항목을 삭제 하시겠습니까?'), t('삭제'), t('취소')).onOk(deleteReservoirCapacity)
}
const onCloseModify = (value) => {
  if (value == 'ok') {
    getData()
  }
}
const deleteReservoirCapacity = async () => {
  try {
    if (selected.value.length === 1) {
      await ReservoirCapacityService.deleteReservoirCapacity(selected.value[0].id)
    } else {
      await ReservoirCapacityService.deleteReservoirCapacities(selected.value.map((v) => v.id))
    }
    ui.notify.success(t('삭제 되었습니다.'))
    selected.value = []
  } catch (error) {
    error.response?.data.extraData.datas.forEach((message) => {
      if (error.response.data.code === 'ERROR_CONSTRAINT_VIOLATION') {
        ui.notify.error(message.path) //임시
      }
    })
  }
  getData()
}

const getData = async () => {
  ui.loading.show()
  rows.value = await ReservoirCapacityService.getReservoirCapacities(
    new Date(selectedRangeDate.value.from).toISOString(),
    new Date(selectedRangeDate.value.to).toISOString(),
  )
  ui.loading.hide()
}

getData()
</script>

<style scoped lang="scss">
.sticky-dynamic:deep {
  height: calc(100vh - 150px);

  .q-table__top,
  thead tr:first-child th {
    background-color: white;
  }

  thead tr th {
    position: sticky;
    z-index: 1;
  }

  thead tr:last-child th {
    top: 48px;
  }

  thead tr:first-child th {
    top: 0;
  }

  tbody {
    scroll-margin-top: 48px;
  }
}
</style>
