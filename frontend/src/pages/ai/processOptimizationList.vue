<template>
  <q-page class="q-pa-sm">
    <q-card class="cust_subPage">
      <q-card-section class="row search_section_wrap">
        <q-item class="col-2 search_left">
          <q-item-section>
            <filterable-select
              outlined
              v-model="selectedTool"
              :options="toolOptions"
              option-value="id"
              option-label="name"
              :label="$t('설비')"
              clearable
            >
              <template v-slot:prepend>
                <q-icon name="construction" />
              </template>
            </filterable-select>
          </q-item-section>
        </q-item>
        <q-item class="col-6 search_right">
          <q-item-section>
            <q-btn class="add_btn with_icon_btn sBtn secondary" @click="onCreate">{{ $t('공정최적화') }} </q-btn>
            <q-btn class="delete_btn with_icon_btn sBtn secondary" :disabled="selected.length === 0" @click="onDelete"
              >{{ $t('삭제') }}
            </q-btn>
          </q-item-section>
        </q-item>
      </q-card-section>
      <simple-table
        :rows="filteredItems"
        :columns="columns"
        row-key="id"
        v-model:selected="selected"
        selection="multiple"
        :loading="loading"
        loading-label="조회중입니다..."
      >
        <template v-slot:body="props">
          <q-tr class="cursor-pointer" :props="props" @click="props.selected = !props.selected">
            <q-td v-for="col in props.cols" :key="col.name" :props="props">
              <template v-if="col.name === 'analysisCheck'">
                <q-btn
                  v-if="props.row.status === 'SUCCESS'"
                  icon="visibility"
                  flat
                  dense
                  color="blue"
                  @click.stop="handleAnalysisCheck(props.row)"
                />
              </template>
              <template v-else-if="col.name === 'status'">
                <div class="row items-center no-wrap">
                  <q-icon
                    :name="getStatusIcon(props.row.status)"
                    :color="getStatusColor(props.row.status)"
                    size="18px"
                    class="q-mr-xs"
                  />
                  <span>{{ getStatusLabel(props.row.status) }}</span>
                </div>
                <template v-if="props.row.status === 'FAIL' && props.row.failureReason">
                  <q-tooltip anchor="top middle" self="bottom middle">
                    <div style="white-space: pre-line">{{ props.row.failureReason }}</div>
                  </q-tooltip>
                </template>
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
import { ref, computed, onMounted } from 'vue'
import { t } from 'src/plugins/i18n'
import { useRouter } from 'vue-router'
import ToolService from 'src/services/modeling/ToolService'
import ProcessOptimizationService from 'src/services/processOptimization/ProcessOptimizationService'

const STATUS_META = {
  SUCCESS: { label: t('완료'), icon: 'check_circle', color: 'positive' },
  STUDYING: { label: t('계산중'), icon: 'pending', color: 'grey' },
  FAIL: { label: t('실패'), icon: 'error', color: 'red' },
}

const router = useRouter()
import useUI from 'src/common/module/ui'
// 상태 관리
const toolOptions = ref([])
const items = ref([])
const selected = ref([])
const selectedTool = ref(null)
const ui = useUI()
const loading = ref(false)

// 테이블 컬럼 정의
const columns = [
  { name: 'name', label: '제목', align: 'left', field: 'name' },
  { name: 'toolName', label: '장비', align: 'left', field: 'toolName' },
  { name: 'model', label: '모델', align: 'left', field: 'aiModelName' },
  { name: 'targetValue', label: 'Target Value', align: 'left', field: 'targetValue' },
  { name: 'status', align: 'left', label: t('상태'), field: 'status' },
  { name: 'optimalValue', label: 'Optimal Value', align: 'left', field: 'optimalValue' },
  { name: 'createAt', label: '생성일', align: 'left', field: 'createAt' },
  { name: 'analysisCheck', label: '분석 확인', align: 'center', field: 'status' },
]

// computed
const filteredItems = computed(() => {
  if (selectedTool.value) {
    return items.value.filter((v) => v.toolId === selectedTool.value.id)
  }
  return items.value
})

// 메서드
const getStatusColor = (status) => STATUS_META[status]?.color ?? 'grey-4'
const getStatusIcon = (status) => STATUS_META[status]?.icon ?? 'help_outline'
const getStatusLabel = (status) => STATUS_META[status]?.label ?? ''

const getTools = async () => {
  try {
    const toolList = await ToolService.getTools()
    toolOptions.value = toolList
  } catch (error) {
    console.error('Tool 데이터 조회 실패:', error)
  }
}

const initData = async () => {
  try {
    loading.value = true
    const result = await ProcessOptimizationService.getOptimizations()
    if (result) {
      items.value = result.map((v) => ({
        ...v,
        toolName: toolOptions.value.find((k) => k.id === v.toolId)?.name || '',
        createAt: new Date(v.createAt).toISOString().split('T')[0],
      }))
    }
  } catch (error) {
    console.error('데이터 초기화 실패:', error)
  } finally {
    loading.value = false
  }
}

const handleAnalysisCheck = (item) => {
  selected.value = []
  router.push({ name: 'processOptimizationAnalysis', query: { id: item.id } })
}

const onCreate = () => {
  router.push({ path: '/ai/process-optimization-analysis' })
}

const onDelete = () => {
  const onOkCallback = async () => {
    const selectedIds = selected.value.map((item) => item.id)
    await deleteOptimizations(selectedIds)
    selected.value = []
  }
  ui.confirm(t('공정 최적화 분석 결과 삭제'), t('선택된 항목을 삭제 하시겠습니까?'), t('삭제'), t('취소')).onOk(
    onOkCallback,
  )
}

const deleteOptimizations = async (selectedIds) => {
  try {
    loading.value = true
    await ProcessOptimizationService.deleteOptimizations(selectedIds)
    ui.notify.success(t('삭제 되었습니다.'))
    await initData()
  } catch (error) {
    if (error.response?.data?.extraData?.datas) {
      error.response.data.extraData.datas.forEach((message) => {
        if (error.response.data.code === 'ERROR_CONSTRAINT_VIOLATION') {
          ui.notify.error(message.path)
        }
      })
    } else {
      ui.notify.error(t('삭제 중 오류가 발생했습니다.'))
    }
  } finally {
    loading.value = false
  }
}

// 라이프사이클 훅
onMounted(() => {
  getTools().then(() => initData())
})
</script>

<style scoped>
.search_section_wrap {
  padding: 1rem;
}

.search_right {
  display: flex;
  justify-content: flex-end;
}

.with_icon_btn {
  margin-left: 0.5rem;
}
</style>
