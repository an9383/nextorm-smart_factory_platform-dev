const CONTEXT_MENU_ACTION_TYPE = {
  CREATE: 'CREATE',
  DELETE: 'DELETE',
}

const LOCATION_TYPE = {
  SITE: 'SITE',
  FAB: 'FAB',
  LINE: 'LINE',
}

const getLocationTypes = (t) => [
  { value: LOCATION_TYPE.SITE, text: t('사이트'), disable: true },
  { value: LOCATION_TYPE.FAB, text: t('공장'), disable: true },
  { value: LOCATION_TYPE.LINE, text: t('라인') },
]

export { CONTEXT_MENU_ACTION_TYPE, LOCATION_TYPE, getLocationTypes }
