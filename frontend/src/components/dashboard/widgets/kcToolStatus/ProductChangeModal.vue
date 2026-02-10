<template>
  <q-dialog v-model="showModal" persistent>
    <q-card style="min-width: 400px">
      <q-card-section>
        <div class="text-h6">제품 변경</div>
      </q-card-section>

      <q-card-section class="q-pt-none q-pb-none">
        <filterable-select
          v-model="product"
          outlined
          autofocus
          :options="moldProducts"
          option-label="name"
          emit-value
          map-options
          :rules="[$rules.required]"
        />
      </q-card-section>

      <q-card-section v-if="product" class="q-pt-md">
        <div class="row q-col-gutter-md q-pa-sm bg-grey-1 rounded-borders">
          <div class="col-4 text-center">
            <div class="text-caption text-grey-7 q-mb-xs">캐비티</div>
            <div class="text-h6 text-weight-medium text-primary">{{ product.cavity ?? '-' }}</div>
          </div>
          <div class="col-4 text-center">
            <div class="text-caption text-grey-7 q-mb-xs">평균생산수량</div>
            <div class="text-h6 text-weight-medium text-primary">{{ product.averageProductionCount ?? '-' }}</div>
          </div>
          <div class="col-4 text-center">
            <div class="text-caption text-grey-7 q-mb-xs">타수</div>
            <div class="text-h6 text-weight-medium text-primary">{{ product.cycleCount ?? '-' }}</div>
          </div>
        </div>
      </q-card-section>

      <q-card-actions align="right" class="text-primary">
        <q-btn flat label="취소" @click="closeModal" />
        <q-btn :disable="!product" flat label="저장" @click="saveProductName" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import FilterableSelect from 'components/common/FilterableSelect.vue'
import EqmsResourceService from 'src/services/EqmsResourceService'

const props = defineProps({
  productId: {
    type: Number,
    default: null,
  },
})

const emit = defineEmits(['save', 'close'])
const showModal = ref(true)

const moldProducts = ref([])
const product = ref(null)

const closeModal = () => {
  emit('close')
}

const saveProductName = () => {
  emit('save', product.value)
}

const getMoldProducts = async () => {
  moldProducts.value = await EqmsResourceService.getMoldProducts()
  if (props.productId) {
    product.value = moldProducts.value.find((p) => p.id === props.productId) || null
  }
}

onMounted(async () => await getMoldProducts())
</script>
