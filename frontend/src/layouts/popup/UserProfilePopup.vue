<template>
  <q-dialog persistent>
    <q-card class="dialog-container">
      <q-card-section class="bg-secondary text-white">
        <div class="text-h6">{{ $t('사용자 정보 수정') }}</div>
      </q-card-section>
      <q-card-section>
        <q-form ref="form">
          <div class="row items-center">
            <div class="col-auto relative-position">
              <div class="row">
                <q-avatar
                  color="primary"
                  text-color="white"
                  class="q-mr-lg rounded-borders cursor-pointer"
                  size="100px"
                  rounded
                  @click="showFileExplorer"
                  ><img v-if="modify.image" :src="modify.image" />
                  <span v-else-if="modify.name">{{ modify.name[0]?.toUpperCase() }}</span>
                  <span v-else class="text-caption">No Profile</span>
                  <q-tooltip> {{ $t('프로필 이미지 등록') }} </q-tooltip>
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
                  <q-tooltip> {{ $t('이미지 제거') }} </q-tooltip>
                </q-badge>
              </div>
              <div class="row q-pt-sm">
                <q-btn
                  color="grey-6"
                  :label="$t('비밀번호 변경')"
                  class="q-px-sm text-caption"
                  @click="isShowChangePasswordPopup = true"
                  ><q-icon size="1em" name="mdi-lock"></q-icon
                ></q-btn>
              </div>
            </div>
            <div class="col">
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
                <q-input v-model="modify.email" :label="$t('이메일')" :rules="[$rules.email]" class="col-6 q-pr-sm" />
                <q-input
                  v-model="modify.phone"
                  :label="$t('전화')"
                  :rules="[$rules.phoneNumber]"
                  class="col-6 q-pr-sm"
                />
              </div>
            </div>
          </div>
          <q-file
            v-model="modify.image"
            label="file"
            ref="fileInputRef"
            v-show="false"
            accept="image/*"
            @update:model-value="onFilesAdded"
          />
        </q-form>
      </q-card-section>
      <q-separator />
      <q-card-actions class="justify-between">
        <span class="field-required-indicate">{{ $t('필수 항목') }}</span>
        <div>
          <q-btn flat color="negative" :label="$t('닫기')" @click="closeDialog" />
          <q-btn flat color="primary" :label="$t('수정')" @click="handleModifyButton" />
        </div>
      </q-card-actions>
      <inner-loading v-model="isLoading" />
    </q-card>
    <q-dialog v-if="isShowChangePasswordPopup" v-model="isShowChangePasswordPopup">
      <q-card class="confirm-dialog">
        <q-card-section>
          <div class="text-h6">{{ $t('비밀번호 변경') }}</div>
        </q-card-section>

        <q-card-section class="q-pt-none">
          <q-input
            v-model="confirmPassword"
            type="password"
            :label="$t('현재 비밀번호')"
            :rules="[$rules.required]"
            class="q-pr-sm col input-required"
          />
          <q-input
            v-model="modify.password"
            type="password"
            :label="$t('새 비밀번호')"
            :rules="[$rules.password, $rules.required]"
            class="q-pr-sm col input-required"
          />
          <q-input
            v-model="modify.passwordConfirm"
            type="password"
            :label="$t('새 비밀번호 확인')"
            :rules="[$rules.passwordConfirm(modify.password)]"
            class="q-pr-sm col input-required"
          />
        </q-card-section>
        <q-separator />
        <q-card-actions class="justify-between">
          <span class="field-required-indicate">{{ $t('필수 항목') }}</span>
          <div>
            <q-btn flat color="red" :label="$t('닫기')" @click="onClickisShowChangePasswordClosePopup" />
            <q-btn flat color="primary" :label="$t('변경')" @click="onClickisShowChangePasswordPopup" />
          </div>
        </q-card-actions>
      </q-card>
    </q-dialog>
  </q-dialog>
</template>
<script setup>
import { defineProps, defineEmits, ref, onMounted } from 'vue'
import useUI from 'src/common/module/ui'
import { t } from '/src/plugins/i18n'
import UserService from 'src/services/system/UserService'
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
const isShowChangePasswordPopup = ref(false)
const confirmPassword = ref('')

const removeImage = () => {
  modify.value.image = null
}

const onClickisShowChangePasswordPopup = async () => {
  const userChangePassword = { changePassword: modify.value.password, currentPassword: confirmPassword.value }
  if (await UserService.changePassword(userChangePassword)) {
    isShowChangePasswordPopup.value = false
    modify.value.passwordConfirm = ''
    modify.value.password = ''
    confirmPassword.value = ''
    ui.notify.success('비밀번호가 변경 되었습니다.')
  } else {
    ui.notify.warning(t('현재 비밀번호가 일치하지 않습니다.'))
    return
  }
}
const onClickisShowChangePasswordClosePopup = () => {
  isShowChangePasswordPopup.value = false
  modify.value.passwordConfirm = ''
  modify.value.password = ''
  confirmPassword.value = ''
}

//아바타 클릭 시 q-file 보여주기
const showFileExplorer = (e) => {
  fileInputRef.value?.pickFiles(e)
}
const onFilesAdded = (files) => {
  const file = files
  if (file.size > 3 * 1024 * 1024) {
    ui.notify.warning(t('3MB 이상은 업로드 할 수 없습니다. '))
    modify.value.image = null
  } else {
    const reader = new FileReader()
    reader.onload = () => {
      modify.value.image = reader.result
    }
    reader.readAsDataURL(file)
  }
}

const initUserData = async () => {
  isLoading.value = true
  modify.value = await UserService.getUser(props.userId)
  modifyUserId.value = props.userId
  isLoading.value = false
}

const handleModifyButton = async () => {
  const body = await validate()
  if (body) {
    try {
      isLoading.value = true
      await UserService.modifyUser(modifyUserId.value, modify.value)
      ui.notify.success('수정 되었습니다.')
      emit('save')
      closeDialog()
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
.confirm-dialog {
  width: 400px;
  height: 365px;
}
</style>
