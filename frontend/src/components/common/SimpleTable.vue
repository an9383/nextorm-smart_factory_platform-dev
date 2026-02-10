<template>
  <q-table v-bind="filteredAttrs.restAttrs" v-model:pagination="pagination" :rows-per-page-label="' '" ref="tableRef">
    <template v-slot:header="props">
      <q-tr :props="props">
        <q-th v-for="col in props.cols" :key="col.name" :props="props">
          <div v-if="col.filterable" class="flex items-center inline-block">
            {{ col.label }}
            <q-icon
              name="filter_list"
              size="xs"
              class="cursor-pointer q-pl-xs"
              color="secondary"
              @click.stop="onOpenPopup(col.name)"
            >
              <q-badge
                v-if="columnFilters[col.name]?.filteredValue"
                color="green"
                rounded
                floating
                transparent
                class="q-py-none"
              />
              <q-popup-proxy transition-show="scale" transition-hide="scale" @hide="closePopup(col.name)">
                <div class="q-px-none filter-popup">
                  <div class="text-bold q-pl-sm q-py-sm bg-secondary text-white">
                    <q-icon name="filter_list" color="white" size="sm"></q-icon>
                    {{ $t('필터') }}
                  </div>
                  <q-item class="q-pa-none q-mt-lg">
                    <q-item-section>
                      <q-input
                        dense
                        v-model="columnFilters[col.name].inputValue"
                        :label="$t(col.label)"
                        clearable
                        class="input-field q-mt-sm"
                      />
                    </q-item-section>
                  </q-item>
                  <q-item class="row q-mt-sm">
                    <q-btn v-close-popup class="negative-btn q-mr-xs col" flat :label="$t('취소')" />
                    <q-btn
                      v-close-popup
                      class="positive-btn col"
                      flat
                      :label="$t('확인')"
                      @click="onFilteringColumn(col.name)"
                    />
                  </q-item>
                </div>
              </q-popup-proxy>
            </q-icon>
          </div>
          <span v-else>{{ col.label }}</span>
        </q-th>
      </q-tr>
    </template>
    <template v-for="(slotFn, slotName) in $slots" v-slot:[slotName]="slotProps">
      <slot :name="slotName" v-bind="slotProps" />
    </template>
    <template v-slot:pagination="{ pagesNumber, isFirstPage, firstPage, prevPage, nextPage, isLastPage, lastPage }">
      <q-btn
        v-if="pagesNumber > 2"
        icon="first_page"
        color="grey-8"
        round
        dense
        flat
        :disable="isFirstPage"
        @click="firstPage"
      ></q-btn>

      <q-btn icon="chevron_left" color="grey-8" round dense flat :disable="isFirstPage" @click="prevPage"></q-btn>

      <div class="row">
        <q-pagination
          v-model="pagination.page"
          color="teal-8"
          :max="pagesNumber"
          size="md"
          :max-pages="5"
          :boundary-numbers="false"
          :ellipses="false"
          @update:model-value="onUpdatePagination"
        />
      </div>

      <q-btn icon="chevron_right" color="grey-8" round dense flat :disable="isLastPage" @click="nextPage"></q-btn>

      <q-btn
        v-if="pagesNumber > 2"
        icon="last_page"
        color="grey-8"
        round
        dense
        flat
        :disable="isLastPage"
        @click="lastPage"
      ></q-btn>
    </template>

    <template v-slot:no-data="">
      <div class="full-width row flex-center text-grey-6 q-py-md">
        <q-icon size="2em" name="info" />
        <span class="q-ml-sm"> {{ $t('조회된 데이터가 없습니다.') }} </span>
      </div>
    </template>
  </q-table>
</template>

<script setup>
import { computed, defineOptions, ref, useAttrs } from 'vue'

defineOptions({
  inheritAttrs: false,
})

const attrs = useAttrs()
const columnFilters = ref({})
const filteredRows = ref(attrs.rows)
const tableRef = ref()

const filteredAttrs = computed(() => {
  // eslint-disable-next-line no-unused-vars
  const { pagination, ...restAttrs } = attrs //pagination 속성은 제거
  return { restAttrs, rows: filteredRows.value }
})
const pagination = ref({
  page: 1,
  rowsPerPage: 10,
  ...(attrs.pagination || {}),
})
const onUpdatePagination = () => {
  tableRef.value.requestServerInteraction()
}
const onOpenPopup = (name) => {
  columnFilters.value[name] = {
    filteredValue: columnFilters.value[name]?.inputValue,
    inputValue: columnFilters.value[name]?.filteredValue,
    isConfirm: false,
  }
}
const onFilteringColumn = (name) => {
  columnFilters.value[name].isConfirm = true
  columnFilters.value[name].filteredValue = columnFilters.value[name].inputValue
  filteringAction()
}

const closePopup = (name) => {
  if (!columnFilters.value[name].isConfirm) {
    columnFilters.value[name].inputValue = columnFilters.value[name].filteredValue
    columnFilters.value[name].isConfirm = false
  }
}

const filteringAction = () => {
  filteredRows.value = attrs.rows.filter((row) =>
    Object.keys(columnFilters.value).every((key) =>
      row[key].includes(columnFilters.value[key].inputValue === null ? '' : columnFilters.value[key].filteredValue),
    ),
  )
  filteredAttrs.value.restAttrs.rows = columnFilters.value.length !== 0 ? filteredRows.value : attrs.rows
}

defineExpose({
  clearColumnFilter: () => {
    columnFilters.value = {}
  },
  $tableRef: tableRef,
})
</script>

<style lang="scss" scoped>
.positive-btn {
  background-color: var(--mainBgColor) !important;
  border: 1px solid var(--mainColor);
  color: var(--mainColor) !important;
  min-height: 30px !important;
}

.negative-btn {
  background-color: rgba(193, 0, 21, 0.1019607843) !important;
  border: 1px solid #c10015;
  color: #c10015 !important;
  min-height: 30px !important;
}

.filter-popup {
  height: 150px;
  width: 250px;
  opacity: 0.8;
}
</style>
