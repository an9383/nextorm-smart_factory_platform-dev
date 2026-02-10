<template>
  <q-page class="q-pa-sm">
    <q-stepper v-model="step" ref="stepper" color="primary" animated>
      <q-step
        :name="1"
        :title="$t('시뮬레이션 데이터 선택')"
        icon="settings"
        done-icon="check"
        done-color="primary"
        color="secondary"
        :done="step > 1"
      >
        <APCSimulationDataSelect
          :apc-model-version-id="apcModelVersionId"
          @simulation-started="moveToSimulationResult"
        />
      </q-step>

      <q-step
        :name="2"
        :title="$t('시뮬레이션 결과')"
        icon="create_new_folder"
        :done="step > 2"
        done-color="primary"
        color="secondary"
      >
        <APCSimulationResult :apc-simulation-id="apcSimulationId" />
        <q-stepper-navigation align="right">
          <!--  <q-btn @click="$refs.stepper.previous()" class="sBtn secondary">
            <q-icon name="arrow_back" size="xs" class="q-mr-xs"></q-icon>
            {{ $t('이전') }}
          </q-btn> -->
          <q-btn @click="moveToSimulationList" class="sBtn primary q-ml-sm">
            <q-icon name="list" size="xs" class="q-mr-xs"></q-icon>
            {{ $t('시뮬레이션 이력') }}
          </q-btn>
        </q-stepper-navigation>
      </q-step>
    </q-stepper>
  </q-page>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import APCSimulationDataSelect from './APCSimulationDataSelect.vue'
import APCSimulationResult from './APCSimulationResult.vue'

const { apcModelVersionId } = defineProps({
  apcModelVersionId: {
    type: Number,
    required: false,
    default: null,
  },
})

const route = useRoute()
const router = useRouter()

const stepper = ref()
const step = ref(route.params.id ? 2 : 1)
const apcSimulationId = ref(route.params.id)

const moveToSimulationResult = (simulationId) => {
  apcSimulationId.value = simulationId
  stepper.value.next()
}

const moveToSimulationList = () => {
  router.replace('/apc/simulation-list')
}
</script>
