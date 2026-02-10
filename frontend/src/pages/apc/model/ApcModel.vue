<template>
  <q-page class="q-pa-sm">
    <q-card class="cust_subPage">
      <q-card-section class="row search_section_wrap">
        <q-item class="search_left"></q-item>
        <q-item class="col-6 search_right">
          <q-item-section>
            <q-btn class="terminal_btn with_icon_btn sBtn" :disabled="selected.length === 0" @click="moveToSimulation"
              >{{ $t('시뮬레이션') }}
            </q-btn>
            <q-btn
              v-permission:apcModel.create
              class="add_btn with_icon_btn sBtn secondary"
              @click="showModifyPopup(true)"
              >{{ $t('추가') }}
            </q-btn>
            <q-btn
              v-permission:apcModel.update
              class="edit_btn with_icon_btn sBtn secondary"
              :disabled="selected.length === 0"
              @click="showModifyPopup(false)"
              >{{ $t('수정') }}
            </q-btn>
            <q-btn
              v-permission:apcModel.delete
              class="delete_btn with_icon_btn sBtn secondary"
              :disabled="selected.length === 0"
              @click="onDelete"
              >{{ $t('삭제') }}
            </q-btn>
            <ApcModelModify
              v-if="isShowModifyPopup"
              v-model="isShowModifyPopup"
              :model-version-id="selectedVersionId"
              @save-success="handleApcModelModifySaveSuccess"
            ></ApcModelModify>
          </q-item-section>
        </q-item>
      </q-card-section>
      <simple-table
        bordered
        :rows="rows"
        :columns="columns"
        row-key="apcModelId"
        color="amber"
        selection="single"
        v-model:selected="selected"
      >
        <template v-slot:body="props">
          <q-tr class="cursor-pointer" :props="props" @click="props.selected = !props.selected">
            <q-td v-for="col in props.cols" :key="col.name" :props="props">
              <template v-if="col.name === 'createAt'">
                {{ date.formatDate(col.value, 'YYYY-MM-DD HH:mm:ss') }}
              </template>
              <template v-else-if="col.value === true">
                <q-icon name="check" size="md" color="primary" />
              </template>
              <template v-else-if="col.value === false"></template>
              <template v-else-if="col.name === 'formulaWorkspace'">
                <q-icon name="calculate" color="green" size="md">
                  <q-tooltip class="formula-tooltip">
                    <Formula :workspace-json-string="col.value"></Formula>
                  </q-tooltip>
                </q-icon>
              </template>
              <template v-else-if="col.name === 'description'">
                <description-tooltip v-if="col.value" :description="col.value"></description-tooltip>
                {{ col.value }}
              </template>
              <template v-else-if="col.name === 'createBy'">
                <UserAvatar :user-id="col.value" />
              </template>
              <template v-else>
                {{ col.value }}
              </template>
            </q-td>
          </q-tr>
        </template>
      </simple-table>
    </q-card>
  </q-page>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { date } from 'quasar'
import useUI from 'src/common/module/ui'
import { t } from 'src/plugins/i18n'
import ApcModelModify from 'pages/apc/model/ApcModelModify.vue'
import ApcModelingService from 'src/services/apc/ApcModelService'
import { useAPCStore } from 'stores/apc'
import { storeToRefs } from 'pinia'
import Formula from 'pages/apc/model/Formula.vue'
import UserAvatar from 'components/common/UserAvatar.vue'
import { apcConditionDelimiterToObject } from 'src/common/apc/apcUtil'
import DescriptionTooltip from 'components/common/DescriptionTooltip.vue'

const router = useRouter()
const apcStore = useAPCStore()
const { apcConditions } = storeToRefs(apcStore)

const ui = useUI()

onMounted(() => {
  loadApcModels()
})
const selected = ref([])
const isShowModifyPopup = ref(false)
const selectedVersionId = ref(null)

const baseColumns = [
  { name: 'version', align: 'left', label: t('Active 버전'), field: 'version' },
  { name: 'formulaWorkspace', align: 'left', label: t('계산식'), field: 'formulaWorkspace' },
  { name: 'isUse', align: 'left', label: t('사용여부'), field: 'isUse' },
  { name: 'description', align: 'left', label: t('설명'), field: 'description', classes: 'desc-column' },
  { name: 'createBy', align: 'left', label: t('생성자'), field: 'createBy' },
  { name: 'createAt', align: 'left', label: t('생성일시'), field: 'createAt' },
]

const columns = computed(() => [
  { name: 'modelName', align: 'left', label: t('모델명'), field: 'modelName' },
  ...apcConditions.value.map((c) => ({
    name: c.key,
    align: 'left',
    label: t(c.name),
    field: c.key,
  })),
  ...baseColumns,
])

const rows = ref([])
const loadApcModels = async () => {
  const apcModels = await ApcModelingService.getApcModels()
  rows.value = await Promise.all(
    apcModels.map(async (model) => {
      const conditions = await apcConditionDelimiterToObject(model.condition)
      return {
        ...model,
        ...conditions,
      }
    }),
  )
}

const showModifyPopup = (isCreate) => {
  if (isCreate) {
    selectedVersionId.value = null
  } else {
    selectedVersionId.value = selected.value[0].activeModelVersionId
  }
  isShowModifyPopup.value = true
}
const onDelete = () => {
  const onOkCallback = async () => {
    await deleteApcModel(selected.value[0].apcModelId)
    selected.value = []
  }
  ui.confirm(t('삭제'), t('선택된 항목을 삭제 하시겠습니까?'), t('삭제'), t('취소')).onOk(onOkCallback)
}

const deleteApcModel = async (apcId) => {
  try {
    await ApcModelingService.deleteApcModel(apcId)
    ui.notify.success(t('삭제 되었습니다.'))
  } catch (error) {
    error.response.data.extraData.datas.forEach((message) => {
      if (error.response.data.code === 'ERROR_CONSTRAINT_VIOLATION') {
        ui.notify.error(message.path) //임시
      }
    })
  }
  await loadApcModels()
}
const handleApcModelModifySaveSuccess = async () => {
  selected.value = []
  await loadApcModels()
}

const moveToSimulation = () => {
  const apcModelVersionId = selected.value[0]?.activeModelVersionId
  router.push({ path: `/apc/simulation`, query: { apcModelVersionId } })
}
</script>
<style>
.formula-tooltip {
  overflow: visible;
  min-width: 0;
  min-height: 0;
  padding: 0;
}

.desc-column {
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
