<template>
  <q-page class="q-pa-sm">
    <q-card class="cust_subPage">
      <q-card-section class="row search_section_wrap">
        <!-- <div class="text-h5 text-weight-bold q-mt-sm">{{ $t('기간별 저수량 트렌드 및 예측') }}</div> -->
        <q-item class="col-10 search_left">
          <q-form ref="form" class="flex">
            <q-item-section class="location-area">
              <cascader
                v-model="selectedLocation"
                :label="$t('위치')"
                :titles="[$t('사업장'), $t('공장'), $t('라인')]"
                :items="locations"
                item-value="id"
                item-text="name"
                :valid-depth="3"
                class="input-required q-pl-sm"
                :rules="[$rules.required]"
              />
            </q-item-section>
            <q-item-section>
              <filterable-select
                v-model="dateType"
                :options="dateTypes"
                option-value="value"
                option-label="label"
                outlined
                :rules="[$rules.required]"
                @change="handleToolChange"
                class="input-required"
              />
            </q-item-section>
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
          </q-form>
        </q-item>
        <q-item class="col-2 search_right">
          <q-item-section>
            <q-btn class="search_btn with_icon_btn sBtn" @click="clickSearch">{{ $t('조회') }}</q-btn>
          </q-item-section>
        </q-item>
      </q-card-section>
      <q-card-section>
        <div class="row chart-height">
          <!-- 차트 영역 -->
          <div :class="isTableVisible ? 'col-6' : 'col-12'" class="full-height">
            <div class="flex justify-end q-mb-lg q-pb-md bdb_dashed">
              <div class="q-pr-sm">
                <q-btn-toggle
                  v-model="chartType"
                  @update:model-value="onChangeChartType"
                  :options="chartTypeOptions"
                  no-caps
                  push
                  toggle-color="primary"
                  color="white"
                  text-color="primary"
                  class="sbtn"
                >
                  <template v-slot:bar>
                    <q-icon name="bar_chart" />
                    {{ $t('바 차트') }}
                  </template>
                  <template v-slot:line>
                    <q-icon name="show_chart" />
                    {{ $t('라인 차트') }}
                  </template>
                </q-btn-toggle>
              </div>
              <q-btn @click="toggleTable" class="datatable_btn with_icon_btn sBtn">
                {{ $t('데이터 보기') }}
              </q-btn>
            </div>
            <ECharts :option="options" class="q-mt-sm" :resizable="true" autoresize />
          </div>
          <!-- 테이블 영역 -->
          <div v-if="isTableVisible" class="col-6 full-height datatable_wrap">
            <q-table
              bordered
              :rows="chartData"
              :columns="columns"
              row-key="id"
              virtual-scroll
              :virtual-scroll-item-size="48"
              :virtual-scroll-sticky-size-start="48"
              class="sticky-dynamic"
              :pagination="{ rowsPerPage: 0 }"
              :rows-per-page-options="[0]"
            >
            </q-table>
          </div>
        </div>
      </q-card-section>
    </q-card>
  </q-page>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import useUI from 'src/common/module/ui'
import { date } from 'quasar'
import { useI18n } from 'vue-i18n'
import { DATE_FORMAT, DATE_HOURS_FORMAT, DATE_TIME_FORMAT, MONTH_FORMAT, YEAR_FORMAT } from 'src/common/constant/format'
import { formatDate } from 'src/common/utils'
import ECharts from 'vue-echarts'
import { filterTreeItem } from 'src/common/treeUtil'
import Cascader from 'components/cascader/Cascader.vue'
import _ from 'lodash'
import ReservoirCapacityService from 'src/services/waterQuality/ReservoirCapacityService'
import LocationService from 'src/services/modeling/LocationService'
import { pickColor } from 'src/common/constant/chart'
import { graphic, use } from 'echarts/core'
import { BarChart, LineChart } from 'echarts/charts'
import { CanvasRenderer } from 'echarts/renderers'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'

use([BarChart, LineChart, GridComponent, LegendComponent, TooltipComponent, CanvasRenderer])

const { t } = useI18n()
const ui = useUI()

const srarchDateType = ref(null)
const dateType = ref({ label: t('연도별'), value: 'YEAR' })
const dateTypes = ref([
  { label: t('연도별'), value: 'YEAR' },
  { label: t('월별'), value: 'MONTH' },
  { label: t('일별'), value: 'DAY' },
  { label: t('시간별'), value: 'HOUR' },
])

const locations = ref([])
const selectedLocation = ref(null)

const selectedRangeDate = ref({
  from: ref(formatDate(date.subtractFromDate(Date.now(), { years: 2 }))),
  to: ref(formatDate(date.subtractFromDate(Date.now(), { days: 1 }))),
})

const form = ref(null)
const chartData = ref([])
const chartType = ref('bar')
const chartTypeOptions = [
  { value: 'bar', slot: 'bar' },
  { value: 'line', slot: 'line' },
]

// table 영역 토글
const isTableVisible = ref(false)
const toggleTable = () => {
  isTableVisible.value = !isTableVisible.value
}

const selectedRangeDateInput = computed(() => {
  if (selectedRangeDate.value == null) {
    return ''
  } else if (!_.isEmpty(selectedRangeDate.value.from) && !_.isEmpty(selectedRangeDate.value.to)) {
    return `${selectedRangeDate.value.from} ~ ${selectedRangeDate.value.to}`
  }
  return ''
})

const columns = ref([
  { name: 'date', align: 'left', label: t('일시'), field: 'date', sortable: true },
  { name: 'reservoirCapacity', align: 'left', label: t('저수량(백만㎥)'), field: 'reservoirCapacity', sortable: true },
  { name: 'rainFall', align: 'left', label: t('강수량(mm)'), field: 'rainFall', sortable: true },
])

const defaultOptions = {
  color: ['#80FFA5', '#37A2FF'],
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'cross',
      label: {
        backgroundColor: '#6a7985',
      },
    },
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '20%',
    top: '5%',
    containLabel: true,
  },
  legend: {
    data: [t('저수량(백만㎥)'), t('강수량(mm)')],
  },
  xAxis: [
    {
      boundaryGap: false,
      data: [],
    },
  ],
  yAxis: [
    {
      type: 'value',
      name: t('저수량(백만㎥)'),
    },
    {
      type: 'value',
      name: t('강수량(mm)'),
      position: 'right',
      offset: 0,
    },
  ],
  series: [
    {
      name: t('저수량(백만㎥)'),
      type: 'line',
      smooth: true,
      lineStyle: {
        width: 0,
      },
      showSymbol: false,
      areaStyle: {
        opacity: 0.8,
        color: new graphic.LinearGradient(0, 0, 0, 1, [
          {
            offset: 0,
            color: '#C2FEFE',
          },
          {
            offset: 1,
            color: '#71C2F5',
          },
        ]),
      },
      emphasis: {
        focus: 'series',
      },
      data: [],
    },
    {
      name: t('강수량(mm)'),
      type: 'line',
      lineStyle: {
        width: 2,
      },
      color: '#F5Bd94',
      showSymbol: false,
      emphasis: {
        focus: 'series',
      },
      data: [],
    },
    {
      type: 'line',
      markArea: {
        silent: true,
        data: [
          [
            {
              xAxis: '',
            },
            {
              xAxis: '',
            },
          ],
        ],
      },
    },
  ],
}

const options = ref(_.cloneDeep(defaultOptions))

watch(
  () => chartData.value,
  () => {
    if (chartData.value) {
      if (chartType.value === 'line') {
        options.value = {
          ..._.cloneDeep(defaultOptions),
          xAxis: [
            {
              boundaryGap: false,
              data: chartData.value.map((m) => (srarchDateType.value == 'HOUR' ? `${m.date}${t('시')}` : m.date)),
            },
          ],
          series: [
            {
              ...defaultOptions.series[0],
              yAxisIndex: 0,
              data: chartData.value.map((m) => m.reservoirCapacity),
            },
            {
              ...defaultOptions.series[1],
              yAxisIndex: 1,
              data: chartData.value.map((m) => m.rainFall),
            },
            {
              ...defaultOptions.series[2],
              markArea: {
                data: generateMarkArea(chartData.value),
                itemStyle: {
                  color: 'rgba(250, 207, 207)',
                },
              },
            },
          ],
        }
      } else if (chartType.value === 'bar' && srarchDateType.value === 'MONTH') {
        const months = Array.from({ length: 12 }, (_, i) => {
          return i < 9 ? `0${i + 1}` : i + 1
        })
        const years = [...new Set(chartData.value.map((item) => item.date.substring(0, 4)))]
        const rainbowColors = generateRainbowColors(years.length)
        const dataByYear = years.map((year, index) => {
          const reservoirCapacity = months.map((month) => {
            const item = chartData.value.find((d) => d.date === `${year}-${month}`)
            return item ? item.reservoirCapacity : 0
          })
          const rainFall = months.map((month) => {
            const item = chartData.value.find((d) => d.date === `${year}-${month}`)
            return item ? item.rainFall : 0
          })
          return {
            year,
            reservoirCapacity,
            rainFall,
            itemStyle: {
              color: rainbowColors[index],
            },
          }
        })
        options.value = {
          ..._.cloneDeep(defaultOptions),
          legend: {
            data: [
              ...dataByYear.map((yearData) => `${yearData.year} ${t('저수량')}`),
              ...dataByYear.map((yearData) => `${yearData.year} ${t('강수량')}`),
            ],
          },
          xAxis: [
            {
              ...defaultOptions.xAxis[0],
              boundaryGap: true,
              data: months.map((m) => `${m}${t('월')}`),
            },
          ],
          series: [
            ...dataByYear.map((yearData) => ({
              name: `${yearData.year} ${t('저수량')}`,
              type: 'bar',
              data: yearData.reservoirCapacity,
              itemStyle: yearData.itemStyle,
            })),
            ...dataByYear.map((yearData) => ({
              name: `${yearData.year} ${t('강수량')}`,
              type: 'line',
              yAxisIndex: 1,
              data: yearData.rainFall,
              itemStyle: yearData.itemStyle,
              width: 2,
            })),
            {
              ...defaultOptions.series[2],
              markArea: {
                data: generateMarkArea(chartData.value),
                itemStyle: {
                  color: 'rgba(250, 207, 207)',
                },
              },
            },
          ],
        }
      } else if (chartType.value === 'bar') {
        options.value = {
          ..._.cloneDeep(defaultOptions),
          xAxis: [
            {
              ...defaultOptions.xAxis[0],
              boundaryGap: true,
              data: chartData.value.map((m) => (srarchDateType.value == 'HOUR' ? `${m.date}${t('시')}` : m.date)),
            },
          ],
          series: [
            {
              ...defaultOptions.series[0],
              type: 'bar',
              itemStyle: {
                color: '#71C2F5',
              },
              emphasis: {
                focus: 'series',
              },
              data: chartData.value.map((m) => m.reservoirCapacity),
            },
            {
              ...defaultOptions.series[1],
              type: 'line',
              yAxisIndex: 1,
              data: chartData.value.map((m) => m.rainFall),
            },
            {
              ...defaultOptions.series[2],
              markArea: {
                data: generateMarkArea(chartData.value),
                itemStyle: {
                  color: 'rgba(250, 207, 207)',
                },
              },
            },
          ],
        }
      }
    }
  },
)

const onChangeChartType = async () => {
  if (chartType.value === 'line') {
    options.value = {
      ..._.cloneDeep(defaultOptions),
      xAxis: [
        {
          boundaryGap: false,
          data: chartData.value.map((m) => (srarchDateType.value == 'HOUR' ? `${m.date}${t('시')}` : m.date)),
        },
      ],
      series: [
        {
          ...defaultOptions.series[0],
          yAxisIndex: 0,
          data: chartData.value.map((m) => m.reservoirCapacity),
        },
        {
          ...defaultOptions.series[1],
          yAxisIndex: 1,
          data: chartData.value.map((m) => m.rainFall),
        },
        {
          ...defaultOptions.series[2],
          markArea: {
            data: generateMarkArea(chartData.value),
            itemStyle: {
              color: 'rgba(250, 207, 207)',
            },
          },
        },
      ],
    }
  } else if (chartType.value === 'bar' && srarchDateType.value === 'MONTH') {
    const months = Array.from({ length: 12 }, (_, i) => {
      return i < 9 ? `0${i + 1}` : i + 1
    })
    const years = [...new Set(chartData.value.map((item) => item.date.substring(0, 4)))]
    const rainbowColors = generateRainbowColors(years.length)
    const dataByYear = years.map((year, index) => {
      const reservoirCapacity = months.map((month) => {
        const item = chartData.value.find((d) => d.date === `${year}-${month}`)
        return item ? item.reservoirCapacity : 0
      })
      const rainFall = months.map((month) => {
        const item = chartData.value.find((d) => d.date === `${year}-${month}`)
        return item ? item.rainFall : 0
      })
      return {
        year,
        reservoirCapacity,
        rainFall,
        itemStyle: {
          color: rainbowColors[index],
        },
      }
    })
    options.value = {
      ..._.cloneDeep(defaultOptions),
      legend: {
        data: [
          ...dataByYear.map((yearData) => `${yearData.year} ${t('저수량')}`),
          ...dataByYear.map((yearData) => `${yearData.year} ${t('강수량')}`),
        ],
      },
      xAxis: [
        {
          ...defaultOptions.xAxis[0],
          boundaryGap: true,
          data: months.map((m) => `${m}${t('월')}`),
        },
      ],
      series: [
        ...dataByYear.map((yearData) => ({
          name: `${yearData.year} ${t('저수량')}`,
          type: 'bar',
          data: yearData.reservoirCapacity,
          itemStyle: yearData.itemStyle,
        })),
        ...dataByYear.map((yearData) => ({
          name: `${yearData.year} ${t('강수량')}`,
          type: 'line',
          yAxisIndex: 1,
          data: yearData.rainFall,
          itemStyle: yearData.itemStyle,
          width: 2,
        })),
      ],
    }
  } else if (chartType.value === 'bar') {
    options.value = {
      ..._.cloneDeep(defaultOptions),
      xAxis: [
        {
          ...defaultOptions.xAxis[0],
          boundaryGap: true,
          data: chartData.value.map((m) => (srarchDateType.value == 'HOUR' ? `${m.date}${t('시')}` : m.date)),
        },
      ],
      series: [
        {
          ...defaultOptions.series[0],
          type: 'bar',
          emphasis: {
            focus: 'series',
          },
          itemStyle: {
            color: '#71C2F5',
          },
          data: chartData.value.map((m) => m.reservoirCapacity),
        },
        {
          ...defaultOptions.series[1],
          type: 'line',
          yAxisIndex: 1,
          data: chartData.value.map((m) => m.rainFall),
        },
        {
          ...defaultOptions.series[2],
          markArea: {
            data: generateMarkArea(chartData.value),
            itemStyle: {
              color: 'rgba(250, 207, 207)',
            },
          },
        },
      ],
    }
  }
}

const generateRainbowColors = (count) => {
  const rainbowColors = []
  for (let i = 0; i < count; i++) {
    rainbowColors.push(pickColor())
  }

  return rainbowColors
}

const generateMarkArea = (chartData) => {
  const markAreaData = []
  const findIsPredicted = chartData.find((item) => item.isPredicted == true)
  if (findIsPredicted) {
    const startDate = srarchDateType.value == 'HOUR' ? `${findIsPredicted.date}${t('시')}` : findIsPredicted.date

    markAreaData.push([
      {
        name: t('예측 구간'),
        xAxis: startDate,
      },
      {
        xAxis: 'max', // 끝나는 날짜, 'max'는 끝까지를 의미
      },
    ])
  }

  return markAreaData
}

const loadLocations = async () => {
  ui.loading.show()
  locations.value = await LocationService.getLocationsTree()
  ui.loading.hide()

  const filterTreeItemByLine = filterTreeItem(locations.value, 'LINE', { id: 'type' })
  if (filterTreeItemByLine.length > 0) {
    selectedLocation.value = filterTreeItemByLine[0].id
  }
}

const clickSearch = () => {
  getData()
}

const getData = async () => {
  const success = await form.value.validate()
  if (success) {
    ui.loading.show()
    try {
      srarchDateType.value = dateType.value.value

      const capacityTrend = await ReservoirCapacityService.getReservoirCapacityTrend(
        dateType.value.value,
        new Date(selectedRangeDate.value.from).toISOString(),
        new Date(selectedRangeDate.value.to).toISOString(),
        selectedLocation.value,
      )
      chartData.value = capacityTrend.map((v) => ({
        date:
          srarchDateType.value == 'YEAR'
            ? date.formatDate(new Date(v.date), YEAR_FORMAT)
            : srarchDateType.value == 'MONTH'
              ? date.formatDate(new Date(v.date), MONTH_FORMAT)
              : srarchDateType.value == 'DAY'
                ? date.formatDate(new Date(v.date), DATE_FORMAT)
                : srarchDateType.value == 'HOUR'
                  ? date.formatDate(new Date(v.date), DATE_HOURS_FORMAT)
                  : date.formatDate(new Date(v.date), DATE_TIME_FORMAT),
        reservoirCapacity: v.reservoirCapacity ? Number(v.reservoirCapacity.toFixed(5)) : 0,
        rainFall: v.rainFall ? Number(v.rainFall.toFixed(1)) : 0.0,
        isPredicted: v.isPredicted,
      }))

      chartData.value.sort((a, b) => new Date(a.date) - new Date(b.date))
    } catch (error) {
      console.error('Error getData :', error)
    } finally {
      ui.loading.hide()
    }
  } else {
    ui.notify.warning(t('유효하지 않은 값이 있습니다.'))
  }
}

onMounted(async () => {
  await loadLocations()
  getData()
})
</script>
<style scoped lang="scss">
.sticky-dynamic:deep {
  height: 75vh;

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

.chart-height {
  height: 78vh;
}
.location-area {
  min-width: 280px;
}
</style>
