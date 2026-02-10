import { defineStore } from 'pinia'
import CodeService from 'src/services/system/CodeService'

export const useAPCStore = defineStore('apc', {
  state: () => ({
    conditions: [],
  }),
  getters: {
    apcConditions: (state) => state.conditions,
  },
  actions: {
    async loadConditions() {
      const conditions = await CodeService.getCodesByCategory('APC_CONDITIONS')
      this.conditions = conditions.map((it) => ({ key: it.code, name: it.name }))
    },
  },
})
