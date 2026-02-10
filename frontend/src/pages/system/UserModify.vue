<template>
  <q-dialog persistent>
    <q-card class="dialog-container">
      <q-card-section class="dialog-title text-white">
        <div class="title">{{ title }}</div>
      </q-card-section>
      <q-card-section class="dialog-contents">
        <q-form ref="form">
          <div class="row items-center">
            <div class="col-auto relative-position">
              <q-avatar
                color="primary"
                text-color="white"
                class="q-mr-lg rounded-borders cursor-pointer"
                size="120px"
                rounded
                @click="showFileExplorer"
                ><img v-if="modify.image" :src="modify.image" />
                <span v-else-if="modify.name">{{ modify.name[0]?.toUpperCase() }}</span>
                <span v-else class="text-caption">{{ $t('No Profile') }}</span>
                <q-tooltip> {{ $t('프로필 이미지 등록') }}</q-tooltip>
              </q-avatar>
              <q-badge
                v-if="modify.image"
                color="red"
                rounded
                floating
                @click.stop="removeImage"
                class="image-remove-badge"
              >
                <q-icon name="close" color="white" />
                <q-tooltip> {{ $t('이미지 제거') }}</q-tooltip>
              </q-badge>
            </div>
            <div class="col flex column" style="gap: 20px">
              <div class="row">
                <q-input
                  v-model="modify.loginId"
                  :label="$t('사용자 아이디')"
                  :rules="[$rules.required]"
                  :disable="props.userId !== null"
                  class="input-required col q-pr-sm"
                />
                <q-input
                  v-model="modify.name"
                  :label="$t('사용자 이름')"
                  :rules="[$rules.required]"
                  class="input-required col q-pr-sm"
                />
              </div>
              <div class="row">
                <q-input
                  v-model="modify.password"
                  type="password"
                  :label="$t('패스워드')"
                  :rules="[$rules.password].concat(!props.userId ? $rules.required : [])"
                  :class="`q-pr-sm col ${!props.userId ? 'input-required' : ''}`"
                />
                <q-input
                  v-model="modify.passwordConfirm"
                  type="password"
                  :label="$t('패스워드 확인')"
                  :rules="[$rules.passwordConfirm(modify.password)]"
                  :class="`q-pr-sm col ${!props.userId ? 'input-required' : ''}`"
                />
              </div>
            </div>
          </div>
          <q-file
            v-model="modify.image"
            :label="$t('file')"
            ref="fileInputRef"
            v-show="false"
            accept="image/png,image/jpeg,image/svg+xml"
            :max-file-size="MAX_FILE_SIZE"
            @rejected="handleRejected"
            @update:model-value="onFilesAdded"
          />
          <div class="row">
            <q-input v-model="modify.email" :label="$t('이메일')" :rules="[$rules.email]" class="col-6 q-pr-sm" />
            <q-input v-model="modify.phone" :label="$t('전화')" :rules="[$rules.phoneNumber]" class="col-6 q-pr-sm" />
          </div>
          <div class="row">
            <q-table
              bordered
              :rows="roleRows"
              :columns="columns"
              :pagination="{ rowsPerPage: 0 }"
              row-key="id"
              v-model:selected="selectedRow"
              hide-bottom
              selection="multiple"
              class="col-12"
              virtual-scroll
              :virtual-scroll-item-size="40"
              style="max-height: 180px"
            />
          </div>
        </q-form>
      </q-card-section>
      <q-separator />
      <q-card-actions class="justify-between">
        <span class="field-required-indicate">{{ $t('필수 입력 필드') }}</span>
        <div>
          <q-btn flat color="negative" :label="$t('닫기')" @click="closeDialog" />
          <q-btn v-if="!props.userId" flat color="primary" :label="$t('저장')" @click="handleSaveButton" />
          <q-btn v-else flat color="primary" :label="$t('수정')" @click="handleModifyButton" />
        </div>
      </q-card-actions>
      <inner-loading v-model="isLoading" />
    </q-card>
  </q-dialog>
</template>
<script setup>
import { computed, defineEmits, defineProps, onMounted, ref } from 'vue'
import useUI from 'src/common/module/ui'
import { t } from '/src/plugins/i18n'
import UserService from 'src/services/system/UserService'
import RoleService from 'src/services/system/RoleService'

const MAX_FILE_SIZE = 3 * 1024 * 1024

const props = defineProps({
  userId: Number,
})
const ui = useUI()

const emit = defineEmits(['save'])
const form = ref(null)
const isLoading = ref(false)
const modify = ref({})
const modifyUserId = ref(null)
const fileInputRef = ref(null)
const selectedRow = ref([])

const title = computed(() => (props.userId ? t('사용자 수정') : t('사용자 등록')))

const columns = ref([
  { name: 'name', align: 'left', label: t('이름'), field: 'name' },
  { name: 'description', align: 'left', label: t('설명'), field: 'description' },
])

const removeImage = () => {
  modify.value.image = null
}

const roleRows = ref([])
//아바타 클릭 시 q-file 보여주기
const showFileExplorer = (e) => {
  fileInputRef.value?.pickFiles(e)
}
const onFilesAdded = (files) => {
  const file = files
  const reader = new FileReader()
  reader.onload = () => {
    modify.value.image = reader.result
  }
  reader.readAsDataURL(file)
}

const handleRejected = (file) => {
  if (file[0].failedPropValidation === 'accept') {
    ui.notify.warning(t('이미지만 업로드 할 수 있습니다. '))
  } else if (file[0].failedPropValidation === 'max-file-size') {
    ui.notify.warning(t('3MB 이상은 업로드 할 수 없습니다. '))
  }
}

const initUserData = async () => {
  if (props.userId) {
    try {
      isLoading.value = true
      modifyUserId.value = props.userId
      modify.value = await UserService.getUser(props.userId)
      if (modify.value.roleIds != null) {
        selectedRow.value = roleRows.value.filter((role) => modify.value.roleIds.includes(role.id))
      } else {
        selectedRow.value = []
      }
    } finally {
      isLoading.value = false
    }
  } else {
    selectedRow.value = []
    modifyUserId.value = null
  }
}

const handleSaveButton = async () => {
  const body = await validate()
  if (body) {
    try {
      modify.value.roleIds = selectedRow.value.map((v) => v.id)
      isLoading.value = true
      await UserService.createUser(modify.value)
      closeDialog()
      ui.notify.success(t('저장 되었습니다.'))
      emit('save')
    } finally {
      isLoading.value = false
    }
  }
}
const handleModifyButton = async () => {
  const body = await validate()
  if (body) {
    try {
      modify.value.roleIds = selectedRow.value.map((v) => v.id)
      isLoading.value = true
      await UserService.modifyUser(modifyUserId.value, modify.value)
      ui.notify.success(t('수정 되었습니다.'))
      closeDialog()
      emit('save')
    } finally {
      isLoading.value = false
    }
  }
}

const validate = async () => {
  const success = await form.value.validate()
  if (!success) {
    ui.notify.warning(t('유효하지 않은 입력값이 있습니다.'))
    return null
  }
  return true
}

const closeDialog = () => {
  modify.value = {}
  emit('update:modelValue', false)
}

onMounted(async () => {
  roleRows.value = await RoleService.getRoles()
  await initUserData()
})
</script>
<style scoped>
.dialog-container {
  width: 700px;
  max-width: 900px;
}

.image-remove-badge {
  cursor: pointer;
  top: -8px;
  right: 15px;
  padding-top: 6px;
  padding-bottom: 6px;
}
</style>
