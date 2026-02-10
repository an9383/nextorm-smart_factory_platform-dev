<template>
  <q-page class="q-pa-sm">
    <q-card class="cust_subPage">
      <q-card-section class="row search_section_wrap">
        <!-- <div class="text-h5 text-weight-bold q-mt-sm">{{ $t('사용자') }}</div> -->
        <q-item class="search_left"></q-item>
        <q-item class="col-4 search_right">
          <q-item-section>
            <q-btn
              v-permission:user.create
              @click="handleCreateButtonClick"
              class="add_btn with_icon_btn sBtn secondary"
              >{{ $t('추가') }}</q-btn
            >
            <q-btn
              v-permission:user.update
              :disabled="selectedUser == null || selectedRow.length > 1"
              @click="handleModifyButtonClick"
              class="edit_btn with_icon_btn sBtn secondary"
              >{{ $t('수정') }}</q-btn
            >
            <q-btn
              v-permission:user.delete
              :disabled="selectedUser == null"
              @click="handleDeleteButtonClick"
              class="delete_btn with_icon_btn sBtn secondary"
              >{{ $t('삭제') }}</q-btn
            >
          </q-item-section>
          <UserModify v-if="isShowModify" v-model="isShowModify" @save="getUsers" :user-id="userId"></UserModify>
        </q-item>
      </q-card-section>
      <simple-table
        bordered
        :rows="rows"
        :columns="columns"
        row-key="id"
        color="amber"
        selection="single"
        v-model:selected="selectedRow"
      >
        <template v-slot:body="props">
          <q-tr class="cursor-pointer" :props="props" @click="props.selected = !props.selected">
            <q-td v-for="col in props.cols" :key="col.name" :props="props">
              <template v-if="col.name === 'image'">
                <q-avatar color="primary" text-color="white" size="40px">
                  <img v-if="col.value" :src="col.value" />
                  <span v-else> {{ props.row.loginId[0].toUpperCase() }}</span>
                </q-avatar>
              </template>
              <template v-else-if="col.name === 'roleIds'">
                {{ roleNames(col.value) }}
              </template>
              <template v-else-if="col.name === 'token'">
                <div class="row items-center no-wrap">
                  <span>{{ col.value }}</span>
                  <q-btn
                    @click.stop="handleUpdateToken(props.row)"
                    flat
                    round
                    dense
                    size="sm"
                    icon="refresh"
                    class="q-ml-xs"
                  >
                    <q-tooltip>{{ t('토큰 갱신') }}</q-tooltip>
                  </q-btn>
                </div>
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
import UserModify from 'src/pages/system/UserModify.vue'
import useUI from 'src/common/module/ui'
import { t } from 'src/plugins/i18n'
import UserService from 'src/services/system/UserService'
import RoleService from 'src/services/system/RoleService'

const ui = useUI()
const isShowModify = ref(false)
const userId = ref()

const columns = ref([
  { name: 'image', align: 'left', label: t('이미지'), field: 'image' },
  { name: 'loginId', align: 'left', label: t('사용자 아이디'), field: 'loginId' },
  { name: 'name', align: 'left', label: t('사용자 이름'), field: 'name' },
  { name: 'roleIds', align: 'left', label: t('권한들'), field: 'roleIds' },
  { name: 'email', align: 'left', label: t('이메일'), field: 'email' },
  { name: 'phone', align: 'left', label: t('전화'), field: 'phone' },
  { name: 'token', align: 'left', label: t('토큰'), field: 'token' },
])
const rows = ref([])
const selectedRow = ref([])

const roleDatas = ref([])

const selectedUser = computed(() => selectedRow.value[0] || null)

//CREATE
const handleCreateButtonClick = () => {
  userId.value = null
  isShowModify.value = true
}
//MODIFY
const handleModifyButtonClick = () => {
  isShowModify.value = true
  userId.value = selectedUser.value.id
}
//DELETE
const handleDeleteButtonClick = () => {
  const onOkCallback = async () => {
    if (selectedRow.value.length > 1) {
      await deleteUser(selectedRow.value.map((v) => v.id))
    } else {
      await deleteUser([selectedUser.value.id])
    }
  }
  ui.confirm(t('삭제'), t('선택된 항목을 삭제 하시겠습니까?'), t('삭제'), t('취소')).onOk(onOkCallback)
}
const deleteUser = async (userIds) => {
  try {
    ui.loading.show()
    if (userIds.length > 1) {
      await UserService.deleteUsers(userIds)
    } else {
      await UserService.deleteUser(userIds[0])
    }
    ui.notify.success(t('삭제 되었습니다.'))
    getUsers()
  } finally {
    ui.loading.hide()
  }
}

const getRoles = async () => {
  roleDatas.value = await RoleService.getRoles()
}

//권한 이름 필터
const roleNames = (roleIds) => {
  if (roleIds != null) {
    return roleDatas.value
      .filter((role) => roleIds.includes(role.id))
      .map((role) => role.name)
      .join(', ')
  } else {
    return ''
  }
}

const getUsers = async () => {
  ui.loading.show()
  rows.value = await UserService.getUsers()
  ui.loading.hide()
  selectedRow.value = []
}

const handleUpdateToken = async (user) => {
  console.log(user.id)
  const onOkCallback = async () => {
    try {
      ui.loading.show()
      await UserService.updateUserToken(user.id)
      ui.notify.success(t('토큰이 갱신 되었습니다.'))
      await getUsers()
    } catch (error) {
      ui.notify.error(t('토큰 갱신에 실패했습니다.'))
      console.error('Token update failed:', error)
    } finally {
      ui.loading.hide()
    }
  }
  ui.confirm(t('토큰 갱신'), t('토큰을 갱신하겠습니까?'), t('확인'), t('취소')).onOk(onOkCallback)
}

onMounted(async () => {
  await getRoles()
  await getUsers()
})
</script>
