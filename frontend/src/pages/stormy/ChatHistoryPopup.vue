<template>
  <q-dialog v-model="modelValue" persistent>
    <q-card style="min-width: 700px; min-height: 800px">
      <q-card-section>
        <div class="text-h6">{{ $t('전체 채팅 이력') }}</div>
      </q-card-section>
      <q-card-section class="q-pt-none" style="height: 700px">
        <simple-table
          class="my-sticky-header-table"
          style="height: 100%"
          flat
          bordered
          :rows="chatSessions"
          :columns="columns"
          row-key="name"
          :filter="filter"
          :pagination="{ rowsPerPage: 0 }"
          :rows-per-page-options="[0]"
          @row-click="onClickRow"
        >
          <template v-slot:top-right>
            <q-input dense debounce="300" v-model="filter" :placeholder="$t('검색')" class="input-field">
              <template v-slot:append>
                <q-icon name="search" />
              </template>
            </q-input>
          </template>
          <template v-slot:body-cell-isFavorite="props">
            <q-td :props="props">
              <div class="my-table-details">
                <q-icon v-if="props.value == 'true'" name="star" size="20px" color="orange"></q-icon>
                <q-icon v-else name="star_outline" size="20px"></q-icon>
              </div>
            </q-td>
          </template>
        </simple-table>
      </q-card-section>

      <q-card-actions align="right" class="text-primary">
        <q-btn flat :label="$t('취소')" v-close-popup />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>
<script setup>
import { ref, defineModel, onMounted } from 'vue'
import { date } from 'quasar'
import { t } from 'src/plugins/i18n'
import ChatService from '/src/services/ChatService'

const modelValue = defineModel('modelValue', {
  type: Boolean,
  required: true,
  default: false,
})

const filter = ref('')
const chatSessions = ref([])

const emit = defineEmits(['select'])
const columns = [
  {
    name: 'title',
    required: true,
    label: t('제목'),
    align: 'left',
    field: (row) => row.title,
    format: (val) => `${val}`,
    sortable: true,
  },
  {
    name: 'updateAt',
    required: true,
    label: t('수정일자'),
    align: 'left',
    field: (row) => row.updateAt,
    format: (val) => date.formatDate(new Date(val), 'YYYY-MM-DD HH:mm:ss'),
    sortable: true,
  },
  {
    name: 'careateAt',
    required: true,
    label: t('생성일자'),
    align: 'left',
    field: (row) => row.createAt,
    format: (val) => date.formatDate(new Date(val), 'YYYY-MM-DD HH:mm:ss'),
    sortable: true,
  },
  {
    name: 'isFavorite',
    required: true,
    label: t('즐겨찾기'),
    align: 'left',
    field: (row) => row.isFavorite,
    format: (val) => `${val}`,
    sortable: true,
  },
]

onMounted(() => {
  loadAllChat()
})

const loadAllChat = async () => {
  const sessions = await ChatService.getChatSessions()
  chatSessions.value = sessions
}

const onClickRow = (e, row) => {
  emit('select', row)
  close()
}

const close = () => {
  modelValue.value = false
}
</script>
<style lang="sass">
.my-sticky-header-table
  /* height or max-height is important */
  height: 310px

  .q-table__top,
  .q-table__bottom,
  thead tr:first-child th
    /* bg color is important for th; just specify one */
    // background-color: #00b4ff
    background-color: white

  thead tr th
    position: sticky
    z-index: 1
  thead tr:first-child th
    top: 0

  /* this is when the loading indicator appears */
  &.q-table--loading thead tr:last-child th
    /* height of all previous header rows */
    top: 48px

  /* prevent scrolling behind sticky top row on focus */
  tbody
    /* height of all previous header rows */
    scroll-margin-top: 48px
</style>
