import { findTreeItem } from 'src/common/treeUtil'

export const publicRoutes = [
  {
    path: '/login',
    component: () => import('/src/pages/login/Login.vue'),
  },
  {
    path: '/login/ai-chat',
    component: () => import('/src/pages/login/Login.vue'),
  },
  {
    path: '/403',
    component: () => import('/src/pages/error/Error403.vue'),
  },
]

export const protectedRoute = [
  {
    path: '/ai-chat',
    component: () => import('layouts/FullScreenLayout.vue'),
    children: [
      {
        path: '',
        component: () => import('/src/pages/stormy/StormyPage.vue'),
      },
    ],
  },
  {
    path: '/fs',
    component: () => import('layouts/FullScreenLayout.vue'),
    children: [
      {
        path: 'infra-by-location-map',
        component: () => import('/src/pages/dataAnalytics/infraByDroneLocation/InfraByLocationMap.vue'),
        props: (route) => ({ latitude: route.query.latitude, longitude: route.query.longitude }),
      },
      {
        path: 'monthly-data-chart',
        component: () => import('/src/pages/dataAnalytics/monthlyDataReport/MonthlyDataChart.vue'),
        props: (route) => ({
          parameterId: route.query.parameterId,
          fromYear: route.query.fromYear,
          toYear: route.query.toYear,
        }),
      },
      {
        path: 'parameter-map-chart',
        component: () => import('/src/pages/waterQuality/parameterMap/ParameterMapChart.vue'),
        props: (route) => ({
          toolId: route.query.toolId,
          parameterId: route.query.parameterId,
          fromDateTime: route.query.fromDateTime,
          toDateTime: route.query.toDateTime,
        }),
      },
      {
        path: 'parameter-trend-chart',
        component: () => import('/src/pages/dataAnalytics/parameterTrend/ParameterTrendChart.vue'),
        props: (route) => ({
          parameterId: route.query.parameterId,
          fromDateTime: route.query.fromDateTime,
          toDateTime: route.query.toDateTime,
        }),
      },
      {
        path: 'image-data-view',
        component: () => import('/src/components/dataview/ImageDataAt.vue'),
        props: (route) => ({
          toolId: route.query.toolId,
          datetime: route.query.datetime,
        }),
      },
    ],
  },
  {
    path: '/',
    component: () => import('layouts/MainLayout.vue'),
    children: [
      {
        path: '/dashboard/:id?',
        component: () => import('/src/pages/dashboard/DashboardPage.vue'),
      },
      {
        path: '/dashboard/slides',
        component: () => import('/src/pages/dashboard/Slides.vue'),
      },
      {
        path: '/drone-active-area',
        component: () => import('/src/pages/waterQuality/gpsEditor/GPSEditor.vue'),
      },
      {
        path: '/parameter-heatmap',
        component: () => import('/src/pages/waterQuality/parameterHeatmap/ParameterHeatmap.vue'),
      },
      {
        path: '/parameter-map',
        component: () => import('/src/pages/waterQuality/parameterMap/ParameterMapPage.vue'),
      },
      {
        path: '/reservoir-capacity',
        component: () => import('/src/pages/waterQuality/reservoirCapacity/ReservoirCapacityList.vue'),
      },
      {
        path: '/reservoir-capacity-trend',
        component: () => import('/src/pages/waterQuality/reservoirCapacity/ReservoirCapacityTrend.vue'),
      },
      {
        path: '/monthly-health-point',
        component: () => import('/src/pages/waterQuality/monthlyHealthPoint/MonthlyHealthPointByLocation.vue'),
      },
      {
        path: '/monthly-data-report',
        component: () => import('/src/pages/dataAnalytics/monthlyDataReport/MonthlyDataReportPage.vue'),
      },
      {
        path: '/infra-drone-location',
        component: () => import('/src/pages/dataAnalytics/infraByDroneLocation/InfraByDroneLocationPage.vue'),
      },
      {
        path: '/monthly-health-forecast',
        component: () => import('/src/pages/dataAnalytics/monthlyHealthForecast/MonthlyHealthForecast.vue'),
      },
      {
        path: '/robot-monitoring',
        component: () => import('/src/pages/waterQuality/robotMonitoring/RobotMonitoringPage.vue'),
      },
      {
        path: '/robot-shoot-photo',
        component: () => import('/src/pages/waterQuality/robotShootPhoto/RobotShootPhoto.vue'),
      },
      {
        path: '/parameter-trend',
        component: () => import('/src/pages/dataAnalytics/parameterTrend/ParameterTrendPage.vue'),
      },
      {
        path: '/recipe-trend',
        component: () => import('/src/pages/dataAnalytics/RecipeTrend/RecipeTrendPage.vue'),
      },
      {
        path: '/multi-parameter-trend',
        component: () => import('/src/pages/dataAnalytics/multiParameterTrend/MultiParameterTrend.vue'),
      },
      {
        path: '/parameter-statistics-trend',
        component: () => import('/src/pages/dataAnalytics/parameterStatisticsTrend/ParameterStatisticsTrend.vue'),
      },
      {
        path: '/fault-data-view',
        component: () => import('/src/pages/dataAnalytics/faultDataView/FaultDataViewPage.vue'),
      },
      {
        path: '/parameter-report',
        component: () => import('/src/pages/waterQuality/parameterReport/ParameterReportPage.vue'),
      },
      {
        path: '/parameter-data',
        component: () => import('/src/pages/dataAnalytics/parameterData/ParameterDataPage.vue'),
      },
      {
        path: '/location',
        component: () => import('/src/pages/modeling/location/LocationPage.vue'),
      },
      {
        path: '/tool',
        component: () => import('/src/pages/modeling/tool/ToolList.vue'),
      },
      {
        path: '/parameter',
        component: () => import('/src/pages/modeling/parameter/ParameterList.vue'),
      },
      {
        path: '/virtual-parameter',
        component: () => import('/src/pages/modeling/virtualParameter/VirtualParameterPage.vue'),
      },
      {
        path: '/dcp-config',
        component: () => import('/src/pages/modeling/dcpConfig/DcpConfigList.vue'),
      },
      {
        path: '/collector-config',
        component: () => import('/src/pages/modeling/collectorConfig/CollectorConfigList.vue'),
      },
      {
        path: '/process-config',
        component: () => import('/src/pages/modeling/processConfig/ProcessConfigList.vue'),
      },
      {
        path: '/summary-config',
        component: () => import('/src/pages/modeling/summaryConfig/SummaryConfigList.vue'),
      },
      {
        path: '/rule',
        component: () => import('/src/pages/modeling/rule/RuleList.vue'),
      },
      {
        path: '/data-import',
        component: () => import('/src/pages/modeling/dataImport/DataImport.vue'),
      },
      {
        path: '/data-migration',
        component: () => import('/src/pages/modeling/dataMigration/DataMigration.vue'),
      },
      {
        path: '/system/user',
        component: () => import('/src/pages/system/User.vue'),
      },
      {
        path: '/system/role',
        component: () => import('/src/pages/system/Role.vue'),
      },
      {
        path: '/system/code',
        component: () => import('/src/pages/system/Code.vue'),
      },
      {
        path: '/system/managed-language',
        component: () => import('/src/pages/system/i18n/ManagedLanguagePage.vue'),
      },
      {
        path: '/system/information',
        component: () => import('/src/pages/system/SystemInformation.vue'),
      },
      {
        path: '/tool-collect-status',
        component: () => import('/src/pages/dataAnalytics/toolCollectStatus/ToolCollectStatusPage.vue'),
      },
      {
        path: '/tool-status',
        component: () => import('/src/pages/dataAnalytics/toolStatus/ToolStatusPage.vue'),
      },
      // Not completed yet
      // {path: '/Taskboard', component: () => import('/src/demo/pages/TaskBoard.vue')},
    ],
  },
  {
    path: '/apc',
    component: () => import('layouts/MainLayout.vue'),
    children: [
      {
        path: '/apc/simulation/:id?',
        component: () => import('/src/pages/apc/simulation/APCSimulationPage.vue'),
        props: (route) => ({ apcModelVersionId: route.query.apcModelVersionId }),
      },
      {
        path: '/apc/simulation-list',
        component: () => import('/src/pages/apc/simulation/APCSimulationList.vue'),
      },
      {
        path: '/apc/model',
        component: () => import('/src/pages/apc/model/ApcModel.vue'),
      },
      {
        path: '/apc/result',
        component: () => import('/src/pages/apc/result/ApcResultPage.vue'),
      },
      {
        path: '/apc/result/parameter-trend',
        component: () => import('/src/pages/apc/result/ApcResultParameterTrendPage.vue'),
      },
    ],
  },

  {
    path: '/ai',
    component: () => import('layouts/MainLayout.vue'),
    children: [
      {
        path: '/ai/model-list',
        component: () => import('/src/pages/ai/modelList/AiModelListPage.vue'),
      },
      {
        path: '/ai/modeling',
        component: () => import('/src/pages/ai/modeling/AiModelingPage.vue'),
        props: (route) => ({ modelId: route.query.id }),
      },
      {
        path: '/ai/process-optimization-analysis',
        name: 'processOptimizationAnalysis', // 여기에 이름 지정
        component: () => import('/src/pages/ai/processOptimizationAnalysis.vue'),
        props: (route) => ({ id: route.query.id && parseInt(route.query.id, 10) }),
      },
      {
        path: '/ai/process-optimization',
        component: () => import('/src/pages/ai/processOptimizationList.vue'),
      },
    ],
  },
  {
    path: '/ocap',
    component: () => import('layouts/MainLayout.vue'),
    children: [
      {
        path: '/ocap/settings',
        component: () => import('/src/pages/ocap/settings/OcapSettingPage.vue'),
      },
      {
        path: '/ocap/alarm-history',
        component: () => import('/src/pages/ocap/alarmHistory/OcapAlarmHistoryPage.vue'),
      },
    ],
  },
]

export const isPublicRoute = (path) => {
  return findTreeItem(publicRoutes, path, { id: 'path' }) !== undefined
}

export const isProtectedRoute = (path) => {
  return findTreeItem(protectedRoute, path, { id: 'path' }) !== undefined
}

export default [...publicRoutes, ...protectedRoute]
