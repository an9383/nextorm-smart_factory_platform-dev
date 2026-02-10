package com.nextorm.processor.worker.dataprocessor;

import com.nextorm.common.db.entity.FaultHistory;
import com.nextorm.common.db.entity.Parameter;
import com.nextorm.common.db.entity.ParameterData;
import com.nextorm.processor.model.ParameterModel;
import com.nextorm.processor.parametercontainer.DcpId;
import com.nextorm.processor.parametercontainer.ParameterContainer;
import com.nextorm.processor.parametercontainer.Parameters;
import com.nextorm.processor.scriptengine.ScriptEngine;
import com.nextorm.processor.scriptengine.ScriptEngineFactory;
import com.nextorm.processor.service.AutoSpecCalculator;
import com.nextorm.processor.service.DcpConfigGeoInfo;
import com.nextorm.processor.service.WorkerService;
import com.nextorm.processor.worker.datacollector.CollectData;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class CollectDataProcessor {
	private final Long toolId;
	private final ParameterContainer parameterContainer;
	private final AutoSpecCalculator autoSpecCalculator;
	private final WorkerService workerService;
	private final ScriptEngineFactory scriptEngineFactory;

	private final Map<Long, ScriptEngine> scriptEngineMap = new HashMap<>();
	private final Map<Long, Object> parameterLatestValueMap = new HashMap<>();
	/**
	 * DCP의 Geo데이터 정보를 저장하는 맵
	 * KEY: DCP_CONFIG_ID
	 */
	private final Map<Long, DcpConfigGeoInfo> dcpConfigGeoInfoMap = new HashMap<>();

	public CollectDataProcessor(
		Long toolId,
		ParameterContainer parameterContainer,
		AutoSpecCalculator autoSpecCalculator,
		WorkerService workerService,
		ScriptEngineFactory scriptEngineFactory
	) {
		this.toolId = toolId;
		this.parameterContainer = parameterContainer;
		this.autoSpecCalculator = autoSpecCalculator;
		this.workerService = workerService;
		this.scriptEngineFactory = scriptEngineFactory;
	}

	public void init() {
		Parameters parameters = parameterContainer.getParametersByToolId(toolId);
		initParameterHistoriesMap(parameters.getParameters());
		initScriptEngines(parameters.getVirtualParameters());
	}

	private void initParameterHistoriesMap(List<ParameterModel> parameters) {
		List<Long> parameterIds = parameters.stream()
											.map(ParameterModel::getId)
											.toList();

		LocalDateTime baseDateTime = LocalDateTime.now()
												  .minusMonths(1L);

		List<ParameterData> parameterDataList = workerService.getParameterDataByParameterIdInAndTraceAtGreaterThanEquals(
			parameterIds,
			baseDateTime);

		for (ParameterData parameterData : parameterDataList) {
			parameterLatestValueMap.put(parameterData.getParameterId(),
				parameterData.getValue(parameterData.getDataType()));
		}
	}

	private void initScriptEngines(List<ParameterModel> virtualParameters) {
		for (ParameterModel virtualParameter : virtualParameters) {
			try {
				ScriptEngine engine = scriptEngineFactory.createEngine(virtualParameter.getVirtualScript(),
					parameterContainer);
				scriptEngineMap.put(virtualParameter.getId(), engine);
				log.info("SET SCRIPT ENGINE: {}", virtualParameter.getName());
			} catch (RuntimeException e) {
				throw new IllegalArgumentException("ScriptEngine 생성 실패: %s".formatted(virtualParameter.getName()), e);
			}
		}
	}

	public ProcessCollectDataResult processCollectDataList(final List<CollectData> collectDataList) {
		int initParameterDataListSize = collectDataList.size() * parameterLatestValueMap.size();
		// parameterDataList 초기 용량 설정 (메모리 재할당 최소화)
		List<ParameterData> parameterDataList = new ArrayList<>(initParameterDataListSize);
		List<FaultHistory> faultHistoryList = new ArrayList<>();

		for (CollectData collectData : collectDataList) {
			CollectedDataProcessContext context = createProcessContext(collectData);
			processCollectData(collectData, context);

			parameterDataList.addAll(context.getParameterDataList());
			faultHistoryList.addAll(context.getFaultHistoryList());
		}
		return new ProcessCollectDataResult(parameterDataList, faultHistoryList);
	}

	private CollectedDataProcessContext createProcessContext(CollectData collectData) {
		DcpId dcpId = DcpId.of(collectData.getDcpId());
		Parameters parameters = parameterContainer.getParametersByDcpId(dcpId);
		LatLng latLng = parseGeoData(collectData);
		LocalDateTime traceAt = collectData.getTraceAt();

		return new CollectedDataProcessContext(parameters, latLng, traceAt);
	}

	private void processCollectData(
		CollectData collectData,
		CollectedDataProcessContext context
	) {
		Map<Long, Object> paramValueMap = extractAndComputeAllParameterValues(collectData, context);

		// 파라미터 값이 있으면 최근 값으로 업데이트
		if (!paramValueMap.isEmpty()) {
			parameterLatestValueMap.putAll(paramValueMap);
			workerService.updateLastCollectedAt(collectData.getDcpId(), collectData.getTraceAt());
		}

		processParametersAndDetectFaults(paramValueMap, context);
	}

	/**
	 * 수집 데이터에서 단순 수집 파라미터 값을 파싱하고, 가상 파라미터를 계산하여 모든 파라미터 값을 반환
	 *
	 * @param collectData: 수집 데이터
	 * @param context:     수집 데이터 처리 컨텍스트
	 */
	private Map<Long, Object> extractAndComputeAllParameterValues(
		CollectData collectData,
		CollectedDataProcessContext context
	) {
		Map<Long, Object> paramValueMap = new HashMap<>();
		Parameters parameters = context.getParameters();

		// 수집 파라미터 파싱
		parseCollectParameter(collectData, parameters.getCollectParameters(), paramValueMap);

		// 가상 파라미터 계산
		calculateVirtualParameters(parameters.getVirtualParameters(), paramValueMap);

		return paramValueMap;
	}

	private void processParametersAndDetectFaults(
		Map<Long, Object> paramValueMap,
		CollectedDataProcessContext context
	) {
		Parameters parameters = context.getParameters();
		LocalDateTime traceAt = context.getTraceAt();
		LatLng latLng = context.getLatLng();

		for (ParameterModel parameter : parameters) {
			Object value = paramValueMap.get(parameter.getId());
			if (value == null) {
				continue;
			}

			if (parameter.isAutoSpecCalculationTarget()) {
				triggerAutoSpecCalculation(parameter);
			}

			ParameterData parameterData = createParameterData(parameter, value, traceAt, latLng);
			// Spec 이상치 검출
			if (parameter.isManualSpecCalculationTarget() && parameterData.isSpecOver()) {
				context.addFaultHistory(parameterData.toFaultHistory());
			}

			context.addParameterData(parameterData);
		}
	}

	private void triggerAutoSpecCalculation(ParameterModel parameter) {
		autoSpecCalculator.calculate(parameter.getId());
		parameter.disableAutoSpecCalculationTarget();
	}

	private ParameterData createParameterData(
		ParameterModel parameter,
		Object value,
		LocalDateTime traceAt,
		LatLng latLng
	) {
		return ParameterData.createOf(parameter.toEntity(), value, traceAt, latLng.latitude(), latLng.longitude());
	}

	private LatLng parseGeoData(CollectData collectData) {
		DcpConfigGeoInfo geoInfo = getDcpConfigGeoInfo(collectData.getDcpId());
		return extractGeoCoordinates(collectData, geoInfo);
	}

	private DcpConfigGeoInfo getDcpConfigGeoInfo(Long dcpId) {
		return dcpConfigGeoInfoMap.computeIfAbsent(dcpId, id -> workerService.getDcpGeoInfo(id));
	}

	private LatLng extractGeoCoordinates(
		CollectData collectData,
		DcpConfigGeoInfo geoInfo
	) {
		if (!geoInfo.isGeoDataType()) {
			return LatLng.empty();
		}

		Double latitude = extractCoordinate(collectData, geoInfo.getLatitudeParameterName());
		Double longitude = extractCoordinate(collectData, geoInfo.getLongitudeParameterName());

		return new LatLng(latitude, longitude);
	}

	private Double extractCoordinate(
		CollectData collectData,
		String parameterName
	) {
		if (parameterName == null) {
			return null;
		}
		Object value = collectData.getValues()
								  .get(parameterName);
		return value != null
			   ? Double.valueOf(value.toString())
			   : null;
	}

	/**
	 * Collect Data에서 단순 수집 파라미터의 값을 파싱하여 paramValueMap에 저장
	 *
	 * @param collectData:       수집 데이터
	 * @param collectParameters: 단순 수집 대상 파라미터
	 * @param paramValueMap:     파싱한 값이 저장될 맵
	 */
	private void parseCollectParameter(
		CollectData collectData,
		List<ParameterModel> collectParameters,
		Map<Long, Object> paramValueMap
	) {
		for (ParameterModel parameter : collectParameters) {
			Object collectedValue = collectData.getValueByParameterName(parameter.getName());
			if (collectedValue == null) {
				log.warn("수집 데이터에서 파라미터 값을 찾을 수 없음. DCP_ID: {}, ParameterName: {}",
					collectData.getDcpId(),
					parameter.getName());
				continue;
			}
			Object castingValue = castingValue(collectedValue, parameter.getDataType(), parameter.getName());
			paramValueMap.put(parameter.getId(), castingValue);
		}
	}

	private Object castingValue(
		Object value,
		Parameter.DataType dataType,
		String parameterName
	) {
		return switch (dataType) {
			case DOUBLE -> {
				try {
					yield Double.valueOf(value.toString());
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException("Double Data Type에 맞지않는 값: " + value + " (parameter: " + parameterName + ")");
				}
			}
			case INTEGER -> {
				try {
					yield Double.valueOf(value.toString())
								.intValue();
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException("Integer Data Type에 맞지않는 값: " + value + " (parameter: " + parameterName + ")");
				}
			}
			case STRING, IMAGE -> String.valueOf(value);
		};
	}

	/**
	 * 가상 파라미터 계산
	 *
	 * @param virtualParameters: 가상 파라미터 목록
	 * @param paramValueMap:     파라미터 값이 저장된 맵
	 */
	private void calculateVirtualParameters(
		List<ParameterModel> virtualParameters,
		Map<Long, Object> paramValueMap
	) {
		for (ParameterModel parameter : virtualParameters) {
			Object executedValue = executeScriptEngine(parameter.getId(), paramValueMap);
			// 가상 파라미터 계산 결과가 null이면 결과에 포함하지 않음
			if (executedValue == null) {
				continue;
			}
			paramValueMap.put(parameter.getId(), executedValue);
		}
	}

	private Object executeScriptEngine(
		Long parameterId,
		Map<Long, Object> paramValueMap
	) {
		Map<Long, Object> mergedParameterValuesMap = new HashMap<>(parameterLatestValueMap);
		for (Map.Entry<Long, Object> entry : paramValueMap.entrySet()) {
			if (entry.getValue() != null) {
				mergedParameterValuesMap.put(entry.getKey(), entry.getValue());
			}
		}

		ScriptEngine scriptEngine = scriptEngineMap.get(parameterId);
		long s = System.currentTimeMillis();
		Object result = scriptEngine.execute(parameterId, mergedParameterValuesMap, parameterLatestValueMap);
		log.info("engine executed: id: {}, {}ms", parameterId, System.currentTimeMillis() - s);
		return result;
	}

	@Getter
	private static class CollectedDataProcessContext {
		private final Parameters parameters;
		private final LatLng latLng;
		private final LocalDateTime traceAt;
		private final List<ParameterData> parameterDataList = new ArrayList<>();
		private final List<FaultHistory> faultHistoryList = new ArrayList<>();

		public CollectedDataProcessContext(
			Parameters parameters,
			LatLng latLng,
			LocalDateTime traceAt
		) {
			this.parameters = parameters;
			this.latLng = latLng;
			this.traceAt = traceAt;
		}

		public void addParameterData(ParameterData parameterData) {
			this.parameterDataList.add(parameterData);
		}

		public void addFaultHistory(FaultHistory faultHistory) {
			this.faultHistoryList.add(faultHistory);
		}
	}

	/**
	 * 수집한 데이터로부터 geo 데이터를 파싱하여 저장하는 레코드
	 */
	record LatLng(Double latitude, Double longitude) {
		static LatLng empty() {
			return new LatLng(null, null);
		}
	}
}
