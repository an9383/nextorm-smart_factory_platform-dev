<template>
  <q-dialog v-model="show" persistent>
    <q-card class="dialog-container">
      <q-card-section class="bg-secondary text-white">
        <div class="text-h6">파라미터 추가 데이터 맵핑</div>
      </q-card-section>
      <q-separator />
      <div class="row q-pa-md scroll" style="height: 70vh">
        <q-form ref="form" style="width: 100%">
          <q-table
            bordered
            :columns="columns"
            :rows="rows"
            row-key="id"
            color="primary"
            virtual-scroll
            :virtual-scroll-item-size="100"
            :rows-per-page-options="[0]"
          >
            <template v-slot:body-cell="props">
              <q-td :props="props">
                <div v-if="props.col.name === 'name'">{{ props.value }}</div>
                <q-input
                  v-else-if="props.col.dataType === 'NUMBER'"
                  v-model.number="extraData[props.row.id][props.col.field]"
                  input-class="text-left"
                  type="number"
                  :rules="props.col.required && [$rules.required]"
                  dense
                />
                <q-input
                  v-else
                  v-model="extraData[props.row.id][props.col.field]"
                  input-class="text-left"
                  :rules="props.col.required && [$rules.required]"
                  dense
                />
              </q-td>
            </template>
          </q-table>
        </q-form>
      </div>
      <q-separator />
      <q-card-actions class="justify-between">
        <span style="color: red">가상 파라미터, 메타 데이터 파라미터는 맵핑 대상이 아닙니다.</span>
        <div>
          <q-btn flat color="negative" :label="$t('닫기')" @click="onCancel" />
          <q-btn flat color="primary" :label="$t('저장')" @click="onOk" />
        </div>
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup>
import { t } from 'src/plugins/i18n'
import { ref } from 'vue'

const show = true

const props = defineProps({
  parameters: {
    type: Array,
    required: true,
  },
  extraDataDefines: {
    type: Array,
    required: true,
  },
})

const extraData = defineModel({ type: Object, default: {} })

props.parameters.forEach((parameter) => {
  if (!extraData.value[parameter.id]) {
    extraData.value[parameter.id] = {}
  }
})

const emit = defineEmits(['close'])

// 수정하다가 취소할 경우, 초기화를 위해 복사
const initCaptureExtraData = JSON.parse(JSON.stringify(extraData.value))

const columns = [
  { name: 'name', align: 'left', label: t('파라미터명'), field: 'name' },
  ...props.extraDataDefines.map((extraDataDefine) => ({
    name: extraDataDefine.key,
    align: 'left',
    label: extraDataDefine.key,
    field: extraDataDefine.key,
    dataType: extraDataDefine.dataType,
    headerClasses: extraDataDefine.required && 'input-required',
    required: extraDataDefine.required,
  })),
]
const rows = [...props.parameters]

const form = ref(null)

const onCancel = () => {
  extraData.value = initCaptureExtraData
  emit('close')
}

const onOk = async () => {
  const isValid = await form.value.validate()
  if (!isValid) {
    form.value.focus()
    return false
  }
  emit('close')
}
</script>

<style scoped>
.dialog-container {
  width: 800px;
  max-width: 1300px;
}

:deep(.q-field__messages) {
  margin-top: 5px;
}

:deep(.input-required::after) {
  content: '*';
  margin-left: 3px;
  color: red;
}
</style>
