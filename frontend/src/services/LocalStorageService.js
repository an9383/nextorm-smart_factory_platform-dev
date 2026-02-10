const LOCALE_KEY = 'locale'
const DASHBOARD_SLIDES_KEY = 'dashboard_slides'

const getItem = (key) => localStorage.getItem(key)
const setItem = (key, value) => localStorage.setItem(key, value)

export default {
  getLocale(defaultLocale) {
    return getItem(LOCALE_KEY) || defaultLocale
  },
  setLocale(locale) {
    setItem(LOCALE_KEY, locale)
  },
  saveDashboardSlides(dashboards) {
    window.localStorage.setItem(DASHBOARD_SLIDES_KEY, dashboards)
  },
  getDashboardSlides() {
    return window.localStorage.getItem(DASHBOARD_SLIDES_KEY)
  },
}
