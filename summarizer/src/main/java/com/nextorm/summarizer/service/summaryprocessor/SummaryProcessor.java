package com.nextorm.summarizer.service.summaryprocessor;

import com.nextorm.common.db.entity.HealthSummaryData;
import com.nextorm.common.db.entity.Parameter;
import com.nextorm.common.db.entity.ParameterData;
import com.nextorm.common.db.entity.SummaryData;
import com.nextorm.common.db.entity.enums.SummaryPeriodType;
import com.nextorm.common.db.repository.HealthSummaryDataRepository;
import com.nextorm.common.db.repository.ParameterDataRepository;
import com.nextorm.common.db.repository.ParameterRepository;
import com.nextorm.common.db.repository.SummaryDataRepository;
import com.nextorm.summarizer.SummaryRange;
import com.nextorm.summarizer.dto.GeoDataDto;
import com.nextorm.summarizer.dto.SummaryDataDto;
import com.nextorm.summarizer.utils.DoubleUtility;
import com.nextorm.summarizer.utils.SPCBaseCalc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SummaryProcessor {
	private final ParameterDataRepository parameterDataRepository;
	private final SummaryDataRepository summaryDataRepository;
	private final HealthSummaryDataRepository healthSummaryDataRepository;
	private final ParameterRepository parameterRepository;

	public void process(
		SummaryRange summaryRange,
		List<Parameter> parameters
	) {
		Map<Long, List<ParameterData>> parameterDataListGroup = getParameterDataListGroupByParameterId(parameters,
			summaryRange);

		List<SummaryDataDto> summaryDataDtoList = calculateSummary(summaryRange, parameterDataListGroup, parameters);

		// ParameterId와 Parameter 매핑 생성
		Map<Long, Parameter> parameterMap = parameters.stream()
													  .collect(Collectors.toMap(Parameter::getId,
														  parameter -> parameter));

		List<SummaryData> summaryDataList = new ArrayList<>();
		List<HealthSummaryData> healthSummaryDataList = new ArrayList<>();
		separateSummaryDataByType(summaryDataDtoList,
			parameterMap,
			summaryDataList,
			healthSummaryDataList,
			summaryRange);

		//SummaryDataList 및 HealthSummaryDataList DB에 저장
		summaryDataRepository.saveAll(summaryDataList);
		if (!healthSummaryDataList.isEmpty()) {
			healthSummaryDataRepository.saveAll(healthSummaryDataList);
		}
	}

	/*
		파라미터 타입이 HEALTH와 아닌거 리스트 분리
	 */
	private void separateSummaryDataByType(
		List<SummaryDataDto> summaryDataDtoList,
		Map<Long, Parameter> parameterMap,
		List<SummaryData> summaryDataList,
		List<HealthSummaryData> healthSummaryDataList,
		SummaryRange summaryRange
	) {
		for (SummaryDataDto summaryDataDto : summaryDataDtoList) {
			Parameter parameter = parameterMap.get(summaryDataDto.getParameterId());
			if (parameter == null) {
				continue;
			}
			if (parameter.getType() == Parameter.Type.HEALTH) {
				if (summaryRange.getSummaryType() == SummaryPeriodType.DAILY) {
					healthSummaryDataList.add(summaryDataDto.toHealthSummaryData());
				}
			} else {
				summaryDataList.add(summaryDataDto.toSummaryData());
			}
		}
	}

	private Map<Long, List<ParameterData>> getParameterDataListGroupByParameterId(
		final List<Parameter> parameters,
		final SummaryRange summaryRange
	) {
		LocalDateTime startTime = summaryRange.getStartTime();
		LocalDateTime endTime = summaryRange.getEndTime();

		List<Long> parameterIds = parameters.stream()
											.map(Parameter::getId)
											.toList();
		List<ParameterData> parameterDataList = parameterDataRepository.findByParameterIdInAndTraceAtGreaterThanEqualAndTraceAtLessThan(
			parameterIds,
			startTime,
			endTime,
			Sort.by(Sort.Direction.ASC, "traceAt"));

		if (parameterDataList.isEmpty()) {
			return Map.of();
		}

		return buildParamGroupMap(parameters, parameterDataList);
	}

	private Map<Long, List<ParameterData>> buildParamGroupMap(
		List<Parameter> parameters,
		List<ParameterData> parameterDataList
	) {
		Map<Long, List<ParameterData>> paramValueMap = new LinkedHashMap<>();
		for (Parameter parameter : parameters) {
			List<ParameterData> parameterDatas = parameterDataList.stream()
																  .filter(v -> v.getParameterId()
																				.equals(parameter.getId()) && (v.getSValue() == null))
																  .toList();
			paramValueMap.put(parameter.getId(), parameterDatas);
		}
		return paramValueMap;
	}

	private List<SummaryDataDto> calculateSummary(
		final SummaryRange summaryRange,
		final Map<Long, List<ParameterData>> paramValueMap,
		final List<Parameter> parameters
	) {
		List<SummaryDataDto> summaryDataDtoList = new ArrayList<>();

		for (Long paramId : paramValueMap.keySet()) {
			List<ParameterData> parameterDataList = paramValueMap.getOrDefault(paramId, List.of());
			if (parameterDataList.isEmpty()) {
				continue;
			}

			Parameter parameter = parameters.stream()
											.filter(v -> v.getId()
														  .equals(paramId))
											.findAny()
											.orElse(null);

			if (parameter.getType() == Parameter.Type.HEALTH && summaryRange.getSummaryType() == SummaryPeriodType.DAILY) {
				summaryDataDtoList.addAll(calculateHealthDailySummary(summaryRange, parameter, parameterDataList));
			} else if (parameter.getType() != Parameter.Type.HEALTH) {
				summaryDataDtoList.add(calculateSummary(summaryRange, parameter, parameterDataList));
			}
		}
		return summaryDataDtoList;
	}

	private List<SummaryDataDto> calculateHealthDailySummary(
		SummaryRange summaryRange,
		Parameter parameter,
		List<ParameterData> parameterDataList
	) {
		List<SummaryDataDto> healthDailySummaryDtos = new ArrayList<>();

		Map<GeoDataDto, List<ParameterData>> geoDataGroupSummaryMap = parameterDataList.stream()
																					   .collect(Collectors.groupingBy(
																						   data -> new GeoDataDto(
																							   DoubleUtility.floorVal(
																								   data.getLatitudeValue(),
																								   4),
																							   DoubleUtility.floorVal(
																								   data.getLongitudeValue(),
																								   4))));
		for (Map.Entry<GeoDataDto, List<ParameterData>> entry : geoDataGroupSummaryMap.entrySet()) {
			GeoDataDto geoKey = entry.getKey();
			int totFaultCount = 0;
			int totCtrlLimitOverCount = 0;
			int totSpecLimitOvercount = 0;
			int totTraceRawDataCount = 0;
			List<ParameterData> groupedParameterDataList = entry.getValue();

			SPCBaseCalc traceRawDataCalc = new SPCBaseCalc();
			traceRawDataCalc.addLatitude(geoKey.getLatitude());
			traceRawDataCalc.addLongitude(geoKey.getLongitude());
			for (ParameterData data : groupedParameterDataList) {
				if (data.isNumberData()) {
					traceRawDataCalc.addValue(data.getNumberValue()
												  .doubleValue());
				}
				if (data.isCtrlLimitOver()) {
					totCtrlLimitOverCount++;
				}
				if (data.isSpecLimitOver()) {
					totSpecLimitOvercount++;
				}
				totTraceRawDataCount++;
			}
			totFaultCount = totCtrlLimitOverCount + totSpecLimitOvercount;

			if (traceRawDataCalc.getDataCount() > 0) {
				if (parameter.getUsl() != null && parameter.getLsl() != null) {
					traceRawDataCalc.calculate(parameter.getUsl(), parameter.getLsl());
				} else {
					traceRawDataCalc.calculate();
				}
				traceRawDataCalc.roundVal(5);
			}

			ParameterData firstData = parameterDataList.get(0);
			ParameterData lastData = parameterDataList.get(parameterDataList.size() - 1);

			SummaryDataDto healDailySummaryDataDto = createSummaryDataDto(summaryRange,
				parameter,
				firstData,
				lastData,
				totFaultCount,
				totCtrlLimitOverCount,
				totSpecLimitOvercount,
				totTraceRawDataCount,
				traceRawDataCalc);
			healthDailySummaryDtos.add(healDailySummaryDataDto);
		}
		return healthDailySummaryDtos;
	}

	public SummaryDataDto calculateSummary(
		SummaryRange summaryRange,
		Parameter parameter,
		List<ParameterData> parameterDataList
	) {
		int totFaultCount = 0;
		int totCtrlLimitOverCount = 0;
		int totSpecLimitOvercount = 0;
		int totTraceRawDataCount = 0;

		SPCBaseCalc traceRawDataCalc = new SPCBaseCalc();
		for (ParameterData data : parameterDataList) {
			if (data.isNumberData()) {
				traceRawDataCalc.addValue(data.getNumberValue()
											  .doubleValue());
			}
			if (data.isCtrlLimitOver()) {
				totCtrlLimitOverCount++;
			}
			if (data.isSpecLimitOver()) {
				totSpecLimitOvercount++;
			}
			totTraceRawDataCount++;
		}
		totFaultCount = totCtrlLimitOverCount + totSpecLimitOvercount;

		if (traceRawDataCalc.getDataCount() > 0) {
			if (parameter.getUsl() != null && parameter.getLsl() != null) {
				traceRawDataCalc.calculate(parameter.getUsl(), parameter.getLsl());
			} else {
				traceRawDataCalc.calculate();
			}
			traceRawDataCalc.roundVal(5);
		}

		ParameterData firstData = parameterDataList.get(0);
		ParameterData lastData = parameterDataList.get(parameterDataList.size() - 1);

		return createSummaryDataDto(summaryRange,
			parameter,
			firstData,
			lastData,
			totFaultCount,
			totCtrlLimitOverCount,
			totSpecLimitOvercount,
			totTraceRawDataCount,
			traceRawDataCalc);
	}

	private SummaryDataDto createSummaryDataDto(
		SummaryRange summaryRange,
		Parameter parameter,
		ParameterData firstData,
		ParameterData lastData,
		int totFaultCount,
		int totCtrlLimitOverCount,
		int totSpecLimitOverCount,
		int totTraceRawDataCount,
		SPCBaseCalc traceRawDataCalc
	) {
		SummaryDataDto.SummaryDataDtoBuilder builder = SummaryDataDto.builder()
																	 .parameterId(parameter.getId())
																	 .latitude(traceRawDataCalc.getLatitude())
																	 .longitude(traceRawDataCalc.getLongitude())
																	 .periodType(summaryRange.getSummaryType())
																	 .sumStartBaseAt(summaryRange.getStartTime())
																	 .sumEndBaseAt(summaryRange.getEndTime())

																	 .trxStartAt(firstData.getTraceAt())
																	 .trxEndAt(lastData.getTraceAt())

																	 .lcl(parameter.getLcl())
																	 .ucl(parameter.getUcl())
																	 .lsl(parameter.getLsl())
																	 .usl(parameter.getUsl())

																	 .realDataCount(totTraceRawDataCount)
																	 .faultCount(totFaultCount)
																	 .ctrlLimitOverCount(totCtrlLimitOverCount)
																	 .specLimitOverCount(totSpecLimitOverCount)

																	 .min(traceRawDataCalc.getDMin())
																	 .max(traceRawDataCalc.getDMax())
																	 .sum(traceRawDataCalc.getDSum())
																	 .avg(traceRawDataCalc.getDAvg())
																	 .median(traceRawDataCalc.getDMedian())
																	 .q1(traceRawDataCalc.getD1_3Median())
																	 .q3(traceRawDataCalc.getD2_3Median())
																	 .std(traceRawDataCalc.getDSTD())
																	 .r(traceRawDataCalc.getRange())
																	 .cp(traceRawDataCalc.getCp())
																	 .cpk(traceRawDataCalc.getCpk())
																	 .cpu(traceRawDataCalc.getCpu())
																	 .cpl(traceRawDataCalc.getCpl())
																	 .ewma(traceRawDataCalc.getEwma());

		Pair<Double, Double> startEndValue = switch (parameter.getDataType()) {
			case DOUBLE -> Pair.of(firstData.getDValue(), lastData.getDValue());
			case INTEGER -> Pair.of(firstData.getIValue()
											 .doubleValue(),
				lastData.getIValue()
						.doubleValue());
			case STRING, IMAGE -> null;
		};

		if (startEndValue != null) {
			builder.startValue(startEndValue.getFirst())
				   .endValue(startEndValue.getSecond());
		}

		return builder.build();
	}

	public void reCalculateSummary(
		Long parameterId,
		LocalDateTime startTime,
		LocalDateTime endTime
	) {
		final long queryHoursUnit = 3L;    // Raw데이터를 조회할 시간단위

		// 시작일의 00시부터 다시 계산해야하므로 LocalTime.MIN으로 설정
		LocalDateTime currentQueryDateTime = LocalDateTime.of(startTime.toLocalDate(), LocalTime.MIN);
		// 종료일의 데이터를 모두 계산해야하므로, 종료시간의 다음날 00시로 설정
		LocalDateTime summaryEndDate = LocalDateTime.of(endTime.toLocalDate(), LocalTime.MIN)
													.plusDays(1L);
		Parameter parameter = parameterRepository.findById(parameterId)
												 .get();
		List<Long> parameterIds = List.of(parameter.getId());

		// 기존에 저장된 섬머리 데이터를 지운다.
		if (parameter.getType() == Parameter.Type.HEALTH) {
			healthSummaryDataRepository.deleteByParameterIdAndPeriodTypeInAndTrxStartAtGreaterThanEqualAndTrxStartAtLessThan(
				parameterId,
				List.of(SummaryPeriodType.values()),
				currentQueryDateTime,
				summaryEndDate);
		} else {
			summaryDataRepository.deleteByParameterIdAndPeriodTypeInAndTrxStartAtGreaterThanEqualAndTrxStartAtLessThan(
				parameterId,
				List.of(SummaryPeriodType.values()),
				currentQueryDateTime,
				summaryEndDate);
		}

		List<SummaryDataDto> summaryDataDtoList = new ArrayList<>();
		ParameterDataClassifier parameterDataClassifier = new ParameterDataClassifier();
		while (currentQueryDateTime.isBefore(summaryEndDate)) {
			LocalDateTime queryEnd = currentQueryDateTime.plusHours(queryHoursUnit);

			List<ParameterData> parameterDataList = parameterDataRepository.findByParameterIdInAndTraceAtGreaterThanEqualAndTraceAtLessThan(
				parameterIds,
				currentQueryDateTime,
				queryEnd,
				Sort.by(Sort.Direction.ASC, "traceAt"));

			parameterDataClassifier.addParameterDataList(parameterDataList);
			currentQueryDateTime = currentQueryDateTime.plusHours(queryHoursUnit);
		}

		Map<SummaryRange, List<ParameterData>> classifiedParameterData = parameterDataClassifier.getClassifiedParameterData();
		for (Map.Entry<SummaryRange, List<ParameterData>> entry : classifiedParameterData.entrySet()) {
			SummaryRange summaryRange = entry.getKey();
			//파라미터 타입이 HEALTH이면서 DAILY가 아닌거는 Summary를 하지 않는다.
			if (parameter.getType() == Parameter.Type.HEALTH && !(summaryRange.getSummaryType() == SummaryPeriodType.DAILY)) {
				continue;
			}
			List<ParameterData> parameterDataList = entry.getValue();
			if (parameter.getType() == Parameter.Type.HEALTH && summaryRange.getSummaryType() == SummaryPeriodType.DAILY) {
				summaryDataDtoList.addAll(calculateHealthDailySummary(summaryRange, parameter, parameterDataList));
			} else if (parameter.getType() != Parameter.Type.HEALTH) {
				SummaryDataDto summaryDataDto = calculateSummary(summaryRange, parameter, parameterDataList);
				summaryDataDtoList.add(summaryDataDto);
			}
		}

		if (parameter.getType() == Parameter.Type.HEALTH) {
			healthSummaryDataRepository.saveAll(summaryDataDtoList.stream()
																  .map(SummaryDataDto::toHealthSummaryData)
																  .toList());
		} else {
			summaryDataRepository.saveAll(summaryDataDtoList.stream()
															.map(SummaryDataDto::toSummaryData)
															.toList());
		}
	}
}
