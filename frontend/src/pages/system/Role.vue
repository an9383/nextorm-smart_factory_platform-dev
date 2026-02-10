<template>
  <q-page class="q-pa-sm">
    <q-card class="cust_subPage">
      <q-card-section class="row search_section_wrap">
        <!-- <div class="text-h5 text-weight-bold q-mt-sm">{{ $t('권한') }}</div> -->
        <q-item class="search_left"></q-item>
        <q-item class="col-4 search_right">
          <q-item-section>
            <q-btn v-permission:role.create @click="showModifyPopup()" class="add_btn with_icon_btn sBtn secondary">{{
              $t('추가')
            }}</q-btn>
            <q-btn
              v-permission:role.update
              :disabled="selectedRoles.length !== 1"
              @click="showModifyPopup(selectedRoles[0]?.id)"
              class="edit_btn with_icon_btn sBtn secondary"
              >{{ $t('수정') }}</q-btn
            >
            <q-btn
              v-permission:role.delete
              :disabled="selectedRoles.length === 0"
              @click="confirmDelete"
              class="delete_btn with_icon_btn sBtn secondary"
              >{{ $t('삭제') }}</q-btn
            >
          </q-item-section>
        </q-item>
      </q-card-section>
      <simple-table
        bordered
        :rows="roles"
        :columns="columns"
        row-key="id"
        selection="multiple"
        v-model:selected="selectedRoles"
      >
        <template v-slot:body="props">
          <q-tr class="cursor-pointer" :props="props" @click="props.selected = !props.selected">
            <q-td v-for="col in props.cols" :key="col.name" :props="props">
              {{ col.value }}
            </q-td>
          </q-tr>
        </template>
      </simple-table>
      <RoleModify
        v-if="modifyPopup.isShow"
        v-model="modifyPopup.isShow"
        @save="loadRoles"
        :role-id="modifyPopup.roleId"
      ></RoleModify>
    </q-card>
  </q-page>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { t } from 'src/plugins/i18n'
import useUI from 'src/common/module/ui'
import RoleService from 'src/services/system/RoleService'
import RoleModify from './RoleModify.vue'

const ui = useUI()
const columns = [
  { name: 'name', label: t('권한명'), field: 'name', align: 'left' },
  { name: 'description', label: t('설명'), field: 'description', align: 'left' },
]

const roles = ref([])
const selectedRoles = ref([])
const modifyPopup = ref({
  isShow: false,
  roleId: undefined,
})

const showModifyPopup = (roleId) => {
  modifyPopup.value = {
    roleId,
    isShow: true,
  }
}

const confirmDelete = async () => {
  ui.confirm(t('권한 삭제'), t('선택한 권한을 삭제 하시겠습니까?'), t('삭제')).onOk(async () => {
    try {
      ui.loading.show()
      if (selectedRoles.value.length === 1) {
        await RoleService.deleteRole(selectedRoles.value[0].id)
      } else {
        await RoleService.deleteRoles(selectedRoles.value.map((role) => role.id))
      }
      ui.notify.success(t('삭제 되었습니다.'))
      await loadRoles()
    } finally {
      ui.loading.hide()
    }
  })
}

const loadRoles = async () => {
  selectedRoles.value = []
  ui.loading.show()
  roles.value = await RoleService.getRoles()
  ui.loading.hide()
}

onMounted(loadRoles)
</script>
