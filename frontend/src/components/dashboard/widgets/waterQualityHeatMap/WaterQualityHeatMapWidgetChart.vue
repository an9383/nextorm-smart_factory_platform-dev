<template>
  <q-dialog persistent>
    <q-card class="dialog-container">
      <q-card-section class="row">
        <div class="text-h6">{{ $t('Health Grade Chart') }}</div>
        <q-space />
        <q-btn icon="close" flat round dense @click="onCancel" />
      </q-card-section>
      <q-separator />
      <ECharts id="echart" ref="chart" :option="options" class="q-mt-md" :resizable="true" />
      <q-card-section>
        <div class="text-subtitle1 text-bold text-center">
          <q-chip color="primary" text-color="white" icon="pin_drop"> {{ $t('latitude') }} : {{ latitude }}</q-chip>
          <q-chip color="secondary" text-color="white" icon="pin_drop"> {{ $t('Longitude') }} : {{ longitude }}</q-chip>
        </div>
      </q-card-section>
      <q-separator />
      <q-card-actions class="justify-between">
        <div>
          <q-btn flat color="negative" :label="$t('닫기')" @click="onCancel" />
        </div>
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup>
import { defineEmits, ref, watch } from 'vue'
import { pt } from '/src/plugins/i18n'
import ECharts from 'vue-echarts'
import { graphic, use } from 'echarts/core'
import { RadarChart } from 'echarts/charts'
import { CanvasRenderer } from 'echarts/renderers'

use([RadarChart, CanvasRenderer])

const show = ref(false)

const props = defineProps({
  latitude: {
    type: Number,
    required: true,
  },
  longitude: {
    type: Number,
    required: true,
  },
  healthData: {
    type: Object,
    required: true,
  },
})

watch(props, () => {
  getParameterDataInLatLng(props.healthData)
})

const emit = defineEmits(['close'])
const options = ref({
  color: ['#5b6991'],
  radar: [
    {
      indicator: [],
      shape: 'circle',
      radius: 200,
      axisName: {
        color: '#fff',
        backgroundColor: '#666',
        borderRadius: 3,
        padding: [3, 5],
      },
      splitArea: {
        areaStyle: {
          opacity: 0.65,
          color: {
            type: 'radial',
            colorStops: [
              {
                offset: 0.3,
                color: '#FF0000',
              },
              {
                offset: 0.6,
                color: '#FF8000',
              },
              {
                offset: 1,
                color: '#3ad33a',
              },
            ],
          },
          shadowColor: 'rgba(0, 0, 0, 0.8)',
          shadowBlur: 100,
        },
      },
    },
  ],
  series: [
    {
      type: 'radar',
      data: [
        {
          value: [],
          name: 'Health Point',
          areaStyle: {
            color: new graphic.RadialGradient(0.3, 0.6, 1, [
              {
                color: 'rgba(0,0,0,0.1)',
                offset: 1,
              },
              {
                color: 'rgba(255, 145, 124, 0.9)',
                offset: 0,
              },
            ]),
          },
          label: {
            show: true,
            formatter: (params) => {
              return params.healthScore
            },
          },
        },
      ],
    },
  ],
})

const getParameterDataInLatLng = (healthData) => {
  const indicator = []
  const value = []
  healthData.forEach((param) => {
    indicator.push({ text: pt(param.parameterName), max: 100 })
    value.push(param.healthScore)
  })
  options.value.radar[0].indicator = indicator
  options.value.series[0].data[0].value = value
}
const onCancel = () => {
  show.value = false
  emit('close', false)
}
</script>
<style scoped>
.dialog-container {
  width: 800px;
  max-width: 800px;
}

#echart {
  height: 500px;
}

.justify-between {
  float: right;
}
</style>
