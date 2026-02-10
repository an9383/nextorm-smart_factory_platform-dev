import { t, pt } from 'src/plugins/i18n'

export const parameterTableColumnsDefinition = [
  {
    name: 'name',
    label: t('파라미터명'),
    field: 'name',
    align: 'center',
    sortable: true,
    format: (val) => pt(val),
  },
  {
    name: 'type',
    label: t('타입'),
    field: 'type',
    align: 'center',
  },
  {
    name: 'dataType',
    label: t('데이터 타입'),
    field: 'dataType',
    align: 'center',
  },
  {
    name: 'isVirtual',
    label: t('가상 파라미터 여부'),
    field: 'isVirtual',
    align: 'center',
  },
]
