<template>
  <div class="column container">
    <div class="col-6" style="margin-bottom: 20px">
      <div class="row justify-center full-height">
        <q-card class="col-4 full-height map-card">
          <q-card-section class="q-py-sm bg-primary">
            <div class="text-subtitle1 text-bold text-white">
              <q-icon name="radio_button_checked" size="sm" class="q-mr-sm" color="#fff"></q-icon
              >{{ selectedTool?.name }}
            </div>
          </q-card-section>
          <tool-map v-if="tools.length !== 0" :tools="tools" @select-tool="handleSelectTool" />
        </q-card>
        <q-card class="col full-height column" style="margin-left: 20px">
          <tool-status v-if="isShowWidget" :key="`ts_${toolId}`" :tool-id="toolId" />
        </q-card>
      </div>
    </div>
    <div class="col">
      <div class="row full-height">
        <q-card class="col-4 full-height column">
          <real-time-battery-charge v-if="isShowWidget" :key="`rtbc_${toolId}`" :parameter-id="realTimeParameterId" />
        </q-card>
        <q-card class="col full-height column" style="margin-left: 20px">
          <battery-voltage-and-charge-trend
            v-if="isShowWidget"
            :key="`bvact_${toolId}`"
            :parameter-ids="trendParameterIds"
          />
        </q-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import ToolStatus from 'pages/waterQuality/robotMonitoring/ToolStatus.vue'
import RealTimeBatteryCharge from 'pages/waterQuality/robotMonitoring/RealTimeBatteryCharge.vue'
import BatteryVoltageAndChargeTrend from 'pages/waterQuality/robotMonitoring/BatteryVoltageAndChargeTrend.vue'
import { computed, onBeforeMount, ref } from 'vue'
import ToolMap from 'pages/waterQuality/robotMonitoring/ToolMap.vue'
import ToolService from 'src/services/modeling/ToolService'
import ParameterService from 'src/services/modeling/ParameterService'

const REAL_TIME_BATTERY_CHARGE_WIDGET_USING_TAG_NAME = 'ctr_bat_soc'
const BATTERY_VOLTAGE_AND_CHARGE_TREND_WIDGET_USING_TAG_NAMES = ['ctr_bat_soc', 'ctr_bat_v']

const IS_SHOW = true

const tools = ref([])
const selectedTool = ref(null)
const selectedToolParameters = ref([])

const isShowWidget = computed(() => {
  return IS_SHOW && selectedTool.value
})

const toolId = computed(() => {
  if (!selectedTool.value) {
    return null
  }
  return selectedTool.value.id
})

const realTimeParameterId = computed(() => {
  if (!selectedTool.value) {
    return null
  }

  const parameter = selectedToolParameters.value.find(
    (parameter) => parameter.name === REAL_TIME_BATTERY_CHARGE_WIDGET_USING_TAG_NAME,
  )
  if (!parameter) {
    return null
  }
  return parameter.id
})

const trendParameterIds = computed(() => {
  if (!selectedTool.value) {
    return []
  }

  const filteredParameters = selectedToolParameters.value.filter((parameter) =>
    BATTERY_VOLTAGE_AND_CHARGE_TREND_WIDGET_USING_TAG_NAMES.includes(parameter.name),
  )
  return filteredParameters.map((parameter) => parameter.id)
})

const changeToolAndParameters = async (newTool) => {
  const { id: toolId } = newTool
  selectedToolParameters.value = await ParameterService.getParameters({ toolId })
  selectedTool.value = newTool
}

const handleSelectTool = async (tool) => {
  await changeToolAndParameters(tool)
}

onBeforeMount(async () => {
  tools.value = await ToolService.getTools()
})
</script>

<style lang="scss" scoped>
.container {
  height: calc(100vh - 50px);
  padding: 10px;
}
.map-card {
  display: flex;
  flex-direction: column;
  .q-card__section {
    height: 60px;
    padding: 20px;
    display: flex;
    align-items: center;
    i {
      animation: locationPoint 1.5s infinite ease-in-out;
    }
  }
}
@keyframes locationPoint {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(0.9);
  }
  100% {
    transform: scale(1);
  }
}
</style>
