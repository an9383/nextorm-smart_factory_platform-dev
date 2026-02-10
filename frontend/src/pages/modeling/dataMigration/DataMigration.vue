<template>
  <q-page class="q-pa-sm">
    <q-card class="cust_subPage">
      <q-card-section class="row search_section_wrap">
        <q-item class="col-4 search_left">
          <q-item-section>
            <q-file
              :label="inputFileLabel"
              v-model="inputFile"
              counter
              :counter-label="counterLabelFunction"
              @update:model-value="handleInputFileChange"
              class="input-file"
            >
              <template v-slot:prepend>
                <q-icon name="attach_file" />
              </template>

              <template v-slot:append>
                <q-icon name="close" @click.stop.prevent="handleRemoveInputFile" class="cursor-pointer" />
              </template>
            </q-file>
            <q-checkbox
              :label="$t('위/경도 포함')"
              class="q-ml-lg"
              v-model="isIncludeGeoData"
              :disable="isIncludeGeoDataCheckBoxDisabled"
            />
          </q-item-section>
        </q-item>
        <q-item class="col-8 search_right">
          <q-item-section>
            <filterable-select
              v-model="selectedToolForAutoMapping"
              :options="toolOptions"
              option-value="id"
              option-label="name"
              :label="$t('설비')"
              @update:model-value="loadParametersByToolForAutoMapping"
              style="width: calc(100% - 120px)"
            />
          </q-item-section>
          <q-item-section>
            <q-btn @click="handleAutoMapping" class="sBtn primary">{{ $t('자동 매핑') }}</q-btn>
          </q-item-section>
          <q-item-section>
            <q-btn
              v-permission:dataMigration.create
              @click="handleSaveButtonClick"
              class="save_btn with_icon_btn sBtn secondary"
              >{{ $t('데이터 저장') }}</q-btn
            >
          </q-item-section>
        </q-item>
      </q-card-section>
      <simple-table :rows="rows" :columns="columns" :row-key="rowKey">
        <template #header="props">
          <q-tr :props="props">
            <q-th v-for="(col, index) in props.cols" :key="col.name" :props="props">
              <q-card-section>
                <div>
                  {{ col.label }}
                </div>
                <div v-if="isNotRequiredColumn(col.label, index, isIncludeGeoData)">
                  <q-btn
                    color="secondary"
                    :size="'sm'"
                    :label="$t(`파라미터 매핑`)"
                    @click="handleParameterMappingButtonClick(col.label)"
                  />
                  <div>{{ getParameterName(col.label) }}</div>
                </div>
              </q-card-section>
            </q-th>
          </q-tr>
        </template>
      </simple-table>
    </q-card>
  </q-page>

  <q-dialog v-model="visibleParameterSelect" persistent>
    <q-card style="width: 600px">
      <q-card-section class="row items-center">
        <q-item-section>
          <filterable-select
            style="margin: 0px"
            outlined
            v-model="selectedTool"
            :options="toolOptions"
            option-value="id"
            option-label="name"
            @update:model-value="loadParametersByTool"
            :label="$t('Tool')"
          >
            <template v-slot:prepend>
              <q-icon name="construction" />
            </template>
          </filterable-select>
        </q-item-section>
        <q-item-section>
          <filterable-select
            style="margin: 0px"
            outlined
            v-model="selectedParameter"
            :options="parameterOptions"
            option-value="id"
            option-label="name"
            :label="$t('Parameter')"
            use-input
            input-debounce="300"
            @filter="filterHandler"
            clearable
          >
            <template v-slot:prepend>
              <q-icon name="dataset" />
            </template>
            <template v-slot:no-option>
              <q-item>
                <q-item-section class="text-grey"> {{ $t('선택 가능한 파라미터가 없습니다.') }}</q-item-section>
              </q-item>
            </template>
          </filterable-select>
        </q-item-section>
      </q-card-section>

      <q-card-actions align="right">
        <q-btn flat :label="$t('닫기')" color="primary" v-close-popup />
        <q-btn flat :label="$t('선택')" color="primary" @click="handleParameterSelectOkClick" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import useUI from 'src/common/module/ui'
import DataImportService from 'src/services/modeling/DataImportService.js'
import ToolService from 'src/services/modeling/ToolService'
import ParameterService from 'src/services/modeling/ParameterService'
import { useI18n } from 'vue-i18n'

// columnName: index
// 필수 컬럼 후보
const REQUIRED_COLUMN_CANDIDATES = {
  timestamp: 0,
  latitude: 1,
  longitude: 2,
}

const { t } = useI18n()
const ui = useUI()

const rows = ref([])
const columns = ref([])
const rowKey = ref('')

const visibleParameterSelect = ref(false)
const selectedHeaderName = ref()

const inputFile = ref(null)
const isIncludeGeoData = ref(false)

// key: headerName, value: parameter
const headerParameterMap = ref({})

const toolOptions = ref([])
const selectedTool = ref(null)
const selectedToolForAutoMapping = ref(null)
const parameterOptions = ref([])
const parameterList = ref([])
const selectedParameter = ref(null)

const inputFileSizeString = ref('')
const inputFileLabel = computed(() =>
  inputFile.value ? `${t('선택된 파일')} (${inputFileSizeString.value})` : t('파일을 선택해주세요'),
)
const isIncludeGeoDataCheckBoxDisabled = computed(() => inputFile.value !== null)

const filterHandler = (val, update) => {
  if (val === '') {
    update(() => {
      parameterOptions.value = parameterList.value
    })
    return
  }
  update(() => {
    const needle = val.toLowerCase()
    parameterOptions.value = parameterList.value.filter((v) => v.name.toLowerCase().indexOf(needle) > -1)
  })
}

const counterLabelFunction = ({ totalSize }) => (inputFileSizeString.value = totalSize)

const resetMigrationState = () => {
  inputFile.value = null
  headerParameterMap.value = {}
  columns.value = []
  rows.value = []
}

/**
 * 필수 컬럼 정보를 반환
 * @param isIncludeGeoData 위/경도 포함 여부
 */
const getRequireColumns = (isIncludeGeoData) => {
  if (isIncludeGeoData) {
    return REQUIRED_COLUMN_CANDIDATES
  }
  const timestamp = REQUIRED_COLUMN_CANDIDATES.timestamp
  return { timestamp }
}

const isNotRequiredColumn = (headerName, headerIndex, isIncludeGeoData) => {
  const foundIndex = getRequireColumns(isIncludeGeoData)[headerName]
  if (!foundIndex && headerName !== 'timestamp') {
    return true
  }
  return foundIndex !== headerIndex
}

const handleAutoMapping = () => {
  const autoMapHeadersToParameters = (headers, parameters) => {
    const map = {}
    headers.forEach((header) => {
      const matchedParam = parameters.find((param) => param.name.trim() === header.trim())
      if (matchedParam) {
        map[header] = matchedParam
      }
    })
    return map
  }

  if (columns.value.length > 0 && parameterList.value.length > 0) {
    headerParameterMap.value = autoMapHeadersToParameters(
      columns.value.map((col) => col.name),
      parameterList.value,
    )
    selectedTool.value = selectedToolForAutoMapping.value
    ui.notify.success(t('자동 매핑이 완료되었습니다.'))
  } else {
    ui.notify.warning(t('매핑할 데이터를 먼저 로드하세요.'))
  }
}

const handleInputFileChange = (file) => {
  const checkRequireColumns = (headers) => {
    const withIndexHeaders = headers.reduce((acc, header, index) => {
      acc[header.trim()] = index + 1 // 0인 경우 아래에서 false로 처리되기 때문에 1부터 시작
      return acc
    }, {})

    const requiredColumns = getRequireColumns(isIncludeGeoData.value)
    return Object.keys(requiredColumns).every((column, index) => {
      let foundHeaderIndex = withIndexHeaders[column]
      if (!foundHeaderIndex) {
        return false
      }
      foundHeaderIndex = --foundHeaderIndex // 위에서 1부터 시작했으므로 1을 빼준다
      return index === foundHeaderIndex
    })
  }

  headerParameterMap.value = {}
  const reader = new FileReader()
  reader.onload = (finishedEvent) => {
    let dataSplit = finishedEvent.target.result.split('\n')

    const headerData = dataSplit[0]
      .replace('\r', '')
      .split(',')
      .map((header) => header.trim())
    if (!checkRequireColumns(headerData)) {
      ui.alert(
        t('필수 컬럼 검증 실패'),
        t(`${Object.keys(getRequireColumns(isIncludeGeoData.value))} 컬럼이 순서대로 존재해야 합니다.`),
      )
      resetMigrationState()
      return false
    }

    columns.value = headerData.map((v) => {
      return { name: v, label: v, field: (row) => row[v] }
    })
    rows.value = []
    let datas = []
    const dataLength = dataSplit.length
    for (let i = 1; i < dataLength; i++) {
      const row = dataSplit[i]
        .replace('\r', '')
        .split(',')
        .map((value) => value.trim())
      let data = {}
      for (let iColumn = 0; iColumn < columns.value.length; iColumn++) {
        data[columns.value[iColumn].name] = row[iColumn]
      }
      datas.push(data)

      // 1000개까지만 보여준다
      if (i >= 1000) {
        break
      }
    }
    rows.value = datas
  }
  reader.readAsText(file)
}

const handleRemoveInputFile = () => {
  resetMigrationState()
}

const getParameterName = (headerName) => {
  const parameter = headerParameterMap.value[headerName]
  if (!parameter) {
    return ''
  }
  return `${t('매핑')}: ${parameter.name}`
}

const handleSaveButtonClick = async () => {
  const headerParameterIdMap = Object.keys(headerParameterMap.value).reduce((acc, headerName) => {
    acc[headerName] = headerParameterMap.value[headerName].id
    return acc
  }, {})

  if (Object.keys(headerParameterIdMap).length === 0) {
    ui.notify.warning(t('최소 1개의 파라미터를 매핑해주세요'))
    return
  }

  ui.loading.show()
  const formData = new FormData()
  formData.append('file', inputFile.value)
  formData.append(
    'headerParameterIdMap',
    new Blob([JSON.stringify(headerParameterIdMap)], {
      type: 'application/json',
    }),
  )
  formData.append(
    'isIncludeGeoData',
    new Blob([isIncludeGeoData.value], {
      type: 'application/json',
    }),
  )

  await DataImportService.migration(formData)
  ui.loading.hide()
  ui.notify.success(t('데이터 마이그레이션이 완료되었습니다.'))
}

const handleParameterSelectOkClick = () => {
  visibleParameterSelect.value = false
  headerParameterMap.value[selectedHeaderName.value] = { ...selectedParameter.value }
}

const handleParameterMappingButtonClick = (headerName) => {
  visibleParameterSelect.value = true
  selectedHeaderName.value = headerName
}

const loadParametersByTool = async () => {
  selectedParameter.value = []
  parameterList.value = await ParameterService.getParameters({ toolId: selectedTool.value.id })
  // if (parameterOptions.value.length > 0) {
  //   selectedParameter.value = parameterOptions.value[0]
  // }
}

const loadParametersByToolForAutoMapping = async () => {
  selectedParameter.value = []
  parameterList.value = await ParameterService.getParameters({ toolId: selectedToolForAutoMapping.value.id })
  // if (parameterOptions.value.length > 0) {
  //   selectedParameter.value = parameterOptions.value[0]
  // }
}

const loadTools = async () => {
  toolOptions.value = await ToolService.getTools()
  if (toolOptions.value.length > 0) {
    selectedTool.value = toolOptions.value[0]
    selectedToolForAutoMapping.value = toolOptions.value[0]
    await loadParametersByTool()
  }
}

onMounted(() => {
  loadTools().then(() => {
    loadParametersByTool()
  })
})
</script>

<style scoped lang="scss">
.input-file {
  min-width: 200px;

  :deep(.q-field__bottom) {
    display: none;
  }
}

.header {
  font-weight: bold;
  margin-bottom: 10px;
}

.action {
  display: flex;
  align-items: center;
  padding: 3px;
}

.action_button {
  display: flex;
  gap: 10px;
}

.q-btn {
  min-width: 100px;
}

.q-select {
  min-width: 150px;
  margin-left: 50px;
}
</style>
