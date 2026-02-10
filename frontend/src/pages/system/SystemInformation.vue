<template>
  <q-page class="q-pa-sm">
    <div class="row full-height">
      <div class="col-12">
        <q-card>
          <q-card-section>
            <div class="title_wrap">
              <h3>{{ $t('시스템 정보 수정') }}</h3>
              <span class="field-required-indicate">{{ $t('필수 입력 필드') }}</span>
            </div>
            <q-form ref="form" lazy-validation>
              <q-list>
                <q-item class="q-mb-lg">
                  <q-item-section>
                    <q-item-label class="text-subtitle2 text-weight-bold q-mb-sm field-required">
                      {{ $t('시스템 이름') }}</q-item-label
                    >
                    <q-item-label><q-input v-model="modify.title" :rules="[$rules.required]" /></q-item-label>
                  </q-item-section>
                </q-item>
                <q-separator spaced inset></q-separator>
                <q-item>
                  <q-item-section>
                    <q-item-label class="text-subtitle2 text-weight-bold q-mb-sm">{{ $t('시스템 로고') }}</q-item-label>
                    <q-item-label>
                      <q-avatar
                        text-color="black"
                        class="q-mr-lg rounded-borders cursor-pointer"
                        size="250px"
                        rounded
                        @click="showFileExplorer"
                        ><img v-if="modify.logo" :src="modify.logo" />
                        <img v-else src="/img/widgets/ecotwin-icon/eco-logo-03.svg" alt="default-logo" />
                        <q-tooltip> {{ $t('로고 이미지 등록') }}</q-tooltip>
                      </q-avatar>
                    </q-item-label>
                  </q-item-section>
                </q-item>
              </q-list>
              <q-file
                v-model="modify.logo"
                :label="$t('file')"
                ref="fileInputRef"
                v-show="false"
                accept="image/png,image/jpeg,image/svg+xml"
                :max-file-size="MAX_FILE_SIZE"
                @rejected="handleRejected"
                @update:model-value="onFilesAdded"
              />
            </q-form>
          </q-card-section>
          <q-separator spaced inset></q-separator>
          <q-card-actions align="right">
            <div>
              <q-btn class="sBtn" :label="$t('저장')" @click="handleSaveButton" />
            </div>
          </q-card-actions>
        </q-card>
      </div>
    </div>
  </q-page>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useAppStore } from 'stores/app'
import useUI from 'src/common/module/ui'
import { t } from 'src/plugins/i18n'
import MetaDataService from 'src/services/modeling/MetaDataService'
const MAX_FILE_SIZE = 10 * 1024 * 1024

const store = useAppStore()
const ui = useUI()
const fileInputRef = ref(null)
const form = ref(null)
const modify = ref({ title: null, logo: null })

const changeSystemInfo = async () => {
  store.loadSystemInfo()
}

const loadMetaDataList = async () => {
  ui.loading.show()
  let data = await MetaDataService.getSystemInfo()
  ui.loading.hide()
  modify.value = {
    title: data.title || null,
    logo: data.logo || null,
  }
}

const handleSaveButton = async () => {
  const isValid = await form.value.validate()
  if (!isValid) {
    ui.notify.warning(t('유효하지 않은 입력값이 있습니다.'))
    return false
  } else {
    confirmSave()
  }
}

const handleRejected = (file) => {
  if (file[0].failedPropValidation === 'accept') {
    ui.notify.warning(t('이미지만 업로드 할 수 있습니다. '))
  } else if (file[0].failedPropValidation === 'max-file-size') {
    ui.notify.warning(t('10MB 이상은 업로드 할 수 없습니다. '))
  }
}

const confirmSave = async () => {
  ui.confirm(t('시스템 정보 수정'), t('저장하시겠습니까?'), t('저장'), t('취소')).onOk(async () => {
    const systemInfoMap = [
      { key: 'logo', largeValue: modify.value.logo },
      { key: 'title', value: modify.value.title },
    ].filter((item) => item.largeValue !== null)
    await save(systemInfoMap)
  })
}

const save = async (systemInfoMap) => {
  ui.loading.show()
  try {
    let result = await MetaDataService.modifySystemInfo(systemInfoMap)
    if (result) {
      ui.notify.success(t('저장 되었습니다.'))
      loadMetaDataList()
      changeSystemInfo()
    }
  } finally {
    ui.loading.hide()
  }
}

const showFileExplorer = (e) => {
  fileInputRef.value?.pickFiles(e)
}

const onFilesAdded = (files) => {
  const file = files
  const reader = new FileReader()
  reader.onload = () => {
    modify.value.logo = reader.result
  }
  reader.readAsDataURL(file)
}

onMounted(async () => {
  await loadMetaDataList()
})
</script>
