<template>
  <q-table
    class="my-sticky-dynamic full-height full-width none_border"
    flat
    bordered
    row-key="id"
    :columns="columns"
    :title="parameterName"
    :rows="rows"
    @row-click="handleRowClick"
    virtual-scroll
    :virtual-scroll-item-size="48"
    :virtual-scroll-sticky-size-start="48"
    :pagination="{
      rowsPerPage: 0,
    }"
    :rows-per-page-options="[0]"
  />
</template>

<script setup>
import FaultHistoryService from 'src/services/parameterData/FaultHistoryService'
import { ref } from 'vue'
import { formatDateTime } from 'src/common/utils'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const columns = [
  { name: 'faultAt', align: 'center', label: t('FaultAt'), field: 'faultAt', format: (value) => formatDateTime(value) },
  { name: 'value', align: 'center', label: t('Value'), field: 'paramValue' },
  { name: 'uslOver', align: 'center', label: t('UslOver'), field: 'uslOver' },
  { name: 'uclOver', align: 'center', label: t('UclOver'), field: 'uclOver' },
  { name: 'lclOver', align: 'center', label: t('LclOver'), field: 'lclOver' },
  { name: 'lslOver', align: 'center', label: t('LslOver'), field: 'lslOver' },
  { name: 'usl', align: 'center', label: t('Usl'), field: 'usl' },
  { name: 'ucl', align: 'center', label: t('Ucl'), field: 'ucl' },
  { name: 'lcl', align: 'center', label: t('Lcl'), field: 'lcl' },
  { name: 'lsl', align: 'center', label: t('Lsl'), field: 'lsl' },
]

const props = defineProps({
  parameterId: {
    type: Number,
    required: true,
  },
  parameterName: {
    type: String,
    required: true,
  },
  from: {
    type: Date,
    required: true,
  },
  to: {
    type: Date,
    required: true,
  },
})

const emit = defineEmits({
  'row-click': (row) => row,
})

const rows = ref([])

if (props.parameterId) {
  FaultHistoryService.getFaultHistories(props.parameterId, props.from, props.to).then((faultHistories) => {
    rows.value = faultHistories
  })
}

const handleRowClick = (event, row) => {
  emit('row-click', row)
}
</script>

<style scoped lang="scss">
.my-sticky-dynamic:deep {
  .q-table__top,
  .q-table__bottom,
  thead tr:first-child th {
    /* bg color is important for th; just specify one */
    background-color: #00b4ff;
  }

  thead tr th {
    position: sticky;
    z-index: 1;
  }

  /* this will be the loading indicator */
  thead tr:last-child th {
    /* height of all previous header rows */
    top: 48px;
  }

  thead tr:first-child th {
    top: 0;
  }

  /* prevent scrolling behind sticky top row on focus */
  tbody {
    /* height of all previous header rows */
    scroll-margin-top: 48px;
  }
}
</style>
