<template>
  <template v-if="menu.children == null || menu.children.length == 0">
    <q-item
      :to="menu.to"
      :key="menu.to"
      :style="{
        marginLeft: 30 * level + 'px',
      }"
      :class="'eco-subMenu'"
    >
      <q-item-section avatar>
        <q-icon :name="menu.icon" v-if="menu.icon != null && menu.icon.length > 0" color="secondary" />
        <q-avatar v-else size="md" class="menu-text-icon" color="secondary">{{ menu.title[0] || '' }}</q-avatar>
      </q-item-section>
      <q-item-section>
        <q-item-label>{{ $t(menu.title) }}</q-item-label>
      </q-item-section>
    </q-item>
  </template>
  <template v-else>
    <q-expansion-item
      :model-value="isChildActive(menu.children)"
      :label="$t(menu.title)"
      :icon="menu.icon"
      :class="menu.icon == '' ? 'custom-expansion-item' : ''"
      :style="{
        marginLeft: 30 * level + 'px',
      }"
      header-class="text-primary"
    >
      <template v-if="menu.id === 'dashboard'" v-slot:header>
        <q-item-section avatar>
          <q-icon :name="menu.icon" color="primary" />
        </q-item-section>
        <q-item-section class="text-black"> {{ $t(menu.title) }} </q-item-section>
        <q-item-section side>
          <div class="row items-center q-gutter-xs">
            <q-btn flat round text-color="blue" to="/dashboard/slides" @click.stop>
              <q-icon name="mdi-newspaper-variant-multiple" color="blue" />
              <q-tooltip class="text-caption"> {{ $t('슬라이드쇼') }} </q-tooltip>
            </q-btn>
            <q-btn
              v-permission:dashboard.create
              flat
              round
              color="positive"
              icon="add_circle"
              to="/dashboard"
              @click.stop
            >
              <q-tooltip class="text-caption"> {{ $t('대시보드 생성') }} </q-tooltip>
            </q-btn>
            <q-btn
              v-permission:dashboard.view
              flat
              round
              color="grey-7"
              icon="mdi-cog"
              class="dashboard-setting"
              @click.stop="showDashboardManagement"
            >
              <q-tooltip class="text-caption"> {{ $t('대시보드 관리') }} </q-tooltip>
            </q-btn>
            <DashBoardManagePopup
              :visible="showDashboardManagementPopup"
              @close="showDashboardManagementPopup = false"
              @updated="handleDashboardUpdated"
            />
          </div>
        </q-item-section>
      </template>
      <template v-for="subItem of menu.children" :key="subItem.to">
        <nav-item :item="subItem" :level="level + 1" :class="'eco-subMenu'"></nav-item>
      </template>
    </q-expansion-item>
  </template>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore } from 'src/stores/app'
import DashBoardManagePopup from 'components/dashboard/DashBoardManagePopup.vue'

const route = useRoute()
const appStore = useAppStore()

const props = defineProps({ item: Object, level: Number })
const menu = ref(null)
const showDashboardManagementPopup = ref(false)

watch(
  () => props.item,
  (item) => {
    menu.value = { ...item }
    menu.value.expand = isChildActive(menu.value.children)
  },
  { immediate: true },
)

// 대시보드 목록 변경 시 메뉴 업데이트
watch(
  () => appStore.dashboards,
  () => {
    if (menu.value && menu.value.id === 'dashboard') {
      menu.value = { ...props.item }
      menu.value.expand = isChildActive(menu.value.children)
    }
  },
  { deep: true },
)

function isChildActive(children) {
  if (children == null) return false
  for (let item of children) {
    if (item.to == route.path) {
      return true
    }
    if (item.children != null) {
      if (isChildActive(item.children)) {
        return true
      }
    }
  }
  return false
}

const showDashboardManagement = () => {
  showDashboardManagementPopup.value = true
}

const handleDashboardUpdated = () => {
  //대시보드 업데이트 시 새로고침
  appStore.loadDashboards()
}
</script>

<style lang="scss" scoped>
.custom-expansion-item .q-item__section--main {
  padding-left: 56px; /* 아이콘 크기와 맞춰 조정 */
}
.menu-text-icon {
  color: white;
  background: black;
  .q-router-link--active & {
    background: var(--q-primary);
  }
}
:deep(.q-item__section--main) {
  .q-item__label {
    color: black;
  }
}
.dashboard-setting {
  margin-left: -6px;
}
</style>
