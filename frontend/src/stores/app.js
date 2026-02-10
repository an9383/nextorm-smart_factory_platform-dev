import { defineStore } from 'pinia'
import DashboardService from 'src/services/dashboard/DashboardService'
import MetaDataService from 'src/services/modeling/MetaDataService'

const DEFAULT_SYSTEM_INFO = {
  title: 'Smart Factory Platform',
  logo: '/img/widgets/ecotwin-icon/eco-logo-03.svg',
}

export const useAppStore = defineStore('app', {
  state: () => ({
    dashboards: [],
    cachedLanguages: [],
    systemInfo: DEFAULT_SYSTEM_INFO,
    showDashboardManagementPopup: false,
  }),
  getters: {
    dashboardsList: (state) => state.dashboards,
    title: (state) => state.systemInfo.title,
    logo: (state) => (state.systemInfo.logo ? state.systemInfo.logo : DEFAULT_SYSTEM_INFO.logo),
  },
  actions: {
    async loadDashboards() {
      this.dashboards = await DashboardService.getDashboards({ isHide: false })
    },
    async loadSystemInfo() {
      const systemInfo = await MetaDataService.getSystemInfo()
      const isNull = Object.keys(systemInfo).length === 0
      this.systemInfo = isNull ? DEFAULT_SYSTEM_INFO : systemInfo
      const { title, logo } = this.systemInfo
      document.title = title
      const link = document.querySelector('link[rel="icon"]')
      if (logo && link) {
        link.href = logo
      }
    },
    putCachedLanguage(language) {
      this.cachedLanguages = [...this.cachedLanguages, language]
    },
    isCachedLanguage(language) {
      return this.cachedLanguages.includes(language)
    },
  },
})
