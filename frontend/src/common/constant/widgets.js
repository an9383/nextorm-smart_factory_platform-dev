import { defineAsyncComponent } from 'vue'
import { t } from '/src/plugins/i18n'

/**
 * 모든 위젯이 허용되는 환경 변수 이름 목록
 * .env 파일 내의 환경 이름이 일치해야 한다.
 */
export const WIDGET_ALL_PERMIT_ENV_NAMES_SET = new Set(['로컬', '개발서버', '데모서버'])

/**
 * 대시보드 위젯 목록
  id: 위젯 아이디 (중복 불가)
  name: 위젯 이름
  description: 위젯 설명
  capture: 위젯 캡쳐 이미지 경로
  minW: 위젯 최소 너비
  minH: 위젯 최소 높이
  permitEnvs: 허용 환경 변수 이름 배열 (falsy 하거나, 배열에 아무런 값이 없으면 모두 허용), 상단의 상수에 있는 환경은 허용됨
  component: 위젯 컴포넌트
  settingComponent: 위젯 설정 컴포넌트 (null인 경우 설정 불가)
 */
const widgets = [
  {
    id: 'parameterDataTrend',
    name: 'Parameter Data Trend',
    description: t('파라미터 데이터 트렌드를 실시간으로 확인 가능합니다.'),
    capture: '/img/widgets/parameter_data_trend_widget.jpg',
    minW: 3,
    minH: 8,
    component: defineAsyncComponent(
      () => import('/src/components/dashboard/widgets/parameterDataTrendChart/ParameterDataTrendWidget.vue'),
    ),
    settingComponent: defineAsyncComponent(
      () => import('/src/components/dashboard/widgets/parameterDataTrendChart/ParameterDataTrendWidgetSetting.vue'),
    ),
  },
  {
    id: 'parameterAreaChart',
    name: 'Parameter Area Chart',
    description: t('파라미터 데이터 통계를 Area 차트로 확인 가능합니다.'),
    capture: '/img/widgets/parameter_area_chart_widget.jpg',
    minW: 3,
    minH: 8,
    component: defineAsyncComponent(
      () => import('/src/components/dashboard/widgets/areaChart/ParameterAreaChartWidget.vue'),
    ),
    settingComponent: defineAsyncComponent(
      () => import('/src/components/dashboard/widgets/areaChart/ParameterAreaChartWidgetSetting.vue'),
    ),
  },
  {
    id: 'parameterScatterChart',
    name: 'Parameter Scatter Chart',
    description: t('파라미터 데이터 통계를 Scatter 차트로 확인 가능합니다.'),
    capture: '/img/widgets/parameter_scatter_chart_widget.jpg',
    minW: 3,
    minH: 8,
    component: defineAsyncComponent(
      () => import('/src/components/dashboard/widgets/scatterChart/ParameterScatterChartWidget.vue'),
    ),
    settingComponent: defineAsyncComponent(
      () => import('/src/components/dashboard/widgets/scatterChart/ParameterScatterChartWidgetSetting.vue'),
    ),
  },
  {
    id: 'realtimeParameterValues',
    name: 'Realtime Parameter Values',
    description: t('실시간 데이터 목록을 확인 가능합니다.'),
    capture: '/img/widgets/realtime_parameter_values_widget.jpg',
    minW: 3,
    minH: 8,
    component: defineAsyncComponent(
      () => import('/src/components/dashboard/widgets/realtimeParameterValues/RealtimeParameterValuesWidget.vue'),
    ),
    settingComponent: defineAsyncComponent(
      () =>
        import('/src/components/dashboard/widgets/realtimeParameterValues/RealtimeParameterValuesWidgetSetting.vue'),
    ),
  },
  {
    id: 'toolStatus',
    name: 'Tool Status',
    description: t('현재 장비의 위치 및 상태를 확인 가능합니다.'),
    capture: '/img/widgets/tool_status_widget.jpg',
    permitEnvs: ['에코피스'],
    minW: 3,
    minH: 11,
    component: defineAsyncComponent(() => import('/src/components/dashboard/widgets/toolStatus/ToolStatusWidget.vue')),
    settingComponent: defineAsyncComponent(
      () => import('/src/components/dashboard/widgets/toolStatus/ToolStatusWidgetSetting.vue'),
    ),
  },
  {
    id: 'collectStatus',
    name: 'Collect Status',
    description: t('현재 장비 DCP 및 파라미터 수집 상태를 확인 가능합니다.'),
    capture: '/img/widgets/collect_status_widget.jpg',
    minW: 3,
    minH: 8,
    component: defineAsyncComponent(
      () => import('/src/components/dashboard/widgets/collectStatus/CollectStatusWidget.vue'),
    ),
    settingComponent: defineAsyncComponent(
      () => import('/src/components/dashboard/widgets/collectStatus/CollectStatusWidgetSetting.vue'),
    ),
  },
  {
    id: 'waterQuality',
    name: 'WaterQuality Heatmap',
    description: t('수질 건강도 점수를 히트맵으로 확인 가능합니다.'),
    capture: '/img/widgets/waterquality_heatmap_widget.jpg',
    permitEnvs: ['에코피스'],
    minW: 5,
    minH: 10,
    component: defineAsyncComponent(
      () => import('/src/components/dashboard/widgets/waterQualityHeatMap/WaterQualityHeatMapWidget.vue'),
    ),
    settingComponent: defineAsyncComponent(
      () => import('/src/components/dashboard/widgets/waterQualityHeatMap/WaterQualityHeatMapWidgetSetting.vue'),
    ),
  },
  {
    id: 'waterQualityPieChart',
    name: 'WaterQuality PieChart',
    description: t('수질 건강도 비율을 파이차트로 확인 가능합니다.'),
    capture: '/img/widgets/waterquality_piechart_widget.jpg',
    permitEnvs: ['에코피스'],
    minW: 3,
    minH: 8,
    component: defineAsyncComponent(
      () => import('/src/components/dashboard/widgets/waterQualityPieChart/WaterQualityPieChartWidget.vue'),
    ),
    settingComponent: defineAsyncComponent(
      () => import('/src/components/dashboard/widgets/waterQualityPieChart/WaterQualityPieChartWidgetSetting.vue'),
    ),
  },
  {
    id: 'underWaterTerrain',
    name: 'UnderWater Terrain',
    description: t('수심 데이터를 이용한 수중 3D 지형을 확인 가능합니다.'),
    capture: '/img/widgets/underwater_terrain_widget.jpg',
    permitEnvs: ['에코피스'],
    minW: 3,
    minH: 8,
    component: defineAsyncComponent(
      () => import('/src/components/dashboard/widgets/underWaterTerrain/UnderWaterTerrainWidget.vue'),
    ),
    settingComponent: defineAsyncComponent(
      () => import('/src/components/dashboard/widgets/underWaterTerrain/UnderWaterTerrainWidgetSetting.vue'),
    ),
  },
  {
    id: 'robotPhoto',
    name: 'Robot Photo',
    description: t('로봇이 촬영한 최신 사진을 확인 가능합니다.'),
    capture: '/img/widgets/robot-photo-widget.jpg',
    permitEnvs: ['에코피스'],
    minW: 3,
    minH: 8,
    component: defineAsyncComponent(
      () => import('/src/components/dashboard/widgets/robotShootPhoto/RobotShootPhotoWidget.vue'),
    ),
    settingComponent: defineAsyncComponent(
      () => import('/src/components/dashboard/widgets/robotShootPhoto/RobotShootPhotoWidgetSetting.vue'),
    ),
  },
  {
    id: 'currentTraceData',
    name: 'Current Trace Data',
    description: t('설비의 현재 데이터를 실시간으로 확인 할 수 있습니다.'),
    capture: '/img/widgets/current_trace_data_widget.png',
    minW: 4,
    minH: 7,
    component: defineAsyncComponent(
      () => import('/src/components/dashboard/widgets/currentTraceData/CurrentTraceDataWidget.vue'),
    ),
    settingComponent: defineAsyncComponent(
      () => import('/src/components/dashboard/widgets/currentTraceData/CurrentTraceDataWidgetSetting.vue'),
    ),
  },
  {
    id: 'parametersLatestValueTable',
    name: 'Parameters Latest Value Table',
    description: t('파라미터 목록의 최신 값을 테이블로 확인 가능합니다.'),
    capture: '/img/widgets/parameter_latest_value_table_widget.png',
    minW: 4,
    minH: 7,
    component: defineAsyncComponent(
      () => import('src/components/dashboard/widgets/parametersLatestValueTable/ParameterLatestValueTableWidget.vue'),
    ),
    settingComponent: defineAsyncComponent(
      () =>
        import(
          'src/components/dashboard/widgets/parametersLatestValueTable/ParameterLatestValueTableWidgetSetting.vue'
        ),
    ),
  },
  {
    id: 'parametersLatestValueColumnTable',
    name: 'Parameters Latest Value Column Table',
    description: t('파라미터들의 최신 값을 컬럼형 테이블로 확인 가능합니다.'),
    capture: '/img/widgets/parameters_latest_value_column_table.png',
    minW: 1,
    minH: 2,
    component: defineAsyncComponent(
      () =>
        import(
          'src/components/dashboard/widgets/parametersLatestValueColumnTable/ParameterLatestValueColumnTableWidget.vue'
        ),
    ),
    settingComponent: defineAsyncComponent(
      () =>
        import(
          'src/components/dashboard/widgets/parametersLatestValueColumnTable/ParameterLatestValueColumnTableWidgetSetting.vue'
        ),
    ),
  },
  {
    id: 'flexibleTable',
    name: 'Flexible Table',
    description: t('파라미터 최신 데이터를 이용하여 테이블을 유연하게 구성할 수 있는 위젯입니다'),
    capture: '/img/widgets/parameter_latest_value_table_widget.png',
    minW: 1,
    minH: 2,
    component: defineAsyncComponent(
      () => import('src/components/dashboard/widgets/flexibleTableWidget/FlexibleTableWidget.vue'),
    ),
    settingComponent: defineAsyncComponent(
      () => import('src/components/dashboard/widgets/flexibleTableWidget/FlexibleTableWidgetSetting.vue'),
    ),
  },
  {
    id: 'kcToolStatus',
    name: 'KC Tool Status',
    description: t('건창스치로폴 설비의 상태를 확인할 수 있는 위젯입니다.'),
    capture: '/img/widgets/location_assistant_widget.png',
    minW: 1,
    minH: 2,
    permitEnvs: ['건창스치로폴'],
    component: defineAsyncComponent(
      () => import('src/components/dashboard/widgets/kcToolStatus/KcToolStatusWidget.vue'),
    ),
    settingComponent: defineAsyncComponent(
      () => import('src/components/dashboard/widgets/kcToolStatus/KcToolStatusWidgetSetting.vue'),
    ),
  },
  {
    id: 'kcFoamingToolStatus',
    name: 'KC Foaming Tool Status',
    description: t('건창스치로폴 발포 설비의 상태를 확인할 수 있는 위젯입니다.'),
    capture: '/img/widgets/location_assistant_widget.png',
    minW: 1,
    minH: 2,
    permitEnvs: ['건창스치로폴'],
    component: defineAsyncComponent(
      () => import('src/components/dashboard/widgets/kcFoamingToolStatus/KcFoamingToolStatusWidget.vue'),
    ),
    settingComponent: defineAsyncComponent(
      () => import('src/components/dashboard/widgets/kcFoamingToolStatus/KcFoamingToolStatusWidgetSetting.vue'),
    ),
  },
  {
    id: 'kcBoilerToolStatus',
    name: 'KC Boiler Tool Status',
    description: t('건창스치로폴 보일러 설비의 상태를 확인할 수 있는 위젯입니다.'),
    capture: '/img/widgets/location_assistant_widget.png',
    minW: 1,
    minH: 2,
    permitEnvs: ['건창스치로폴'],
    component: defineAsyncComponent(
      () => import('src/components/dashboard/widgets/kcBoilerToolStatus/KcBoilerToolStatusWidget.vue'),
    ),
    settingComponent: defineAsyncComponent(
      () => import('src/components/dashboard/widgets/kcBoilerToolStatus/KcBoilerToolStatusWidgetSetting.vue'),
    ),
  },
  /* 범용적 위젯(건창 스치로폴 요구사항) */
  {
    id: 'CurrentTime',
    name: 'Current Time',
    description: t('현재 시간을 확인할 수 있는 위젯입니다.'),
    capture: '/img/widgets/current_time_widget.png',
    minW: 1,
    minH: 2,
    permitEnvs: ['건창스치로폴'],
    component: defineAsyncComponent(() => import('src/components/dashboard/widgets/currentTime/CurrentTimeWidget.vue')),
    settingComponent: defineAsyncComponent(
      () => import('src/components/dashboard/widgets/currentTime/CurrentTimeWidgetSetting.vue'),
    ),
  },
  {
    id: 'ProductionRate',
    name: 'Production Rate',
    description: t('설비별 생산율을 확인할 수 있는 위젯입니다.'),
    capture: '/img/widgets/production_rate_widget.png',
    minW: 1,
    minH: 2,
    permitEnvs: ['건창스치로폴'],
    component: defineAsyncComponent(
      () => import('src/components/dashboard/widgets/productionRate/ProductionRateWidget.vue'),
    ),
    settingComponent: defineAsyncComponent(
      () => import('src/components/dashboard/widgets/productionRate/ProductionRateWidgetSetting.vue'),
    ),
  },
  {
    id: 'toolParameterDataLineChart',
    name: '설비 파라미터 추이 비교',
    description: t('여러 설비의 파라미터 데이터를 라인 차트로 시간별 추이 비교합니다.'),
    capture: '/img/widgets/location_assistant_widget.png',
    minW: 3,
    minH: 8,
    component: defineAsyncComponent(
      () => import('src/components/dashboard/widgets/toolParameterDataLineChart/ToolParameterDataLineChartWidget.vue'),
    ),
    settingComponent: defineAsyncComponent(
      () =>
        import(
          'src/components/dashboard/widgets/toolParameterDataLineChart/ToolParameterDataLineChartWidgetSetting.vue'
        ),
    ),
  },
  {
    id: 'toolParameterDataBarChart',
    name: '설비 파라미터 값 비교',
    description: t('여러 설비의 파라미터 데이터를 막대 차트로 값 비교합니다.'),
    capture: '/img/widgets/location_assistant_widget.png',
    minW: 3,
    minH: 8,
    component: defineAsyncComponent(
      () => import('src/components/dashboard/widgets/toolParameterDataBarChart/ToolParameterDataBarChartWidget.vue'),
    ),
    settingComponent: defineAsyncComponent(
      () =>
        import('src/components/dashboard/widgets/toolParameterDataBarChart/ToolParameterDataBarChartWidgetSetting.vue'),
    ),
  },
  {
    id: 'toolParameterDataPieChart',
    name: '설비 파라미터 비율 분석',
    description: t('여러 설비의 파라미터 데이터를 파이 차트로 비율 분석합니다.'),
    capture: '/img/widgets/location_assistant_widget.png',
    minW: 3,
    minH: 8,
    component: defineAsyncComponent(
      () => import('src/components/dashboard/widgets/toolParameterDataPieChart/ToolParameterDataPieChartWidget.vue'),
    ),
    settingComponent: defineAsyncComponent(
      () =>
        import('src/components/dashboard/widgets/toolParameterDataPieChart/ToolParameterDataPieChartWidgetSetting.vue'),
    ),
  },
  {
    id: 'metricWidget',
    name: '메트릭 표시',
    description: t('파라미터의 최신 값을 메트릭 형식으로 표시합니다.'),
    capture: '',
    minW: 2,
    minH: 2,
    component: defineAsyncComponent(() => import('src/components/dashboard/widgets/metric/MetricWidget.vue')),
    settingComponent: defineAsyncComponent(
      () => import('src/components/dashboard/widgets/metric/MetricWidgetSetting.vue'),
    ),
  },
  {
    id: 'locationAssistant',
    name: 'Location Assistant Widget',
    description: t('위젯의 위치 조정을 위한 헬퍼 위젯입니다.'),
    capture: '/img/widgets/location_assistant_widget.png',
    minW: 1,
    minH: 3,
    component: defineAsyncComponent(
      () => import('src/components/dashboard/widgets/common/LocationAssistantWidget.vue'),
    ),
    settingComponent: null,
  },
]

export const findWidgetById = (widgetId) => {
  return widgets.find((v) => v.id === widgetId)
}

export default widgets
