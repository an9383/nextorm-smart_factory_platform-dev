<template>
  <simple-table
    class="col-8"
    bordered
    :rows="rows"
    :columns="columns"
    row-key="id"
    color="amber"
    selection="single"
    :pagination="{ rowsPerPage: 10 }"
    v-model:selected="selected"
  >
    <template v-slot:body="props">
      <q-tr class="cursor-pointer" :props="props" @click="props.selected = !props.selected">
        <q-td v-for="col in props.cols" :key="col.name" :props="props">
          {{ col.value }}
        </q-td>
      </q-tr>

      <q-tr v-show="props.selected" :props="props">
        <q-td colspan="100%">
          <div class="text-left">
            <q-table
              :title="t('설비 목록')"
              :class="'expanded-table'"
              :rows="props.row.tools"
              :columns="[
                { name: 'name', align: 'left', label: t('이름'), field: 'name' },
                { name: 'type', align: 'left', label: t('타입'), field: 'type' },
                { name: 'toolType', align: 'left', label: t('설비 타입'), field: 'toolType' },
              ]"
              hide-bottom
            />
          </div>
        </q-td>
      </q-tr>
    </template>
  </simple-table>
</template>

<script setup>
import { ref, toRefs, watch } from 'vue'
import { t } from 'src/plugins/i18n'
import { date } from 'quasar'

const columns = [
  { name: 'id', align: 'left', label: t('Id'), field: 'id' },
  { name: 'name', align: 'left', label: t('이름'), field: 'name', sortable: true },
  {
    name: 'toolCount',
    align: 'left',
    label: t('연결 설비 개수'),
    field: 'toolCount',
    format: (value) => `${value}`,
  },
  {
    name: 'isUseFailover',
    align: 'left',
    label: t('Failover 사용'),
    field: 'isUseFailover',
  },
  { name: 'systemIp', align: 'left', label: t('시스템 IP'), field: 'systemIp', sortable: true },
  {
    name: 'connectionTimeout',
    align: 'left',
    label: t('연결 확인 시간'),
    field: 'connectionTimeout',
    sortable: true,
  },
  { name: 'hosts', align: 'left', label: t('Hosts'), field: 'hosts', sortable: true },
  { name: 'createBy', align: 'left', label: t('생성자'), field: 'createBy' },
  {
    name: 'createAt',
    align: 'left',
    label: t('생성 일시'),
    field: 'createAt',
    format: (value) => {
      return date.formatDate(value, 'YYYY-MM-DD HH:mm:ss')
    },
  },
]

const props = defineProps({
  configs: {
    type: Array,
    default: () => [],
  },
})

const { configs } = toRefs(props)
const rows = ref([])
const selected = ref([])

const emit = defineEmits(['update:selected'])

watch(configs, (newConfigs) => {
  rows.value = newConfigs.map((config) => {
    return {
      ...config,
      toolCount: config.tools.length,
    }
  })
})

watch(selected, (newSelected) => {
  emit('update:selected', newSelected)
})
</script>

<style scoped>
.expanded-table:deep(.items-center) {
  padding-bottom: 0;
}
</style>
