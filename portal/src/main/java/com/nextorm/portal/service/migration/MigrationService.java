package com.nextorm.portal.service.migration;

import com.nextorm.common.db.entity.Parameter;
import com.nextorm.common.db.entity.ParameterData;
import com.nextorm.common.db.repository.*;
import com.nextorm.processor.parametercontainer.ParameterContainer;
import com.nextorm.processor.parametercontainer.ParameterQueryService;
import com.nextorm.processor.scriptengine.ScriptEngine;
import com.nextorm.processor.scriptengine.ScriptEngineFactory;
import com.nextorm.summarizer.service.summaryprocessor.SummaryProcessor;
import de.siegmar.fastcsv.reader.NamedCsvRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MigrationService {
	private final ParameterRepository parameterRepository;
	private final ParameterDataRepository parameterDataRepository;
	private final FaultHistoryRepository faultHistoryRepository;
	private final VirtualParameterParameterMappingRepository virtualParameterParameterMappingRepository;
	private final DcpConfigRepository dcpConfigRepository;

	private final SummaryProcessor summaryProcessor;
	private final ScriptEngineFactory scriptEngineFactory;

	public void migrate(
		Map<String, Long> headerParameterIdMap,
		MultipartFile file,
		boolean isIncludeGeoData
	) {
		CsvHeaderNameParameterMap headerNameParameterMap = convertToCsvHeaderNameParameterMap(headerParameterIdMap);
		MigrationDataWrappers parseResults = parse(headerNameParameterMap, file, isIncludeGeoData);

		// 마이그레이션 파라미터를 사용하는 가상파라미터 조회
		List<Parameter> virtualParameters = virtualParameterParameterMappingRepository.findAllByParameterIdAndIsUsingCalculation(
			new ArrayList<>(headerParameterIdMap.values()),
			true);

		if (!virtualParameters.isEmpty()) {
			List<MigrationDataWrapper> virtualParameterResult = calculateVirtualParameters(virtualParameters,
				parseResults);
			parseResults.addAll(virtualParameterResult);
		}

		for (MigrationDataWrapper migrationData : parseResults) {
			Parameter parameter = migrationData.getParameter();

			deleteParameterDataAndFaultHistory(parameter.getId(), migrationData.getStartAt(), migrationData.getEndAt());

			if (parameter.isSpecAvailable()) {
				parameterDataRepository.bulkInsertParameterData(migrationData.getParameterDataList());
				faultHistoryRepository.bulkInsertFaultHistory(migrationData.getFaultDataList());
			} else {
				parameterDataRepository.bulkInsertParameterDataRequireValue(migrationData.getParameterDataList());
			}

			summaryProcessor.reCalculateSummary(parameter.getId(),
				migrationData.getStartAt(),
				migrationData.getEndAt());
		}
	}

	private CsvHeaderNameParameterMap convertToCsvHeaderNameParameterMap(Map<String, Long> headerParameterIdMap) {
		List<Parameter> parameters = parameterRepository.findAllById(headerParameterIdMap.values());

		Map<Long, Parameter> parameterIdParameterMap = parameters.stream()
																 .collect(Collectors.toMap(Parameter::getId,
																	 parameter -> parameter));

		CsvHeaderNameParameterMap headerNameParameterMap = new CsvHeaderNameParameterMap();
		headerParameterIdMap.forEach((headerName, parameterId) -> headerNameParameterMap.addMapping(headerName,
			parameterIdParameterMap.get(parameterId)));
		return headerNameParameterMap;
	}

	/**
	 * ParameterData와 FaultHistory를 삭제한다
	 *
	 * @param parameterId: 파라미터 ID
	 * @param startAt:     삭제 범위 (시작)
	 * @param endAt:       삭제 종료 (끝)
	 */
	private void deleteParameterDataAndFaultHistory(
		Long parameterId,
		LocalDateTime startAt,
		LocalDateTime endAt
	) {
		int deletedRowCount = parameterDataRepository.deleteAllByParameterIdAndTraceAtBetween(parameterId,
			startAt,
			endAt);

		int deletedFaultRowCount = faultHistoryRepository.deleteByParameterIdAndFaultAtBetween(parameterId,
			startAt,
			endAt);

		log.info("parameterId: {}, deletedRowCount: {}, deletedFaultRowCount: {}",
			parameterId,
			deletedRowCount,
			deletedFaultRowCount);
	}

	private MigrationDataWrappers parse(
		CsvHeaderNameParameterMap headerNameParameterMap,
		MultipartFile file,
		boolean isIncludeGeoData
	) {
		MigrationDataParser parser = new MigrationDataParser();
		try {
			Function<NamedCsvRecord, MigrationBase> recordToMigrationBaseFunction = isIncludeGeoData
																					? MigrationDataParser.recordToMigrationBase
																					: MigrationDataParser.recordToMigrationBaseWithoutGeoData;

			return new MigrationDataWrappers(parser.parse(headerNameParameterMap,
				file.getInputStream(),
				recordToMigrationBaseFunction));
		} catch (IOException e) {
			throw new CsvParsingException(e);
		}
	}

	private List<MigrationDataWrapper> calculateVirtualParameters(
		List<Parameter> virtualParameters,
		MigrationDataWrappers migrationDataWrappers
	) {
		List<Long> missingParameterIds = findMissingParameterIds(virtualParameters,
			migrationDataWrappers.getParameters());

		// 가상 파라미터 계산에 사용할 파라미터 데이터 컨테이너 생성
		VirtualParameterCalculationDataContainer dataContainer = new VirtualParameterCalculationDataContainer();
		addMissingParameterDataToDataContainer(dataContainer,
			missingParameterIds,
			migrationDataWrappers.getMigrationStartAt(),
			migrationDataWrappers.getMigrationEndAt());
		addMigrationDataToDataContainer(dataContainer, migrationDataWrappers);

		// 마이그레이션 데이터의 시간 및 위치 정보
		List<MigrationBase> migrationDataBase = migrationDataWrappers.getMigrationBase();

		return virtualParameters.stream()
								.map(virtualParameter -> calculateVirtualParameter(virtualParameter,
									migrationDataBase,
									dataContainer))
								.toList();
	}

	private MigrationDataWrapper calculateVirtualParameter(
		Parameter virtualParameter,
		List<MigrationBase> migrationDataBase,
		VirtualParameterCalculationDataContainer dataContainer
	) {
		List<Parameter> calculationRequiredParameters = virtualParameter.getCalculationRequiredParameters();

		// 마이그레이션에 사용되는 base데이터를 시간 기준으로 병합한다
		List<MigrationBase> mergedCalculationBases = mergeCalculationTimeTicks(migrationDataBase,
			calculationRequiredParameters,
			dataContainer);

		// 가상 파라미터 계산
		ParameterContainer parameterContainer = createAndInitContainer(virtualParameter);
		ScriptEngine engine = scriptEngineFactory.createEngine(virtualParameter.getVirtualScript(), parameterContainer);

		MigrationDataWrapper migrationDataWrapper = new MigrationDataWrapper(virtualParameter);
		LocalDateTime previousTraceAt = null;
		for (MigrationBase base : mergedCalculationBases) {
			Map<Long, Object> parameterValues = getLatestParameterValues(calculationRequiredParameters,
				base.getTraceAt(),
				dataContainer);

			Map<Long, Object> previousParameterValues = Map.of();
			if (previousTraceAt != null) {
				previousParameterValues = getLatestParameterValues(calculationRequiredParameters,
					previousTraceAt,
					dataContainer);
			}

			String value = engine.execute(virtualParameter.getId(), parameterValues, previousParameterValues)
								 .toString();

			migrationDataWrapper.addData(MigrationData.of(value,
				base.getTraceAt(),
				base.getLatitude(),
				base.getLongitude()));

			previousTraceAt = base.getTraceAt();
		}

		engine.close();
		return migrationDataWrapper;
	}

	private ParameterContainer createAndInitContainer(Parameter virtualParameter) {
		ParameterContainer parameterContainer = new ParameterContainer(new ParameterQueryService(dcpConfigRepository,
			parameterRepository));
		parameterContainer.initParametersByToolId(virtualParameter.getTool()
																  .getId(), List.of());
		return parameterContainer;
	}

	private List<MigrationBase> mergeCalculationTimeTicks(
		List<MigrationBase> migrationDataBase,
		List<Parameter> calculationRequiredParameters,
		VirtualParameterCalculationDataContainer dataContainer
	) {
		long s = System.currentTimeMillis();
		Map<LocalDateTime, MigrationBase> mergeBases = new HashMap<>();

		for (Parameter parameter : calculationRequiredParameters) {
			ConcurrentSkipListMap<LocalDateTime, MigrationData> dataListMap = dataContainer.getData(parameter.getId());
			for (Map.Entry<LocalDateTime, MigrationData> entry : dataListMap.entrySet()) {
				MigrationData migrationData = entry.getValue();
				mergeBases.put(entry.getKey(), migrationData.toMigrationBase());
			}
		}

		mergeBases.putAll(migrationDataBase.stream()
										   .collect(Collectors.toMap(MigrationBase::getTraceAt, it -> it)));

		List<MigrationBase> result = mergeBases.values()
											   .stream()
											   .sorted(Comparator.comparing(MigrationBase::getTraceAt))
											   .toList();
		log.info("mergeCalculationTimeTicks: {}ms", System.currentTimeMillis() - s);
		return result;
	}

	private void addMigrationDataToDataContainer(
		VirtualParameterCalculationDataContainer dataContainer,
		MigrationDataWrappers migrationDataWrappers
	) {
		for (Parameter parameter : migrationDataWrappers.getParameters()) {
			List<ParameterData> parameterDataList = migrationDataWrappers.getParameterDataList(parameter.getId());
			dataContainer.addData(parameter.getId(), parameterDataList);
		}
	}

	private Map<Long, Object> getLatestParameterValues(
		List<Parameter> calculationRequiredParameters,
		LocalDateTime baseTime,
		VirtualParameterCalculationDataContainer dataContainer
	) {
		Map<Long, Object> parameterValues = new HashMap<>();
		for (Parameter needParameter : calculationRequiredParameters) {
			Map.Entry<LocalDateTime, MigrationData> latestData = dataContainer.getLatestData(needParameter.getId(),
				baseTime);
			MigrationData migrationData = latestData.getValue();
			parameterValues.put(needParameter.getId(), migrationData.getValue());
		}
		return parameterValues;
	}

	private void addMissingParameterDataToDataContainer(
		VirtualParameterCalculationDataContainer dataContainer,
		List<Long> missingParameterIds,
		LocalDateTime migrationStartAt,
		LocalDateTime migrationEndAt
	) {
		for (Long parameterId : missingParameterIds) {
			List<ParameterData> parameterDataList = parameterDataRepository.findByParameterIdAndTraceAtBetween(
				parameterId,
				migrationStartAt,
				migrationEndAt,
				null);
			dataContainer.addData(parameterId, parameterDataList);
		}
	}

	/**
	 * 가상 파라미터 계산에 필요하지만, 마이그레이션 대상이 아닌 파라미터 ID를 찾는다
	 *
	 * @param virtualParameters:   가상 파라미터 리스트
	 * @param migrationParameters: 마이그레이션 대상 파라미터 리스트
	 * @return 가상 파라미터 계산에 필요하지만, 마이그레이션 대상이 아닌 파라미터 ID 리스트
	 */
	private List<Long> findMissingParameterIds(
		List<Parameter> virtualParameters,
		List<Parameter> migrationParameters
	) {
		Set<Long> migrationParameterIds = migrationParameters.stream()
															 .map(Parameter::getId)
															 .collect(Collectors.toSet());

		List<Long> calculationRequiredParameterIds = virtualParameters.stream()
																	  .map(Parameter::getCalculationRequiredParameters)
																	  .flatMap(Collection::stream)
																	  .mapToLong(Parameter::getId)
																	  .distinct()
																	  .boxed()
																	  .toList();

		List<Long> notIncludeIds = new ArrayList<>();
		for (Long requireParameterId : calculationRequiredParameterIds) {
			if (!migrationParameterIds.contains(requireParameterId)) {
				notIncludeIds.add(requireParameterId);
			}
		}
		return notIncludeIds;
	}

	private static class VirtualParameterCalculationDataContainer {
		private final ConcurrentHashMap<Long, ConcurrentSkipListMap<LocalDateTime, MigrationData>> dataContainer = new ConcurrentHashMap<>();

		public void addData(
			Long parameterId,
			List<ParameterData> parameterDataList
		) {
			ConcurrentSkipListMap<LocalDateTime, MigrationData> concurrentSkipListMap = convertToConcurrentSkipListMap(
				parameterDataList);
			dataContainer.put(parameterId, concurrentSkipListMap);
		}

		public ConcurrentSkipListMap<LocalDateTime, MigrationData> getData(Long parameterId) {
			return dataContainer.get(parameterId);
		}

		private ConcurrentSkipListMap<LocalDateTime, MigrationData> convertToConcurrentSkipListMap(List<ParameterData> parameterDataList) {
			return new ConcurrentSkipListMap<>(parameterDataList.stream()
																.collect(Collectors.toMap(ParameterData::getTraceAt,
																	it -> MigrationData.of(it.getValue()
																							 .toString(),
																		it.getTraceAt(),
																		it.getLatitudeValue(),
																		it.getLongitudeValue()))));
		}

		public Map.Entry<LocalDateTime, MigrationData> getLatestData(
			Long parameterId,
			LocalDateTime traceAt
		) {
			ConcurrentSkipListMap<LocalDateTime, MigrationData> values = dataContainer.get(parameterId);
			return values.floorEntry(traceAt);
		}
	}
}
