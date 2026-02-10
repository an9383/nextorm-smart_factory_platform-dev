<template>
  <q-dialog v-model="modelValue" persistent>
    <q-card class="dialog-container">
      <q-card-section class="bg-secondary text-white">
        <div class="text-h6">{{ $t('APC 모델 리스트') }}</div>
      </q-card-section>

      <q-card-section class="col">
        <simple-table
          :pagination="{
            rowsPerPage: 0,
          }"
          hide-pagination
          bordered
          :rows="rows"
          :columns="columns"
          color="amber"
          row-key="apcModelId"
          selection="single"
          v-model:selected="selected"
          :filter="filter"
        >
          <template v-slot:top-right>
            <q-input dense v-model="filter" :placeholder="$t('검색')">
              <template v-slot:append>
                <q-icon name="search" />
              </template>
            </q-input>
          </template>
          <template v-slot:body="props">
            <q-tr class="cursor-pointer" :props="props" @click="props.selected = !props.selected">
              <q-td v-for="col in props.cols" :key="col.name" :props="props">
                <template v-if="col.name === 'description'">
                  <description-tooltip v-if="col.value" :description="col.value"></description-tooltip>
                  {{ col.value }}
                </template>
                <template v-else>
                  {{ col.value }}
                </template>
              </q-td>
            </q-tr>
          </template>
        </simple-table>
      </q-card-section>
      <q-separator />
      <q-card-actions class="float-right">
        <q-btn flat color="negative" :label="$t('닫기')" @click="handleCancelButton" />
        <q-btn flat color="primary" :label="$t('선택')" @click="handleSelectButton" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>
<script setup>
import { computed, defineEmits, onMounted, ref } from 'vue'
import { storeToRefs } from 'pinia'
import { useAPCStore } from 'stores/apc'
import { t } from 'src/plugins/i18n'
import ApcModelService from 'src/services/apc/ApcModelService'
import { apcConditionDelimiterToObject } from 'src/common/apc/apcUtil'
import DescriptionTooltip from 'components/common/DescriptionTooltip.vue'

const apcStore = useAPCStore()
const { apcConditions } = storeToRefs(apcStore)

onMounted(() => {
  loadApcModels()
})

const selected = ref([])
const filter = ref()
const rows = ref([])
const baseColumns = [
  {
    name: 'modelName',
    align: 'center',
    label: t('모델명'),
    field: 'modelName',
  },
  {
    name: 'version',
    align: 'center',
    label: t('Active 버전'),
    field: 'version',
    classes: 'version-column',
  },
]

const columns = computed(() => [
  ...baseColumns,
  ...apcConditions.value.map((c) => ({
    name: c.key,
    align: 'center',
    label: t(c.name),
    field: c.key,
  })),
  { name: 'description', align: 'center', label: t('설명'), field: 'description', classes: 'desc-column' },
])

const loadApcModels = async () => {
  const apcModels = await ApcModelService.getApcModels()

  rows.value = await Promise.all(
    apcModels.map(async (v) => {
      const conditions = await apcConditionDelimiterToObject(v.condition)
      return {
        versionId: v.activeModelVersionId,
        version: v.version,
        apcModelId: v.apcModelId,
        description: v.description,
        modelName: v.modelName,
        ...conditions,
      }
    }),
  )
}

const emit = defineEmits(['select-value'])
const handleSelectButton = () => {
  if (selected.value[0] === undefined) return
  emit('select-value', selected.value[0])
  close()
}

const close = () => {
  emit('update:modelValue', false)
}

const handleCancelButton = () => {
  close()
}
</script>

<style scoped lang="scss">
.dialog-container {
  min-width: 1000px;
  max-width: 1500px;
  max-height: 800px;
}

:deep(.q-table__bottom) {
  height: 50px !important;
}

:deep(.q-field) {
  height: 34px !important;
}

.desc-column {
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.version-column {
  width: 10px;
}
</style>
