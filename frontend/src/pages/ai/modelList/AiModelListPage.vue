<!-- eslint-disable no-unused-vars -->
<template>
  <q-page class="q-pa-sm">
    <q-card class="cust_subPage">
      <q-card-section class="row search_section_wrap">
        <q-item class="col-2 search_left">
          <q-item-section> </q-item-section>
        </q-item>
        <q-item class="col-4 search_right">
          <q-item-section>
            <q-btn
              v-permission:aiModelList.create
              @click="clickCreate"
              class="delete_btn with_icon_btn sBtn secondary"
              >{{ $t('추가') }}</q-btn
            >
            <q-btn
              v-permission:aiModelList.delete
              :disabled="selected == null || selected.length == 0"
              @click="clickDelete"
              class="delete_btn with_icon_btn sBtn secondary"
              >{{ $t('삭제') }}</q-btn
            >
          </q-item-section>
        </q-item>
      </q-card-section>
      <simple-table
        v-model:selected="selected"
        :rows="rows"
        :columns="columns"
        :pagination="{ rowsPerPage: 10 }"
        row-key="id"
        color="amber"
        selection="single"
        bordered
      >
        <template v-slot:body="props">
          <q-tr class="cursor-pointer" :props="props" @click="props.selected = !props.selected">
            <q-td v-for="col in props.cols" :key="col.name" :props="props">
              <template v-if="col.name === 'parameters'">
                <div class="column q-gutter-xs">
                  <div class="row items-center no-wrap">
                    <q-avatar size="20px" class="q-mr-xs" color="primary" text-color="white"> X </q-avatar>
                    <div class="ellipsis" style="max-width: 300px">
                      <span>{{ props.row.parameters.x }}</span>
                    </div>
                  </div>
                  <div class="row items-center no-wrap">
                    <q-avatar size="20px" class="q-mr-xs" color="teal" text-color="white"> Y </q-avatar>
                    <div class="ellipsis" style="max-width: 300px">
                      <span>{{ props.row.parameters.y }}</span>
                    </div>
                  </div>
                  <q-tooltip anchor="top middle" self="bottom middle">
                    <div style="white-space: pre-line">
                      X: {{ props.row.parameters.x }}<br />
                      Y: {{ props.row.parameters.y }}
                    </div>
                  </q-tooltip>
                </div>
              </template>
              <template v-else-if="col.name === 'status'">
                <div class="row items-center no-wrap">
                  <q-icon
                    :name="getStatusIcon(props.row.status)"
                    :color="getStatusColor(props.row.status)"
                    size="18px"
                    class="q-mr-xs"
                  />
                  <span>{{ props.row.status }}</span>
                </div>
                <template v-if="props.row.status === '실패' && props.row.failureReason">
                  <q-tooltip anchor="top middle" self="bottom middle">
                    <div style="white-space: pre-line">{{ props.row.failureReason }}</div>
                  </q-tooltip>
                </template>
              </template>
              <template v-else-if="col.name === 'validate' && props.row.status === '완료'">
                <q-btn @click.stop="() => handleValidateButtonClick(props.row.id)" class="add_btn sBtn">
                  {{ $t('검증') }}
                </q-btn>
              </template>
              <template v-else>
                <div>{{ col.value }}</div>
              </template>
            </q-td>
          </q-tr>
        </template>
      </simple-table>
    </q-card>
  </q-page>
</template>

<script setup>
import { onBeforeMount, ref } from 'vue'
import { date } from 'quasar'
import useUI from 'src/common/module/ui'
import { t } from 'src/plugins/i18n'
import SimpleTable from 'components/common/SimpleTable.vue'
import AiService from 'src/services/ai/AiService'
import { useRouter } from 'vue-router'

const router = useRouter()
const ui = useUI()

const columns = [
  { name: 'name', align: 'left', label: t('모델명'), field: 'name' },
  { name: 'description', align: 'left', label: t('모델 설명'), field: 'description' },
  { name: 'toolName', align: 'left', label: t('설비'), field: 'toolName' },
  { name: 'parameters', align: 'left', label: t('파라미터'), field: 'parameters' },
  { name: 'status', align: 'left', label: t('상태'), field: 'status' },
  { name: 'algorithm', align: 'left', label: t('알고리즘'), field: 'algorithm' },
  {
    name: 'createdAt',
    align: 'left',
    label: t('생성일'),
    field: 'createdAt',
    format: (value) => {
      if (value) {
        return date.formatDate(value, 'YYYY-MM-DD')
      }
    },
  },
  { name: 'validate', align: 'left', label: t('모델 검증'), field: 'validate' },
]

const rows = ref([])
const selected = ref([])

const STATUS_META = {
  완료: { label: t('완료'), icon: 'check_circle', color: 'positive' },
  학습중: { label: t('학습중'), icon: 'pending', color: 'grey' },
  실패: { label: t('실패'), icon: 'error', color: 'red' },
}

const getStatusColor = (status) => STATUS_META[status]?.color ?? 'grey-4'
const getStatusIcon = (status) => STATUS_META[status]?.icon ?? 'help_outline'

const getAndSetRows = async () => {
  const models = await AiService.getModels()
  rows.value = models.map((row) => ({
    ...row,
    parameters: {
      x: row.parameterDetails?.map((p) => p.name).join(', '),
      y: row.yparameterName ?? row.yparameterId,
    },
  }))
}

const clickCreate = async () => {
  await router.push({ path: `/ai/modeling` })
}

const clickDelete = () => {
  ui.confirm(t('삭제'), t('선택된 항목을 삭제 하시겠습니까?'), t('삭제'), t('취소')).onOk(deleteModel)
}

const deleteModel = async () => {
  try {
    await AiService.delete(selected.value[0].id)
    ui.notify.success(t('삭제 되었습니다.'))
    await getAndSetRows()
  } catch (error) {
    error.response?.data.extraData.datas.forEach((message) => {
      if (error.response.data.code === 'ERROR_CONSTRAINT_VIOLATION') {
        ui.notify.error(message.path) //임시
      }
    })
  }
}

const handleValidateButtonClick = async (modelId) => {
  await router.push({ path: `/ai/modeling`, query: { id: modelId } })
}

onBeforeMount(async () => {
  await getAndSetRows()
})
</script>
