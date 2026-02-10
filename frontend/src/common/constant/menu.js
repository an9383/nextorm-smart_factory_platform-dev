import { findHierarchyInTree, findInTree } from 'src/common/utils'
import { t } from 'src/plugins/i18n'

const metaEnv = import.meta.env
const devMode = metaEnv?.DEV || false
if (devMode) {
  console.log('import: ', import.meta.env)
}

const envs = import.meta.env
const VISIBLE_CATEGORY_INFO = Object.keys(envs)
  .filter((key) => key.startsWith('VITE_visible.category'))
  .reduce((acc, key) => {
    const category = key.split('.')[2]
    acc[category] = envs[key]
    return acc
  }, {})

if (devMode) {
  console.log('categories', VISIBLE_CATEGORY_INFO)
}

const base_menus = [
  {
    id: 'dashboard',
    title: t('대시보드'),
    icon: 'dashboard',
    to: '/dashboard',
  },
  {
    id: 'waterDigitalTwin',
    category: 'ecoTwin',
    title: t('수자원 디지털 트윈'),
    icon: 'water_drop',
    children: [
      {
        id: 'gpsEditor',
        title: t('로봇 가동 구역 설정'),
        icon: 'travel_explore',
        to: '/drone-active-area',
      },
      // {
      //   id: 'parameterHeatmap',
      //   title: t('Parameter Heatmap'),
      //   icon: 'gradient',
      //   to: '/parameter-heatmap',
      // },
      {
        id: 'parameterMap',
        title: t('파라미터 맵'),
        icon: 'pin_drop',
        to: '/parameter-map',
      },
      {
        id: 'robotMonitoring',
        title: t('로봇 모니터링'),
        icon: 'dvr',
        to: '/robot-monitoring',
      },
      {
        id: 'robotShootPhoto',
        title: t('로봇 촬영 사진'),
        icon: 'dvr',
        to: '/robot-shoot-photo',
      },
      {
        id: 'reservoirCapacity',
        title: t('저수량'),
        icon: 'water',
        to: '/reservoir-capacity',
      },
      {
        id: 'reservoirCapacityTrend',
        title: t('저수량 트렌드'),
        icon: 'waves',
        to: '/reservoir-capacity-trend',
      },
      {
        id: 'monthlyHealthPoint',
        title: t('위치별 월간 건강도'),
        icon: 'pin_drop',
        to: '/monthly-health-point',
      },
      {
        id: 'monthlyHealthForecast',
        title: t('월별 건강도 예측'),
        icon: 'insert_chart',
        to: '/monthly-health-forecast',
      },
      {
        id: 'infraByDroneLocation',
        title: t('로봇 지역 주변 인프라'),
        icon: 'report',
        to: '/infra-drone-location',
      },
    ],
  },
  {
    id: 'dataAnalysis',
    title: t('데이터 분석'),
    icon: 'analytics',
    children: [
      {
        id: 'parameterTrend',
        title: t('파라미터 트렌드'),
        icon: 'show_chart',
        to: '/parameter-trend',
      },
      {
        id: 'multiParameterTrend',
        title: t('복합 파라미터 트렌드'),
        icon: 'stacked_line_chart',
        to: '/multi-parameter-trend',
      },
      {
        id: 'parameterStatisticsTrend',
        title: t('파라미터 통계 트렌드'),
        icon: 'query_stats',
        to: '/parameter-statistics-trend',
      },
      {
        id: 'parameterReport',
        title: t('파라미터 리포트'),
        icon: 'event_note',
        to: '/parameter-report',
      },
      {
        id: 'faultDataView',
        title: t('이상 데이터 확인'),
        icon: 'report',
        to: '/fault-data-view',
      },
      {
        id: 'monthlyDataReport',
        title: t('월별 데이터 리포트'),
        icon: 'insert_chart',
        to: '/monthly-data-report',
      },
      {
        id: 'parameterData',
        title: t('파라미터 데이터'),
        to: '/parameter-data',
      },
      {
        id: 'recipeTrend',
        title: t('레시피 파라미터 트렌드'),
        icon: 'show_chart',
        to: '/recipe-trend',
      },
      {
        id: 'toolCollectStatus',
        title: t('설비 수집 현황'),
        icon: 'router',
        to: '/tool-collect-status',
      },
      {
        id: 'toolStatus',
        title: t('설비 현황'),
        icon: 'router',
        to: '/tool-status',
      },
    ],
  },
  {
    id: 'modeling',
    title: t('모델링'),
    caption: t('모델링'),
    icon: 'design_services',
    to: '',
    children: [
      {
        id: 'location',
        title: t('위치'),
        caption: t('위치'),
        icon: 'factory',
        to: '/location',
      },
      {
        id: 'tool',
        title: t('설비'),
        caption: t('설비'),
        icon: 'construction',
        to: '/tool',
      },
      {
        id: 'parameter',
        title: t('파라미터'),
        caption: t('파라미터'),
        icon: 'sensors',
        to: '/parameter',
      },
      {
        id: 'virtualParameter',
        title: t('가상 파라미터'),
        caption: t('가상 파라미터'),
        icon: 'data_object',
        to: '/virtual-parameter',
      },
      {
        id: 'dcpConfig',
        title: t('DCP 설정'),
        caption: t('DCP 설정'),
        icon: 'cable',
        to: '/dcp-config',
      },
      {
        id: 'collectorConfig',
        title: t('Collector 설정'),
        caption: t('Collector 설정'),
        icon: 'library_books',
        to: '/collector-config',
      },
      {
        id: 'processConfig',
        title: t('Process 설정'),
        caption: t('Process 설정'),
        icon: 'account_tree',
        to: '/process-config',
      },
      {
        id: 'summaryConfig',
        title: t('Summary 설정'),
        caption: t('Summary 설정'),
        icon: 'summarize',
        to: '/summary-config',
      },
      {
        id: 'rule',
        title: t('규칙'),
        caption: t('규칙'),
        icon: 'rule',
        to: '/rule',
      },
      {
        id: 'dataImport',
        title: t('데이터 가져오기'),
        caption: t('데이터 가져오기'),
        icon: 'upload_file',
        to: '/data-import',
      },
      {
        id: 'dataMigration',
        title: t('데이터 마이그레이션'),
        caption: t('데이터 마이그레이션'),
        icon: 'upload_file',
        to: '/data-migration',
      },
    ],
  },
  {
    id: 'apc',
    category: 'apc',
    title: t('APC'),
    icon: 'account_tree',
    children: [
      {
        id: 'apcModel',
        title: t('APC 모델'),
        to: '/apc/model',
      },
      {
        id: 'apcSimulationList',
        title: t('APC 시뮬레이션 이력'),
        to: '/apc/simulation-list',
      },
      {
        id: 'apcResult',
        title: t('APC 결과'),
        to: '/apc/result',
      },
      {
        id: 'apcResultParameterTrend',
        title: t('APC 결과 파라미터 트렌드'),
        to: '/apc/result/parameter-trend',
      },
    ],
  },
  {
    id: 'ai',
    category: 'ai',
    title: t('AI'),
    icon: 'account_tree',
    children: [
      {
        id: 'aiModelList',
        title: t('모델 목록'),
        to: '/ai/model-list',
      },
      {
        id: 'aiModeling',
        title: t('AI 모델링'),
        to: '/ai/modeling',
      },
      {
        id: 'processOptimizationAnalysis',
        title: t('공정 최적화'),
        to: '/ai/process-optimization-analysis',
      },
      {
        id: 'processOptimizationList',
        title: t('공정 최적화 결과'),
        to: '/ai/process-optimization',
      },
    ],
  },
  {
    id: 'ocap',
    category: 'ocap',
    title: t('OCAP'),
    icon: 'account_tree',
    children: [
      {
        id: 'ocapSettings',
        title: t('OCAP 설정'),
        to: '/ocap/settings',
      },
      {
        id: 'ocapAlarmHistory',
        title: t('OCAP 알림 내역'),
        to: '/ocap/alarm-history',
      },
    ],
  },
  {
    id: 'system',
    title: t('시스템 관리'),
    icon: 'settings',
    children: [
      {
        id: 'user',
        title: t('사용자'),
        icon: 'mdi-account-multiple',
        to: '/system/user',
      },
      { id: 'role', title: t('권한'), icon: 'lock', to: '/system/role' },
      {
        id: 'code',
        title: t('코드'),
        icon: 'data_object',
        to: '/system/code',
      },
      {
        id: 'i18n',
        title: t('다국어 관리'),
        icon: 'data_object',
        to: '/system/managed-language',
      },
      {
        id: 'systemInformation',
        title: t('시스템 정보'),
        icon: 'mdi-domain',
        to: '/system/information',
      },
    ],
  },
]

const menus = base_menus.filter((menu) => {
  const category = menu.category
  if (category) {
    const info = VISIBLE_CATEGORY_INFO[category] || 'false'
    return info.toLowerCase() === 'true'
  }
  return true
})
export default menus

export function findByPath(menuPath) {
  return findInTree(menus, 'to', menuPath)
}

export function findHierarchyById(menuId) {
  return findHierarchyInTree(menus, 'id', menuId)
}
