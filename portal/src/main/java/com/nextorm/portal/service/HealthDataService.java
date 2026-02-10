package com.nextorm.portal.service;

import com.nextorm.common.db.entity.HealthSummaryData;
import com.nextorm.common.db.entity.Parameter;
import com.nextorm.common.db.entity.ParameterData;
import com.nextorm.common.db.entity.enums.SummaryPeriodType;
import com.nextorm.common.db.repository.HealthSummaryDataRepository;
import com.nextorm.common.db.repository.ParameterDataRepository;
import com.nextorm.common.db.repository.ParameterRepository;
import com.nextorm.portal.dto.parameterdata.GPSCoordinateDto;
import com.nextorm.portal.dto.parameterdata.HeatMapHealthDataResponseDto;
import com.nextorm.portal.dto.parameterdata.MonthlyHealthDataDto;
import com.nextorm.portal.dto.parameterdata.MonthlyHealthScoreByLocationResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HealthDataService {
	private final ParameterRepository parameterRepository;
	private final HealthSummaryDataRepository healthSummaryDataRepository;
	private final ParameterDataRepository parameterDataRepository;

	public List<MonthlyHealthScoreByLocationResponseDto> getMonthlyHeatMapHealthData(
		List<Long> parameterIds,
		LocalDateTime fromDate,
		LocalDateTime toDate
	) {
		List<HealthSummaryData> healthSummaryDatas = healthSummaryDataRepository.findAllByParameterIdInAndPeriodTypeAndTrxStartAtBetween(
			parameterIds,
			SummaryPeriodType.DAILY,
			fromDate,
			toDate);

		Map<YearMonth, List<HealthSummaryData>> yearMonthMap = groupingYearMonth(healthSummaryDatas);

		return toLatestHealthScoreList(parameterIds, yearMonthMap);

	}

	private List<MonthlyHealthScoreByLocationResponseDto> toLatestHealthScoreList(
		List<Long> parameterIds,
		Map<YearMonth, List<HealthSummaryData>> yearMonthMap
	) {
		List<Parameter> parameters = parameterRepository.findByIdIn(parameterIds);

		return yearMonthMap.entrySet()
						   .stream()
						   .flatMap(entry -> {
							   List<HealthSummaryData> healthSummaryDatas = entry.getValue();
							   Map<GPSCoordinateDto, List<HealthSummaryData>> gpsCoordinateMap = groupingGpsCoordinateBySummaryData(
								   healthSummaryDatas);

							   return gpsCoordinateMap.entrySet()
													  .stream()
													  .map(gpsEntry -> {
														  List<MonthlyHealthScoreByLocationResponseDto.HealthData> healthDatas = toHealthData(
															  parameters,
															  gpsEntry.getValue());

														  int avgHealthScore = calculateAverageHealthScore(gpsEntry.getValue());

														  return MonthlyHealthScoreByLocationResponseDto.of(entry.getKey(),
															  avgHealthScore,
															  gpsEntry.getKey(),
															  healthDatas);
													  });
						   })
						   .toList();
	}

	private List<MonthlyHealthScoreByLocationResponseDto.HealthData> toHealthData(
		List<Parameter> parameters,
		List<HealthSummaryData> healthSummaryData
	) {
		return parameters.stream()
						 .map(parameter -> {
							 List<HealthSummaryData> filteredData = healthSummaryData.stream()
																					 .filter(summaryData -> summaryData.getParameterId()
																													   .equals(
																														   parameter.getId()))
																					 .toList();
							 int healthScore = calculateAverageHealthScore(filteredData);
							 return MonthlyHealthScoreByLocationResponseDto.of(healthScore, parameter.getName());
						 })
						 .toList();
	}

	public List<MonthlyHealthDataDto> getMonthlyForecastHealthData(
		List<Long> parameterIds,
		LocalDateTime fromDate,
		LocalDateTime toDate
	) {
		List<Parameter> parameters = parameterRepository.findByIdIn(parameterIds);
		List<HealthSummaryData> healthSummaryDatas = healthSummaryDataRepository.findAllByParameterIdInAndPeriodTypeAndTrxStartAtBetween(
			parameterIds,
			SummaryPeriodType.DAILY,
			fromDate,
			toDate);

		Map<YearMonth, List<HealthSummaryData>> yearMonthMap = groupingYearMonth(healthSummaryDatas);

		Map<Integer, Map<String, Double>> monthlyHealthScoreMap = groupingMonthlyAverageHealthScore(yearMonthMap,
			parameters);

		List<MonthlyHealthDataDto> monthlyHealthScoreList = toMonthlyHealthScoreList(monthlyHealthScoreMap);

		// 임시 예측 데이터(랜덤값) 생성 및 합산
		monthlyHealthScoreList.stream()
							  .map(MonthlyHealthDataDto::getMonth)
							  .max(Integer::compareTo)
							  .ifPresent(maxMonth -> {
								  List<MonthlyHealthDataDto> forcecastData = forecastData(parameterIds, maxMonth);
								  monthlyHealthScoreList.addAll(forcecastData);
							  });
		return monthlyHealthScoreList;

	}

	private List<MonthlyHealthDataDto> toMonthlyHealthScoreList(
		Map<Integer, Map<String, Double>> monthlyHealthData
	) {

		return monthlyHealthData.entrySet()
								.stream()
								.map(entry -> MonthlyHealthDataDto.of(entry.getKey(), entry.getValue()))
								.collect(Collectors.toList());
	}

	private Map<Integer, Map<String, Double>> groupingMonthlyAverageHealthScore(
		Map<YearMonth, List<HealthSummaryData>> yearMonthMap,
		List<Parameter> parameters
	) {
		return yearMonthMap.entrySet()
						   .stream()
						   .collect(Collectors.toMap(entry -> entry.getKey()
																   .getMonthValue(),
							   entry -> parameters.stream()
												  .collect(Collectors.toMap(Parameter::getName,
													  parameter -> (double)calculateAverageHealthScore(entry.getValue(),
														  parameter)))));
	}

	private List<MonthlyHealthDataDto> forecastData(
		List<Long> parameterIds,
		int length
	) {
		List<Parameter> parameters = parameterRepository.findByIdIn(parameterIds);
		List<MonthlyHealthDataDto> result = new ArrayList<>();
		Random random = new Random();
		for (int i = length + 1; i < 13; i++) {
			result.add(MonthlyHealthDataDto.builder()
										   .month(i)
										   .parameters(parameters.stream()
																 .map(parameter -> MonthlyHealthDataDto.Item.builder()
																											.name(
																												parameter.getName())
																											.score(
																												random.nextInt(
																													((90 - 50) + 1)) + 50)
																											.build())
																 .toList())
										   .isForecast(true)
										   .build());
		}
		return result;
	}

	private Map<YearMonth, List<HealthSummaryData>> groupingYearMonth(List<HealthSummaryData> healthSummaryDatas) {

		return healthSummaryDatas.stream()
								 .collect(Collectors.groupingBy(healthSummaryData -> YearMonth.from(healthSummaryData.getTrxStartAt())));
	}

	public List<HeatMapHealthDataResponseDto> getHeatMapHealthData(
		List<Long> parameterIds,
		LocalDateTime fromDate,
		LocalDateTime toDate
	) {
		List<Parameter> parameters = parameterRepository.findByIdIn(parameterIds);
		List<HeatMapHealthDataResponseDto> sumHealthDatas = new ArrayList<>();

		LocalDateTime todayMidnight = LocalDateTime.now()
												   .toLocalDate()
												   .atTime(LocalTime.MIDNIGHT);
		// fromDate가 현재날짜 기준 00시 이전 여부
		boolean beforeMidnight = fromDate.isBefore(todayMidnight);
		boolean afterMidnight = toDate.isAfter(todayMidnight);

		// toDate가 현재날짜 00 시 이후일 경우 parameterData 에서 건강도 계산
		if (afterMidnight) {
			LocalDateTime startDate;
			LocalDateTime endDate = toDate;
			//fromDate가 현재날짜 00시 이후가 아닐경우 fromDate를 현재시간 00시로 변경
			if (!fromDate.isAfter(todayMidnight)) {
				startDate = todayMidnight;
			} else {
				startDate = fromDate;
			}

			List<ParameterData> parameterDatas = parameterDataRepository.findByParameterIdInAndTraceAtBetween(
				parameterIds,
				startDate,
				endDate,
				Sort.by(Sort.Direction.ASC, "traceAt"));

			Map<GPSCoordinateDto, List<ParameterData>> gpsCoordinateMap = groupingGpsCoordinateByParameterData(
				parameterDatas);

			List<HeatMapHealthDataResponseDto> healthScoreList = toLatestHealthScoreList(gpsCoordinateMap, parameters);

			sumHealthDatas.addAll(healthScoreList);
		}
		//fromDate가 현재날짜 00시 이전일경우
		if (beforeMidnight) {
			LocalDateTime startDate = fromDate;
			LocalDateTime endDate = todayMidnight;
			List<HealthSummaryData> healthSummaryDatas = healthSummaryDataRepository.findAllByParameterIdInAndPeriodTypeAndTrxStartAtBetween(
				parameterIds,
				SummaryPeriodType.DAILY,
				startDate,
				endDate);
			Map<GPSCoordinateDto, List<HealthSummaryData>> gpstCoordinateSummaryDataMap = groupingGpsCoordinateBySummaryData(
				healthSummaryDatas);

			List<HeatMapHealthDataResponseDto> healthDatasByHealthSummaryData = toLowestHealthScoreList(
				gpstCoordinateSummaryDataMap,
				parameters);
			sumHealthDatas.addAll(healthDatasByHealthSummaryData);
		}

		return mergeAndFilterLowestHealthScore(sumHealthDatas);

	}

	private List<HeatMapHealthDataResponseDto> mergeAndFilterLowestHealthScore(List<HeatMapHealthDataResponseDto> healthDatas) {
		Map<GPSCoordinateDto, List<HeatMapHealthDataResponseDto>> healthDataGroup = healthDatas.stream()
																							   .collect(Collectors.groupingBy(
																								   healthData -> new GPSCoordinateDto(
																									   healthData.getLatitude(),
																									   healthData.getLongitude())));

		return healthDataGroup.values()
							  .stream()
							  .map(dtos -> dtos.stream()
											   .filter(dataResponseDto -> dataResponseDto.getHealthScore() != 0)
											   .min(Comparator.comparingDouble(HeatMapHealthDataResponseDto::getHealthScore))
											   .orElseThrow(() -> new IllegalArgumentException("최저값을 찾을 수 없습니다")))
							  .toList();
	}

	private Map<GPSCoordinateDto, List<ParameterData>> groupingGpsCoordinateByParameterData(
		List<ParameterData> parameterDatas
	) {

		return parameterDatas.stream()
							 .collect(Collectors.groupingBy(parameterData -> toGpsCoordinate(parameterData.getLatitudeValue(),
								 parameterData.getLongitudeValue())));
	}

	private GPSCoordinateDto toGpsCoordinate(
		double lat,
		double lng
	) {
		// 좌표 소수점 4째자리 계산
		double latPoint = Math.floor(lat * 10000) / 10000;
		double lngPoint = Math.floor(lng * 10000) / 10000;

		return new GPSCoordinateDto(latPoint, lngPoint);
	}

	private List<HeatMapHealthDataResponseDto> toLatestHealthScoreList(
		Map<GPSCoordinateDto, List<ParameterData>> gpsCoordinateParameterDataMap,
		List<Parameter> parameters
	) {

		return gpsCoordinateParameterDataMap.entrySet()
											.stream()
											.map(entry -> {
												List<ParameterData> parameterDatas = entry.getValue();
												List<HeatMapHealthDataResponseDto.HealthData> healthDatas = toLatestHealthDataListByParameterData(
													parameters,
													parameterDatas);
												return HeatMapHealthDataResponseDto.of(entry.getKey(), healthDatas);
											})
											.toList();
	}

	private List<HeatMapHealthDataResponseDto.HealthData> toLatestHealthDataListByParameterData(
		List<Parameter> parameters,
		List<ParameterData> parameterDatas
	) {
		return parameters.stream()
						 .map(parameter -> {
							 ParameterData currentParameterData = parameterDatas.stream()
																				.filter(parameterData -> parameterData.getParameterId()
																													  .equals(
																														  parameter.getId()))
																				.max(Comparator.comparing(ParameterData::getTraceAt))
																				.orElse(null);
							 if (currentParameterData == null) {
								 return null;
							 }

							 return HeatMapHealthDataResponseDto.of(currentParameterData.getNumberValue()
																						.intValue(),
								 parameter.getName());
						 })
						 .filter(Objects::nonNull)
						 .toList();
	}

	private List<HeatMapHealthDataResponseDto> toLowestHealthScoreList(
		Map<GPSCoordinateDto, List<HealthSummaryData>> gpsCoordinateDataMap,
		List<Parameter> parameters
	) {
		return gpsCoordinateDataMap.entrySet()
								   .stream()
								   .map(entry -> {
									   List<HealthSummaryData> summaryDatas = entry.getValue();
									   List<HeatMapHealthDataResponseDto.HealthData> healthData

										   = parameters.stream()
													   .map(parameter -> {
														   int healthScore = calculateAverageHealthScore(summaryDatas,
															   parameter);

														   return HeatMapHealthDataResponseDto.of(healthScore,
															   parameter.getName());
													   })
													   .filter(score -> score.getHealthScore() != 0)
													   .toList();
									   return HeatMapHealthDataResponseDto.of(entry.getKey(), healthData);
								   })
								   .toList();
	}

	private int calculateAverageHealthScore(
		List<HealthSummaryData> summaryDatas,
		Parameter parameter
	) {
		Double sumValue = summaryDatas.stream()
									  .filter(healthSummaryData -> healthSummaryData.getParameterId()
																					.equals(parameter.getId()))
									  .mapToDouble(HealthSummaryData::getSum)
									  .sum();
		Double sumCount = summaryDatas.stream()
									  .filter(healthSummaryData -> healthSummaryData.getParameterId()
																					.equals(parameter.getId()))
									  .mapToDouble(HealthSummaryData::getRealDataCount)
									  .sum();
		return (int)(sumValue / sumCount);
	}

	private int calculateAverageHealthScore(List<HealthSummaryData> summaryDatas) {
		double sumValue = summaryDatas.stream()
									  .mapToDouble(HealthSummaryData::getSum)
									  .sum();
		double sumCount = summaryDatas.stream()
									  .mapToDouble(HealthSummaryData::getRealDataCount)
									  .sum();

		return (int)(sumValue / sumCount);
	}

	private Map<GPSCoordinateDto, List<HealthSummaryData>> groupingGpsCoordinateBySummaryData(
		List<HealthSummaryData> healthSummaryDatas
	) {

		return healthSummaryDatas.stream()
								 .collect(Collectors.groupingBy(healthSummaryData -> {
									 Double latitude = healthSummaryData.getLatitude();
									 Double longitude = healthSummaryData.getLongitude();

									 return toGpsCoordinate(latitude, longitude);
								 }));
	}

}
