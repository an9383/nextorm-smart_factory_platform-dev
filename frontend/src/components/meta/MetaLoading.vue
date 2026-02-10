<script setup>
import { QSpinnerGears, useQuasar } from 'quasar'
import { onMounted, onUnmounted } from 'vue'

onMounted(() => {
  showLoading()
})

onUnmounted(() => {
  hideLoading()
})

const $q = useQuasar()
const showLoading = () => {
  $q.loading.show({
    spinner: QSpinnerGears,
    message: 'Loading...',
    spinnerSize: 100,
    boxClass: 'bg-grey-2 text-grey-8 text-bold',
    spinnerColor: 'primary',
  })
}
const hideLoading = () => {
  $q.loading.hide()
}
</script>

<style scoped></style>
