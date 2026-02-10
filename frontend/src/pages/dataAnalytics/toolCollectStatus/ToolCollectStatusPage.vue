<template>
  <q-page class="q-pa-md">
    <q-card class="cust_subPage">
      <q-card-section class="row search_section_wrap">
        <q-item class="col-12 search_right">
          <q-item-section>
            <q-btn @click="onSearch" class="search_btn with_icon_btn sBtn">
              {{ $t('조회') }}
            </q-btn>
          </q-item-section>
        </q-item>
      </q-card-section>
      <simple-table
        bordered
        :rows="rows"
        :columns="columns"
        row-key="name"
        class="sticky-header"
        :pagination="{
          rowsPerPage: 0,
        }"
        hide-pagination
        virtual-scroll
      >
        <template v-slot:body="props">
          <q-tr
            :props="props"
            @click="props.selected = !props.selected"
            class="cursor-pointer hover-effect"
            :class="{ 'row-selected': props.selected }"
          >
            <q-td v-for="col in props.cols" :key="col.name" :props="props">
              <template v-if="col.name === 'toolName'">
                <div class="tool-name">
                  {{ props.row.toolName || $t('Unknown') }}
                  <q-btn size="xs" class="q-ml-md" color="primary" round @click="showTrendChart(props.row)"
                    ><q-icon name="show_chart" color="white"></q-icon
                    ><q-tooltip>{{ $t('트렌드 차트 보기') }}</q-tooltip></q-btn
                  >
                </div>
              </template>
              <template v-if="col.name === 'dcpCollectStatus'">
                <div class="status-container">
                  <q-chip class="status-chip good" :label="$t('양호') + ': ' + props.row.dcpGoodCnt" />
                  <div>{{ ':' }}</div>
                  <q-chip
                    class="status-chip"
                    :class="getBadChipClass(props.row.dcpBadCnt)"
                    :label="$t('문제') + ': ' + props.row.dcpBadCnt"
                  />
                </div>
              </template>
              <template v-if="col.name === 'paramCollectStatus'">
                <div class="status-container">
                  <q-chip class="status-chip good" :label="$t('양호') + ': ' + props.row.parameterGoodCnt" />
                  <div>{{ ':' }}</div>
                  <q-chip
                    class="status-chip"
                    :class="getBadChipClass(props.row.parameterBadCnt)"
                    :label="$t('문제') + ': ' + props.row.parameterBadCnt"
                  />
                </div>
              </template>
              <template v-if="col.name === 'lastCollectedAt'">
                <div class="collection-info">
                  <template v-if="props.row.lastCollectedAtList && props.row.lastCollectedAtList.length > 0">
                    <div
                      v-for="(lastCollectedAt, index) in props.row.lastCollectedAtList"
                      :key="lastCollectedAt"
                      class="collection-item"
                    >
                      <q-chip
                        class="dcp-chip"
                        size="sm"
                        :label="$t('DCP ID') + ': ' + (props.row.dcpIds[index] || $t('Unknown'))"
                      />
                      <span class="timestamp">
                        {{ lastCollectedAt ? formatDateTime(lastCollectedAt) : $t('-') }}
                      </span>
                    </div>
                  </template>
                </div>
              </template>
            </q-td>
          </q-tr>
        </template>
      </simple-table>
    </q-card>
  </q-page>
</template>

<script setup>
import { ref } from 'vue'
import { date } from 'quasar'
import ToolService from 'src/services/modeling/ToolService'
import { t } from 'src/plugins/i18n'
import useUI from 'src/common/module/ui'
import { useRouter } from 'vue-router'

const router = useRouter()
const ui = useUI()

const rows = ref([])
const toolCollectData = ref([])

const formatDateTime = (dateStr) => {
  return date.formatDate(dateStr, 'YYYY-MM-DD HH:mm:ss')
}

const getBadChipClass = (badCnt) => {
  if (badCnt === '' || badCnt === null || badCnt === 0) {
    return 'neutral'
  }
  return 'bad'
}

const columns = ref([
  {
    name: 'toolName',
    align: 'left',
    label: t('설비명'),
    field: 'toolName',
    sortable: true,
  },
  {
    name: 'dcpCollectStatus',
    align: 'center',
    label: t('DCP 수집 상태'),
    field: 'dcpCollectStatus',
    sortable: true,
  },
  {
    name: 'paramCollectStatus',
    align: 'center',
    label: t('파라미터 수집 상태'),
    field: 'paramCollectStatus',
    sortable: true,
  },
  {
    name: 'lastCollectedAt',
    align: 'center',
    label: t('최근 수집 시간'),
    field: 'lastCollectedAt',
  },
])

const getToolCollectStatus = async () => {
  try {
    ui.loading.show()
    toolCollectData.value = await ToolService.getToolCollectStatus()
    rows.value = toolCollectData.value.map((status) => ({
      toolId: status.toolId,
      toolName: status.toolName,
      dcpGoodCnt: status.dcpGoodCnt,
      dcpBadCnt: status.dcpBadCnt,
      parameterGoodCnt: status.parameterGoodCnt,
      parameterBadCnt: status.parameterBadCnt,
      lastCollectedAtList: status.lastCollectedAtList,
      dcpIds: status.dcpIds || [],
      parameterIds: status.badCollectedParameterIds.concat(status.goodCollectedParameterIds),
    }))
  } catch {
    rows.value = []
  } finally {
    ui.loading.hide()
  }
}

const showTrendChart = (data) => {
  let startDate =
    data.lastCollectedAtList.filter((v) => v).length !== 0
      ? data.lastCollectedAtList
          .filter((v) => v)
          .map((dateStr) => new Date(dateStr))
          .reduce((oldest, current) => (current < oldest ? current : oldest))
      : null
  if (startDate === null) {
    ui.notify.warning(t('최근 수집한 데이터가 없습니다.'))
    return
  }
  startDate = date.subtractFromDate(startDate, { minutes: 10 })

  router.push({
    path: `/parameter-trend`,
    query: {
      toolId: data.toolId,
      parameterId: data.parameterIds[0],
      fromDateTime: startDate,
      toDateTime: new Date(),
      isSearch: true,
    },
  })
}

const onSearch = async () => {
  getToolCollectStatus()
}

getToolCollectStatus()
</script>

<style lang="scss" scoped>
.sticky-header {
  height: calc(100vh - 180px);
}
.tool-name {
  font-size: 15px;
  font-weight: 600;
  color: #334155;
}

.status-container {
  display: flex;
  gap: 12px;
  justify-content: center;
  align-items: center;
}

.status-chip {
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 13px;
  font-weight: 500;

  &.good {
    background-color: #e0f2fe;
    color: #0369a1;
  }

  &.neutral {
    background-color: #f1f5f9;
    color: #64748b;
  }

  &.bad {
    background-color: #fee2e2;
    color: #dc2626;
  }
}

.collection-info {
  .collection-item {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 8px;

    &:last-child {
      margin-bottom: 0;
    }
  }

  .dcp-chip {
    background-color: #e0e7ff;
    color: #4f46e5;
    font-weight: 500;
  }

  .timestamp {
    font-size: 14px;
    font-weight: 600;
    color: #475569;
  }
}

.hover-effect {
  transition: background-color 0.2s ease;

  &:hover {
    background-color: #f8fafc;
  }
}

.row-selected {
  background-color: #f0f9ff;
}
</style>
