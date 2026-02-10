<template>
  <q-page class="q-pa-md page">
    <q-card class="cust_subPage">
      <q-card-section class="row search_section_wrap">
        <!-- <div class="text-h5 text-weight-bold q-mt-sm" style="display: inline">{{ $t('다국어 관리') }}</div> -->
        <q-item class="col-3 search_left">
          <q-item-section>
            <q-option-group
              class="q-mr-sm"
              style="display: inline-flex"
              v-model="messageType"
              @update:modelValue="handleMessageTypeChange"
              inline
              :options="messageTypeOptions"
            />
          </q-item-section>
        </q-item>
        <q-item class="col-8 search_right">
          <q-item-section>
            <q-btn @click="handleSaveButtonClick" class="save_btn with_icon_btn sBtn secondary">{{
              $t('변경사항 저장')
            }}</q-btn>
            <q-btn
              v-permission:i18n.create
              v-if="isNormalMessageTypeMode"
              @click="handleAddRowButtonClick"
              class="add_btn with_icon_btn sBtn secondary"
              >{{ $t('행 추가') }}</q-btn
            >
            <q-btn
              v-permission:i18n.delete
              :disable="isDisableDeleteButton"
              @click="removeRow"
              class="delete_btn with_icon_btn sBtn secondary"
              >{{ $t('삭제') }}</q-btn
            >
            <q-btn
              :disable="isDisableDeleteButton"
              @click="isShowTranslationPopup = true"
              class="add_btn with_icon_btn sBtn secondary"
              >{{ $t('자동 번역') }}</q-btn
            >
          </q-item-section>
        </q-item>
      </q-card-section>

      <message-table
        :languages="languages"
        v-model:rows="tableRows"
        v-model:messages="messages"
        v-model:selected-rows="selectedRows"
      />
      <q-dialog v-model="isShowTranslationPopup" persistent>
        <q-card class="dialog-container">
          <q-card-section class="bg-secondary text-white">
            <div class="text-h6">{{ $t('자동 번역') }}</div>
          </q-card-section>
          <q-card-section>
            <filterable-select
              filled
              v-model="language"
              :options="optionLanguages"
              :label="$t('기준언어')"
              option-value="key"
              option-label="value"
              :rules="[$rules.required]"
              class="input-required q-pr-sm q-pb-none"
            />
          </q-card-section>
          <q-card-section>
            <div class="field-required-indicate q-pb-md row">{{ $t('필수 입력 필드') }}</div>
            <div class="absolute-right row">
              <q-btn
                flat
                color="negative"
                class="q-mr-sm"
                :label="$t('닫기')"
                @click="isShowTranslationPopup = false"
              />
              <q-btn flat color="primary" :label="$t('번역')" @click="translateMessage" />
            </div>
          </q-card-section>
        </q-card>
      </q-dialog>
    </q-card>
  </q-page>
</template>

<script setup>
import { computed, onBeforeMount, ref } from 'vue'
import I18nService from 'src/services/system/I18nService'
import MetaDataService from 'src/services/modeling/MetaDataService'
import useUI from 'src/common/module/ui'
import { PARAMETER_KEY_PREFIX, t } from 'src/plugins/i18n'
import MessageTable from 'pages/system/i18n/MessageTable.vue'
import ParameterService from 'src/services/modeling/ParameterService'

const NEW_KEY_PREFIX = 'newKey__'
const MESSAGE_TYPE_NORMAL = 'NORMAL'
const MESSAGE_TYPE_PARAMETER = 'PARAMETER'

const messageTypeOptions = [
  { label: t('일반'), value: MESSAGE_TYPE_NORMAL },
  { label: t('파라미터'), value: MESSAGE_TYPE_PARAMETER },
]

const ui = useUI()
const isShowTranslationPopup = ref(false)
const messageType = ref(MESSAGE_TYPE_NORMAL)
const messages = ref({}) // 메시지 관리 타입에 따라 필터링된 메시지

const languages = ref([])
const language = ref(null)
const originalMessages = ref({}) // api로 가져온 메시지 자체

const tableRows = ref([])
const selectedRows = ref([])
const removeKeys = ref([])
const optionLanguages = ref([])

const isNormalMessageTypeMode = computed(() => messageType.value === MESSAGE_TYPE_NORMAL)
const isDisableDeleteButton = computed(() => selectedRows.value.length === 0)

const getNotDefineParameterTableRows = async (languages, definedParameterKeys) => {
  const parameterKeySet = new Set(definedParameterKeys)
  const parameters = await ParameterService.getParameters({})

  const notIncludeParameterKeys = [
    ...parameters
      .map((param) => param.name)
      .filter((parameterKey) => !parameterKeySet.has(parameterKey))
      .reduce((acc, parameterKey) => acc.add(parameterKey), new Set()),
  ]

  return notIncludeParameterKeys.map((parameterKey, index) => {
    const row = createNewRow(parameterKey)
    row.id = `${PARAMETER_KEY_PREFIX}${row.id}_${index}`
    languages.forEach((lang) => {
      row[lang] = parameterKey
    })
    return row
  })
}

const handleMessageTypeChange = (type) => {
  const handleOnOk = async () => {
    let filteredMessages = filteredMessageByType(messageType.value, originalMessages.value)
    const isNormalMessageTypeMode = messageType.value === MESSAGE_TYPE_NORMAL

    removeKeys.value = []
    messages.value = filteredMessages

    if (isNormalMessageTypeMode) {
      tableRows.value = createRows(languages.value, messages.value, messageType.value)
      return
    }

    tableRows.value = [
      ...createRows(languages.value, filteredMessages, messageType.value),
      ...(await getNotDefineParameterTableRows(languages.value, Object.keys(filteredMessages))),
    ]
  }

  // 현재는 타입이 두가지 뿐이므로 이렇게 처리.. 타입이 늘어나면 방식을 바꿀 필요가 있음
  const handleOnCancel = () => {
    if (type === MESSAGE_TYPE_NORMAL) {
      messageType.value = MESSAGE_TYPE_PARAMETER
    } else {
      messageType.value = MESSAGE_TYPE_NORMAL
    }
  }

  ui.confirm(
    t('안내'),
    t('저장하지 않은 내용이 있는 경우, 저장하지 않은 내용은 초기화 됩니다. 타입을 변경하시겠습니까?'),
  )
    .onCancel(handleOnCancel)
    .onOk(handleOnOk)
}
//선택한 데이터 번역 부분
const translateMessage = async () => {
  if (language.value) {
    const targetLanguages = optionLanguages.value.filter((v) => v.key !== language.value.key).map((lang) => lang.key)
    const baseLanguage = language.value.key // 현재 선택된 언어의 키

    const noMessageRow = selectedRows.value.find((row) => row[baseLanguage] === undefined || row[baseLanguage] === '')

    if (noMessageRow !== undefined) {
      ui.notify.warning(t('선택한 기준언어에 빈값이 있습니다.'))
      return
    }

    const translationData = selectedRows.value.map((row) => row[baseLanguage])
    ui.loading.show()
    const translateResults = await Promise.all(
      targetLanguages.map((lang) => I18nService.translation(lang.toUpperCase(), translationData)),
    )

    const newTableRows = [...tableRows.value]

    targetLanguages.forEach((lang, index) => {
      const translatedMsgs = translateResults[index].translations
      selectedRows.value.forEach((selectedRow, index) => {
        const updatedRow = newTableRows.find((row) => row.key === selectedRow.key)
        if (updatedRow) {
          updatedRow[lang] = translatedMsgs[index].text
          updatedRow.isEdited = true
        }
      })
    })

    tableRows.value = newTableRows

    ui.loading.hide()
    ui.notify.success(t('번역이 완료 되었습니다.'))
    isShowTranslationPopup.value = false
  } else {
    ui.notify.warning(t('기준 언어를 선택해주세요.'))
  }
}

/**
 *
 * @param key {key=}
 */
const createNewRow = (key = 'newKey') => {
  return {
    id: `${NEW_KEY_PREFIX}${Date.now()}`,
    key: key,
    isNew: true,
    isEdited: false,
  }
}

const handleAddRowButtonClick = () => {
  tableRows.value = [...tableRows.value, createNewRow()]
}

const removeRow = () => {
  ui.confirm(t('삭제 하시겠습니까?'), t('[변경사항 저장]을 실행해야 반영됩니다')).onOk(() => {
    const selectedRowsIdSet = new Set(selectedRows.value.map((row) => row.id))
    tableRows.value = tableRows.value.filter((row) => !selectedRowsIdSet.has(row.id))
    selectedRows.value = []

    const excludeNewKeys = [...selectedRowsIdSet].filter((key) => !key.startsWith(NEW_KEY_PREFIX))
    removeKeys.value = [...removeKeys.value, ...excludeNewKeys]

    let copyOriginalMessages = { ...originalMessages.value }
    for (let key of excludeNewKeys) {
      delete copyOriginalMessages[key]
    }
    originalMessages.value = copyOriginalMessages
  })
}

const validEmptyCell = (newRows, editedRows) => {
  const checkCells = ['key', ...languages.value]

  let isEmptyCell = false
  for (let row of newRows) {
    for (let cell of checkCells) {
      const cellValue = row[cell]
      if (!cellValue || cellValue.trim() === '') {
        isEmptyCell = true
        break
      }
    }
  }

  if (isEmptyCell) {
    return isEmptyCell
  }

  for (let row of editedRows) {
    for (let cell of checkCells) {
      const cellValue = row[cell]
      if (!cellValue || cellValue.trim() === '') {
        isEmptyCell = true
        break
      }
    }
  }
  return isEmptyCell
}

const validDuplicateKey = (newRows) => {
  let originalKeySet = new Set(Object.keys(originalMessages.value))
  const newKeys = newRows.map((v) => v.key)
  let isDuplicateKey = false
  for (let key of newKeys) {
    if (originalKeySet.has(key)) {
      isDuplicateKey = true
      break
    }
    originalKeySet.add(key)
  }
  return isDuplicateKey
}

const handleSaveButtonClick = async () => {
  const newRows = tableRows.value.filter((row) => row.isNew)
  const editedRows = tableRows.value.filter((row) => row.isEdited && !row.isNew)

  const modifyCount = newRows.length + editedRows.length + removeKeys.value.length
  if (modifyCount === 0) {
    ui.notify.info(t('변경사항이 없습니다.'))
    return
  }

  const isEmptyCell = validEmptyCell(newRows, editedRows)
  if (isEmptyCell) {
    ui.notify.warning(t('입력되지 않은 셀이 존재합니다.'))
    return
  }

  const isDuplicateKey = validDuplicateKey(newRows)
  if (isDuplicateKey) {
    ui.notify.warning(t('중복된 키가 존재합니다.'))
    return
  }

  // 현재 작업중인 메시지 타입이 Parameter인 경우에는 prefix를 추가해야한다
  const decorateKey = (key) => ifMessageTypeIsParameterThanDecoratePrefix(key, messageType.value)

  const newMessages = newRows.flatMap((row) =>
    languages.value.map((lang) => ({
      key: decorateKey(row.key),
      lang,
      message: row[lang],
    })),
  )

  const updateMessages = editedRows.flatMap((row) =>
    languages.value.map((lang) => ({
      key: decorateKey(row.key),
      lang,
      message: row[lang],
    })),
  )

  const confirmMessage = `
  ${t('신규')}: ${newRows.length}
  <br/>${t('수정')}: ${editedRows.length}
  <br/>${t('삭제')}: ${removeKeys.value.length}
  `

  const onOkCallback = async () => {
    await I18nService.updateMessages(newMessages, updateMessages, removeKeys.value)
    ui.notify.success(t('변경사항이 반영되었습니다.'))
    await init(languages.value)
  }

  ui.confirm(t('변경사항을 저장하시겠습니까?'), confirmMessage, t('저장'), t('취소'), { html: true }).onOk(onOkCallback)
}

const filteredMessageByType = (messageType, messages) => {
  const isParameterFilterMessagesMode = messageType === MESSAGE_TYPE_PARAMETER

  const isParameterKey = (key) => key.startsWith(PARAMETER_KEY_PREFIX)
  const isNotParameterKey = (key) => !isParameterKey(key)
  const predicate = isParameterFilterMessagesMode ? isParameterKey : isNotParameterKey

  return Object.keys(messages)
    .filter(predicate)
    .reduce((acc, currentKey) => {
      let newKey = currentKey
      if (isParameterFilterMessagesMode) {
        newKey = currentKey.slice(PARAMETER_KEY_PREFIX.length)
      }
      acc[newKey] = messages[currentKey]
      return acc
    }, {})
}

const ifMessageTypeIsParameterThanDecoratePrefix = (key, messageType) =>
  messageType === MESSAGE_TYPE_NORMAL ? key : `${PARAMETER_KEY_PREFIX}${key}`

const createRows = (languages, messages, messageType) => {
  const rows = []

  for (let key in messages) {
    const message = messages[key]

    let row = {
      id: ifMessageTypeIsParameterThanDecoratePrefix(key, messageType),
      key: key,
      isNew: false,
      isEdited: false,
    }
    languages.forEach((lang) => {
      row[lang] = message[lang]
    })
    rows.push(row)
  }
  return rows
}

const init = async (languages) => {
  originalMessages.value = await I18nService.getMessages()
  let filteredMessages = filteredMessageByType(messageType.value, originalMessages.value)

  tableRows.value = createRows(languages, filteredMessages, messageType.value)
  messages.value = filteredMessages
  removeKeys.value = []
}

onBeforeMount(async () => {
  languages.value = await MetaDataService.getI18nLanguages()
  optionLanguages.value = languages.value.map((k) => ({ key: k, value: t(k) }))
  await init(languages.value)
})
</script>

<style lang="scss" scoped>
.page {
  position: relative;
}

.table-wrapper {
  /* 헤더 사이즈 및 padding을 제외하여 사이즈 계산 */
  height: calc(100% - 44px - 32px) !important;
  height: -moz-calc(100% - 44px - 32px) !important;
  height: -webkit-calc(100% - 44px - 32px) !important;

  width: calc(100% - 32px);
  position: absolute;
}
.dialog-container {
  width: 500px;
  max-width: 900px;
}
</style>
