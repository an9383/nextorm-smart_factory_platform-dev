<template>
  <div class="row full-height">
    <div class="full-height full-width">
      <fast-trend-chart
        :parameter-ids="parameterIds"
        :from-date="fromDateTime"
        :to-date="toDateTime"
        :use-brush="true"
        @selectedDataRange="handleSelectedDataRange"
        @changeChartData="handleChartDataChange"
      />
    </div>
  </div>
</template>

<script setup>
import FastTrendChart from 'pages/ai/modeling/parameterSelectStep/FastTrendChart.vue'
import { computed, ref } from 'vue'

const props = defineProps({
  selectedXParameterIds: {
    type: Array,
    required: true,
  },
  selectedYParameterId: {
    type: Number,
    required: true,
  },
  fromDateTime: {
    type: Date,
    required: true,
  },
  toDateTime: {
    type: Date,
    required: true,
  },
})

const selectedDataRange = ref(null)
const parameterIds = computed(() => {
  return [props.selectedYParameterId, ...props.selectedXParameterIds]
})

const handleSelectedDataRange = ({ from, to, data }) => {
  selectedDataRange.value = { from, to, data }
}

const handleChartDataChange = (data) => {
  console.log('차트 데이터 변경:', data)
}

defineExpose({
  getDataRange: () => selectedDataRange.value,
})
</script>

<style scoped></style>
