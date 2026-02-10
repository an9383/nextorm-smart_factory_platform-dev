package com.nextorm.portal.service;

import com.nextorm.common.db.entity.Parameter;
import com.nextorm.common.db.entity.ParameterData;
import com.nextorm.common.db.entity.SummaryData;
import com.nextorm.common.db.entity.enums.SummaryPeriodType;
import com.nextorm.common.db.repository.ParameterDataRepository;
import com.nextorm.common.db.repository.ParameterRepository;
import com.nextorm.common.db.repository.SummaryDataRepository;
import com.nextorm.common.db.repository.ToolRepository;
import com.nextorm.common.db.repository.dto.ParameterRawDataStatistics;
import com.nextorm.portal.common.exception.parameter.LotEventParameterNotFoundException;
import com.nextorm.portal.common.exception.parameter.NotSupportParameterTypeException;
import com.nextorm.portal.common.exception.parameter.ParameterNotFoundException;
import com.nextorm.portal.dto.parameterdata.*;
import com.nextorm.portal.enums.Operator;
import com.nextorm.portal.enums.TimeCriteria;
import lombok.RequiredArgsConstructor;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParameterDataService {
	private final ParameterDataRepository parameterDataRepository;
	private final ParameterRepository parameterRepository;
	private final SummaryDataRepository summaryDataRepository;
	private final ToolRepository toolRepository;

	/**
	 * 단일 파라미터 raw 데이터 트렌드를 조회한다.
	 *
	 * @param parameterId 파라미터 ID
	 * @param from        조회시작일시
	 * @param to          조회종료일시
	 * @return 파라미터 raw 데이터 트렌드
	 */
	public ParameterDataTrendDto getParameterDataTrend(
		Long parameterId,
		LocalDateTime from,
		LocalDateTime to
	) {
		to = adjustToMinuteEnd(to);
		List<ParameterDataTrendDto> result = this.getParameterDataTrend(Arrays.asList(parameterId), from, to);
		if (result.isEmpty()) {
			return null;
		}

		return result.get(0);
	}

	/**
	 * 파라미터 raw 데이터 트렌드를 조회한다.
	 *
	 * @param parameterIds 파라미터 ID
	 * @param from         조회시작일시
	 * @param to           조회종료일시
	 * @return 파라미터 raw 데이터 트렌드
	 */
	public List<ParameterDataTrendDto> getParameterDataTrend(
		List<Long> parameterIds,
		LocalDateTime from,
		LocalDateTime to
	) {
		List<Parameter> parameters = parameterRepository.findByIdIn(parameterIds);
		List<ParameterData> parameterDatas = parameterDataRepository.findByParameterIdInAndTraceAtBetween(parameterIds,
			from,
			to,
			Sort.by(Sort.Direction.ASC, "traceAt"));

		return parameters.stream()
						 .map(param -> {
							 ParameterDataTrendDto paramDataTrend = new ParameterDataTrendDto();
							 paramDataTrend.setParameterId(param.getId());
							 paramDataTrend.setParameterName(param.getName());
							 paramDataTrend.setParameterType(param.getType());
							 paramDataTrend.setParameterDataType(param.getDataType());
							 paramDataTrend.setToolId(param.getTool()
														   .getId());
							 paramDataTrend.setToolName(param.getTool()
															 .getName());

							 List<ParameterRawDataDto> rawDatas = parameterDatas.stream()
																				.filter(data -> param.getId()
																									 .equals(data.getParameterId()))
																				.map(data -> {
																					ParameterRawDataDto paramData = new ParameterRawDataDto();
																					paramData.setParameterId(data.getParameterId());
																					paramData.setLsl(data.getLsl());
																					paramData.setLcl(data.getLcl());
																					paramData.setUsl(data.getUsl());
																					paramData.setUcl(data.getUcl());
																					paramData.setValue(data.getValue(
																						param.getDataType()));
																					paramData.setTraceAt(data.getTraceAt());
																					return paramData;
																				})
																				.collect(Collectors.toList());

							 paramDataTrend.setRawDatas(rawDatas);

							 return paramDataTrend;
						 })
						 .collect(Collectors.toList());
	}

	/**
	 * 파라미터 raw 데이터의 정규분포 차트 데이터를 구한다.
	 *
	 * @param parameterIds 파라미터 ID
	 * @param from         조회시작일시
	 * @param to           조회종료일시
	 * @return 정규분포 차트 데이터
	 */
	public List<ParameterDataDistributionDto> getParameterDataNormalDistribution(
		List<Long> parameterIds,
		LocalDateTime from,
		LocalDateTime to
	) {
		to = adjustToMinuteEnd(to);
		List<Parameter> parameters = parameterRepository.findByIdIn(parameterIds);
		List<ParameterData> parameterDatas = parameterDataRepository.findByParameterIdInAndTraceAtBetween(parameterIds,
			from,
			to,
			Sort.by(Sort.Direction.ASC, "traceAt"));
		return parameters.stream()
						 .filter(param -> param.isNumberType())
						 .map(param -> {
							 ParameterDataDistributionDto dataDistribution = new ParameterDataDistributionDto();
							 dataDistribution.setParameterId(param.getId());
							 dataDistribution.setParameterName(param.getName());
							 dataDistribution.setParameterType(param.getType());
							 dataDistribution.setParameterDataType(param.getDataType());
							 dataDistribution.setToolId(param.getTool()
															 .getId());
							 dataDistribution.setToolName(param.getTool()
															   .getName());

							 double[] rawDatas = parameterDatas.stream()
															   .filter(data -> param.getId()
																					.equals(data.getParameterId()))
															   .mapToDouble(data -> {
																   Object value = data.getValue(param.getDataType());
																   return ((Number)value).doubleValue();
															   })
															   .sorted()
															   .toArray();

							 if (rawDatas.length > 1) {
								 DescriptiveStatistics stats = new DescriptiveStatistics(rawDatas);
								 double mean = stats.getMean();
								 double stdDev = stats.getStandardDeviation();

								 if (stdDev != 0) {
									 NormalDistribution normalDistribution = new NormalDistribution(mean, stdDev);
									 List<ParameterDataDistributionDto.Distribution> distributions = Arrays.stream(
																											   rawDatas)
																										   .mapToObj(
																											   value -> new ParameterDataDistributionDto.Distribution(
																												   value,
																												   normalDistribution.density(
																													   value)))
																										   .collect(
																											   Collectors.toList());

									 dataDistribution.setDistributions(distributions);
								 }
							 }
							 return dataDistribution;
						 })
						 .collect(Collectors.toList());
	}

	/**
	 * 파라미터 데이터의 Spec out
	 *
	 * @param parameterIds
	 * @param from
	 * @param to
	 * @return
	 */
	public List<ParameterDataSpecOutCountDto> getParameterDataSpecOutCount(
		List<Long> parameterIds,
		LocalDateTime from,
		LocalDateTime to
	) {
		to = adjustToMinuteEnd(to);
		List<Parameter> parameters = parameterRepository.findByIdIn(parameterIds);
		List<ParameterData> parameterDatas = parameterDataRepository.findByParameterIdInAndTraceAtBetween(parameterIds,
			from,
			to,
			Sort.by(Sort.Direction.ASC, "traceAt"));
		return parameters.stream()
						 .filter(param -> param.isNumberType())
						 .map(param -> {
							 ParameterDataSpecOutCountDto specOutCount = new ParameterDataSpecOutCountDto();
							 specOutCount.setParameterId(param.getId());
							 specOutCount.setParameterName(param.getName());
							 specOutCount.setParameterType(param.getType());
							 specOutCount.setParameterDataType(param.getDataType());
							 specOutCount.setToolId(param.getTool()
														 .getId());
							 specOutCount.setToolName(param.getTool()
														   .getName());

							 long specOutCnt = parameterDatas.stream()
															 .filter(data -> param.getId()
																				  .equals(data.getParameterId()))
															 .filter(data -> {
																 double value = ((Number)data.getValue(param.getDataType())).doubleValue();
																 if (data.getLsl() != null && data.getUsl() != null) {
																	 return value < data.getLsl() || value > data.getUsl();
																 } else if (data.getLsl() != null) {
																	 return value < data.getLcl();
																 } else if (data.getUsl() != null) {
																	 return value > data.getUsl();
																 }
																 return false;
															 })
															 .count();

							 specOutCount.setSpecOutCnt(specOutCnt);
							 return specOutCount;
						 })
						 .collect(Collectors.toList());
	}

	/**
	 * 파라미터 데이터의 통계값을 구한다.
	 *
	 * @param parameterIds 파라미터 ID
	 * @param from         조회시작일시
	 * @param to           조회종료일시
	 * @return 파라미터 데이터  통계값(평균, 최소, 최대)
	 */
	public List<ParameterRawDataStatistics> getRawDataStatistics(
		List<Long> parameterIds,
		LocalDateTime from,
		LocalDateTime to
	) {
		to = adjustToMinuteEnd(to);
		return parameterDataRepository.getParameterDataStatistics(parameterIds, from, to);
	}

	public ReplyDataDto getParameterDataPivot(
		Long parameterId,
		LocalDateTime from,
		LocalDateTime to
	) {
		to = adjustToMinuteEnd(to);
		final String latitude = "latitude";
		final String longitude = "longitude";
		Parameter parameterInfo = parameterRepository.findById(parameterId)
													 .get();
		List<String> parameterNames = new ArrayList<>();
		parameterNames.add(parameterInfo.getName());
		parameterNames.add(latitude);
		parameterNames.add(longitude);

		Long toolId = parameterInfo.getTool()
								   .getId();

		List<ParameterData> parameterDatas = parameterDataRepository.findByParameterIdAndTraceAtBetween(parameterId,
			from,
			to,
			null);

		Map<LocalDateTime, Map<String, Object>> keysData = new HashMap<>();

		for (int i = 0; i < parameterDatas.size(); i++) {
			ParameterData parameterData = parameterDatas.get(i);
			LocalDateTime date = parameterData.getTraceAt();
			Long paramId = parameterData.getParameterId();
			Map<String, Object> data = new HashMap<>();
			if (keysData.containsKey(date)) {
				data = keysData.get(date);
			}
			Double latitudeValue = parameterData.getLatitudeValue();
			Double longitudeValue = parameterData.getLongitudeValue();

			if (latitudeValue != null && longitudeValue != null) {
				data.put(parameterInfo.getName(),
					Double.valueOf(parameterData.getValue(parameterInfo.getDataType())
												.toString()));
				data.put(latitude, Double.valueOf(parameterData.getLatitudeValue()));
				data.put(longitude, Double.valueOf(parameterData.getLongitudeValue()));
				keysData.put(date, data);
			}
		}

		//traceAt,1,2,3,4,5
		ReplyDataDto replyDataDto = new ReplyDataDto();

		List<String> headers = new ArrayList<>();
		headers.add("date");
		for (String paramName : parameterNames) {
			headers.add(paramName);
		}

		replyDataDto.setHeaders(headers);

		List<LocalDateTime> keyList = new ArrayList<>(keysData.keySet());
		Collections.sort(keyList);

		List<List<Object>> resultDatas = new ArrayList<>();
		for (int i = 0; i < keyList.size(); i++) {
			Map<String, Object> data = keysData.get(keyList.get(i));
			List<Object> datas = new ArrayList<>();
			datas.add(keyList.get(i)); //date
			for (int paramIndex = 0; paramIndex < parameterNames.size(); paramIndex++) {
				String parameterName = parameterNames.get(paramIndex);
				if (data.containsKey(parameterName)) {
					datas.add(data.get(parameterName));
				} else {
					datas.add(null);
				}
			}
			resultDatas.add(datas);
		}
		replyDataDto.setDatas(resultDatas);

		return replyDataDto;
	}

	public ParameterStatisticsResponseDto getStatisticsByParameterId(
		Long parameterId,
		Operator operator,
		TimeCriteria timeCriteria,
		LocalDateTime fromDate,
		LocalDateTime toDate
	) {
		Parameter parameter = parameterRepository.findById(parameterId)
												 .orElseThrow(() -> new IllegalArgumentException("찾을 수 없는 파라미터: " + parameterId));

		DateRange dateRange = createStatisticsDateRange(timeCriteria, fromDate, toDate);
		Function<SummaryData, String> groupClassifier = createGroupClassifier(timeCriteria);

		List<SummaryData> summaryDataList = summaryDataRepository.findAllByParameterIdAndPeriodTypeAndTrxStartAtBetween(
			parameterId,
			SummaryPeriodType.DAILY,
			dateRange.start(),
			dateRange.end());

		List<ParameterStatisticsResponseDto.ChartItem> chartItems;
		chartItems = summaryDataList.stream()
									.collect(Collectors.groupingBy(groupClassifier))
									.entrySet()
									.stream()
									.map(entry -> ParameterStatisticsResponseDto.ChartItem.from(entry.getKey(),
										operator.calculate(entry.getValue())))
									.sorted(Comparator.comparing(ParameterStatisticsResponseDto.ChartItem::getDate))
									.toList();

		return ParameterStatisticsResponseDto.of(parameter.getName(), chartItems);
	}

	private DateRange createStatisticsDateRange(
		TimeCriteria timeCriteria,
		LocalDateTime fromDate,
		LocalDateTime toDate
	) {
		return switch (timeCriteria) {
			case DAY -> {
				LocalDateTime end = null;
				LocalDateTime start = null;
				if (fromDate != null && toDate != null) {
					start = fromDate;
					end = toDate;
				} else {
					end = LocalDateTime.now();
					start = LocalDateTime.of(end.minusDays(14L)
												.toLocalDate(), LocalTime.MIN);
				}
				yield new DateRange(start, end);
			}
			case WEEK -> {
				LocalDateTime end = null;
				LocalDateTime mondayOfStartWeek = null;
				if (fromDate != null && toDate != null) {
					mondayOfStartWeek = fromDate;
					end = toDate;
				} else {
					end = LocalDateTime.now();
					mondayOfStartWeek = end.minusWeeks(14L);
				}

				mondayOfStartWeek = mondayOfStartWeek.minusDays(end.toLocalDate()
																   .getDayOfWeek()
																   .getValue() - 1L);
				yield new DateRange(mondayOfStartWeek, end);
			}
			case MONTH -> {
				LocalDateTime end = null;
				LocalDateTime start = null;
				if (fromDate != null && toDate != null) {
					start = fromDate;
					end = toDate;
				} else {
					end = LocalDateTime.now();
					start = end.minusMonths(12);
				}

				start = LocalDateTime.of(start.withDayOfMonth(1)
											  .toLocalDate(), LocalTime.MIN);
				yield new DateRange(start, end);
			}
		};
	}

	private Function<SummaryData, String> createGroupClassifier(TimeCriteria timeCriteria) {
		return switch (timeCriteria) {
			case DAY -> {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				yield summary -> formatter.format(summary.getTrxStartAt()
														 .toLocalDate());
			}

			case WEEK -> summary -> {
				LocalDate localDate = summary.getTrxStartAt()
											 .toLocalDate();
				return localDate.minusDays(localDate.getDayOfWeek()
													.getValue() - 1L)
								.toString();
			};

			case MONTH -> summary -> {
				LocalDate localDate = summary.getTrxStartAt()
											 .toLocalDate();
				return String.format("%s-%02d", localDate.getYear(), localDate.getMonthValue());
			};

		};
	}

	public List<RecentParameterDataResponseDto> getRecentParameterData(
		Long parameterId,
		Long lastParameterDataId,
		Integer limit
	) {
		Parameter parameter = parameterRepository.findById(parameterId)
												 .orElseThrow(() -> new IllegalArgumentException("찾을 수 없는 파라미터: " + parameterId));

		List<ParameterData> parameterDataList = List.of();
		if (lastParameterDataId != null) {
			parameterDataList = parameterDataRepository.findLimitRecentByIdGreaterThanAndParameterIdOrderByTraceAtDesc(
				lastParameterDataId,
				parameterId,
				limit);
		} else {
			parameterDataList = parameterDataRepository.findLimitRecentByParameterIdOrderByTraceAtDesc(parameterId,
				limit);
		}

		return parameterDataList.stream()
								.map(it -> RecentParameterDataResponseDto.of(it.getId(),
									parameter.getName(),
									it.getValue(parameter.getDataType()),
									it.getTraceAt()))
								.toList();
	}

	public List<UnderWaterTerrainDto> getUnderWaterTerrainData(
		Long toolId,
		LocalDateTime from,
		LocalDateTime to
	) {
		to = adjustToMinuteEnd(to);
		final String PARAM_NAME_DEPTH_DATA = "depth_data";

		//toolId로 필수 파라미터 get
		List<Parameter> parametersInfos = parameterRepository.findByToolId(toolId);
		Map<String, Long> paramNameIdMap = parametersInfos.stream()
														  .filter(parameter -> PARAM_NAME_DEPTH_DATA.equals(parameter.getName()))
														  .collect(Collectors.toMap(Parameter::getName,
															  Parameter::getId));

		//해당 파라미터의 기간으로 데이터 get
		List<ParameterData> parameterDatas = parameterDataRepository.findByParameterIdInAndTraceAtBetween(List.copyOf(
																												  paramNameIdMap.values())
																											  .stream()
																											  .sorted(
																												  Collections.reverseOrder())
																											  .toList(),
			from,
			to,
			null);

		//데이터를 시간으로 grouping
		Map<LocalDateTime, List<ParameterData>> timeGroupingMap = parameterDatas.stream()
																				.collect(Collectors.groupingBy(
																					ParameterData::getTraceAt));

		return timeGroupingMap.keySet()
							  .stream()
							  .filter(traceAt -> timeGroupingMap.get(traceAt)
																.size() != 3)
							  .map(traceAt -> {
								  List<ParameterData> timeParamDatas = timeGroupingMap.get(traceAt);
								  ParameterData depthData = timeParamDatas.stream()
																		  .filter(paramData -> paramNameIdMap.get(
																												 PARAM_NAME_DEPTH_DATA)
																											 .equals(
																												 paramData.getParameterId()))
																		  .findFirst()
																		  .orElse(null);

								  // NullPointerException을 방지 - 기본값 설정
								  double latitudeValue = depthData != null
														 ? depthData.getLatitudeValue()
														 : 0.0;
								  double longitudeValue = depthData != null
														  ? depthData.getLongitudeValue()
														  : 0.0;
								  double depthDataValue = depthData != null
														  ? depthData.getNumberValue()
																	 .doubleValue()
														  : 0.0;

								  return UnderWaterTerrainDto.builder()
															 .traceAt(traceAt)
															 .latitude(latitudeValue)
															 .longitude(longitudeValue)
															 .depthData(depthDataValue)
															 .build();

							  })
							  .toList();

	}

	public ParameterDataDto getImageParameterDataByToolAndTraceAt(
		Long toolId,
		LocalDateTime dateTime
	) {

		ParameterData parameterData = parameterDataRepository.findImageByToolIdAndTraceAt(toolId, dateTime);

		if (parameterData == null) {
			return null;
		} else {
			return ParameterDataDto.from(parameterData);
		}
	}

	public List<TraceAtGroupedParameterDataDto> getParameterDataGroupedByTraceAt(
		List<Long> parameterIds,
		LocalDateTime from,
		LocalDateTime to
	) {
		to = adjustToMinuteEnd(to);
		List<ParameterData> parameterDatas = parameterDataRepository.findByParameterIdInAndTraceAtBetween(parameterIds,
			from,
			to,
			Sort.by(Sort.Direction.ASC, "traceAt"));

		Map<LocalDateTime, List<ParameterData>> groupedParameterDataMap = parameterDatas.stream()
																						.collect(Collectors.groupingBy(
																							it -> it.getTraceAt()
																									.withNano(0)));

		return groupedParameterDataMap.entrySet()
									  .stream()
									  .sorted(Map.Entry.comparingByKey())
									  .map(it -> TraceAtGroupedParameterDataDto.builder()
																			   .traceAt(it.getKey())
																			   .parameterDatas(it.getValue()
																								 .stream()
																								 .map(ParameterDataDto::from)
																								 .toList())
																			   .build())
									  .toList();
	}

	/**
	 * String 타입의 파라미터의 데이터를 중복 제거하여 조회
	 */
	public List<String> getDistinctParameterDataByStringTypeParameter(
		Long parameterId,
		LocalDateTime from,
		LocalDateTime to
	) {
		to = adjustToMinuteEnd(to);
		Parameter parameter = parameterRepository.findById(parameterId)
												 .map(it -> {
													 if (it.isNumberType()) {
														 throw new NotSupportParameterTypeException();
													 }
													 return it;
												 })
												 .orElseThrow(ParameterNotFoundException::new);

		return parameterDataRepository.findDistinctStringValueByParameterIdAndTraceAtBetween(parameter.getId(),
			from,
			to);
	}

	public List<RecipeTrendDto> getRecipeTrend(RecipeTrendRequestDto requestDto) {
		List<RecipeWorkTime> recipeWorks = getTargetRecipeRecipeWorks(requestDto.getToolId(),
			requestDto.getFrom(),
			adjustToMinuteEnd(requestDto.getTo()),
			requestDto.getRecipeParameterId(),
			requestDto.getRecipeName());

		List<RecipeTrendDto> results = new ArrayList<>();
		for (RecipeWorkTime recipeWork : recipeWorks) {

			List<ParameterData> parameterDatas = parameterDataRepository.findByParameterIdAndTraceAtBetween(requestDto.getParameterId(),
				recipeWork.start,
				recipeWork.end,
				Sort.by(Sort.Direction.ASC, "traceAt"));

			// 첫번째 데이터의 시간을 기준으로 레시피 데이터가 생성된 시간을 계산한다
			final LocalDateTime firstTime = parameterDatas.get(0)
														  .getTraceAt();

			List<RecipeTrendDto.Item> items = parameterDatas.stream()
															.map(it -> {
																LocalTime currentTime = it.getTraceAt()
																						  .toLocalTime();

																LocalTime time = currentTime.minusHours(firstTime.getHour())
																							.minusMinutes(firstTime.getMinute())
																							.minusSeconds(firstTime.getSecond())
																							.minusNanos(firstTime.getNano());

																return RecipeTrendDto.Item.of(time,
																	it.getNumberValue());
															})
															.toList();

			results.add(RecipeTrendDto.of(recipeWork.start, recipeWork.end, items));
		}

		return results;
	}

	/**
	 * 툴의 레시피 작업 시작/종료 시간 목록을 조회한다
	 */
	private List<RecipeWorkTime> getTargetRecipeRecipeWorks(
		Long toolId,
		LocalDateTime from,
		LocalDateTime to,
		Long recipeParameterId,
		String recipeName
	) {
		// TODO:: LOT EVENT 파라미터에 대한 정책이 정해지면 관리 방식을 그에 맞게 변경할 필요가 있다
		final String lotEventParameterName = "LOT_EVENT";
		Parameter lotEventParameter = parameterRepository.findByToolIdAndName(toolId, lotEventParameterName)
														 .orElseThrow(LotEventParameterNotFoundException::new);

		List<ParameterData> lotEventParameterDataList = parameterDataRepository.findByParameterIdAndTraceAtBetween(
			lotEventParameter.getId(),
			from,
			to,
			Sort.by(Sort.Direction.ASC, "traceAt"));

		Predicate<RecipeWorkTime> isTargetRecipeWork = recipeWork -> {
			return parameterDataRepository.findByParameterIdAndTraceAtAndSValue(recipeParameterId,
											  recipeWork.start,
											  recipeName)
										  .isPresent();
		};

		return parseRecipeWorks(lotEventParameterDataList).stream()
														  .filter(isTargetRecipeWork)
														  .toList();
	}

	// 레시피 시작 - 끝 시간을 파싱한다
	private List<RecipeWorkTime> parseRecipeWorks(List<ParameterData> recipeParameterDatas) {
		final String workStartValue = "START";
		final String workEndValue = "END";

		List<RecipeWorkTime> recipeWorks = new ArrayList<>();
		LocalDateTime start = null;
		for (ParameterData data : recipeParameterDatas) {
			String value = data.getSValue();
			LocalDateTime traceAt = data.getTraceAt();

			if (workStartValue.equals(value)) {
				start = traceAt;
			} else if (workEndValue.equals(value) && start != null) {
				recipeWorks.add(new RecipeWorkTime(start, traceAt));
				start = null;
			}
		}
		return recipeWorks;
	}

	public List<RecipeDtwTrendDto> getRecipeDtwTrend(RecipeTrendRequestDto requestDto) {
		List<RecipeWorkTime> recipeWorks = getTargetRecipeRecipeWorks(requestDto.getToolId(),
			requestDto.getFrom(),
			adjustToMinuteEnd(requestDto.getTo()),
			requestDto.getRecipeParameterId(),
			requestDto.getRecipeName());

		Function<RecipeWorkTime, List<ParameterData>> findParameterDataByWorkTime = recipeWork -> {
			return parameterDataRepository.findByParameterIdAndTraceAtBetween(requestDto.getParameterId(),
				recipeWork.start,
				recipeWork.end,
				Sort.by(Sort.Direction.ASC, "traceAt"));
		};

		List<RecipeWorkTimeWithDataList> timeWithDataLists = recipeWorks.stream()
																		.map(it -> new RecipeWorkTimeWithDataList(it,
																			findParameterDataByWorkTime.apply(it)))
																		.toList();

		int dataLengthMedian = calculateDataLengthMedian(timeWithDataLists);
		double marginRate = 0.1;
		int marginLength = (int)(dataLengthMedian * marginRate);
		int targetLengthMin = dataLengthMedian - marginLength;
		int targetLengthMax = dataLengthMedian + marginLength;

		IntPredicate isNotTargetLength = length -> length < targetLengthMin || length > targetLengthMax;

		RecipeWorkTimeWithDataList baseWork = null;
		double[] baseValues = null;

		List<RecipeDtwTrendDto> results = new ArrayList<>();
		for (RecipeWorkTimeWithDataList work : timeWithDataLists) {
			int currentLength = work.parameterDataList()
									.size();
			if (isNotTargetLength.test(currentLength)) {
				continue;
			}

			if (baseWork == null) {
				baseWork = work;
				baseValues = work.parameterDataList()
								 .stream()
								 .mapToDouble(it -> it.getNumberValue()
													  .doubleValue())
								 .toArray();

				results.add(RecipeDtwTrendDto.of(work.start, work.end, toDoubleList(baseValues)));
				continue;
			}

			// DTW 계산 후 warping data 출력
			DTW dtw = new DTW(baseValues,
				work.parameterDataList()
					.stream()
					.mapToDouble(it -> it.getNumberValue()
										 .doubleValue())
					.toArray());
			results.add(RecipeDtwTrendDto.of(work.start, work.end, toDoubleList(dtw.getWarpingData())));
		}

		return results;
	}

	private List<Double> toDoubleList(double[] values) {
		return Arrays.stream(values)
					 .boxed()
					 .toList();
	}

	private int calculateDataLengthMedian(List<RecipeWorkTimeWithDataList> timeWithDataLists) {
		return timeWithDataLists.stream()
								.mapToInt(recipe -> recipe.parameterDataList()
														  .size())
								.sorted()
								.skip((timeWithDataLists.size() - 1) / 2)
								.findFirst()
								.orElse(0);
	}

	public LocalDateTime adjustToMinuteEnd(LocalDateTime dateTime) {
		return dateTime.withSecond(59)
					   .withNano(999_999_999);
	}

	public Page<ParameterRawDataDto> getParameterDatasByPageable(
		Long parameterId,
		LocalDateTime from,
		LocalDateTime to,
		Pageable pageable
	) {

		to = adjustToMinuteEnd(to);

		Page<ParameterData> parameterDatas = parameterDataRepository.findAllByParameterIdAndTraceAtBetween(parameterId,
			from,
			to,
			pageable);

		return parameterDatas.map(data -> {
			ParameterRawDataDto paramData = new ParameterRawDataDto();
			paramData.setTraceAt(data.getTraceAt());
			paramData.setValue(data.getValue());
			paramData.setUcl(data.getUcl());
			paramData.setLcl(data.getLcl());
			paramData.setUsl(data.getUsl());
			paramData.setLsl(data.getLsl());
			return paramData;
		});
	}

	public List<ParameterDataWithinPeriodDto> getLatestParameterDataWithinPeriod(
		List<Long> parameterIds,
		Integer period
	) {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime from = now.minusSeconds(period);

		//파라미터 이름 매핑
		Map<Long, Parameter> parameterMap = parameterRepository.findAllByIdInWithTool(parameterIds)
															   .stream()
															   .collect(Collectors.toMap(Parameter::getId,
																   Function.identity()));

		// 결과 매핑 순서 보장 + null 대응
		return parameterIds.stream()
						   .map(parameterId -> {
							   Parameter parameter = parameterMap.get(parameterId);
							   if (parameter == null) {
								   return null; // 파라미터 정보 자체 없음
							   }

							   return parameterDataRepository.findFirstByParameterIdAndTraceAtAfterOrderByTraceAtDesc(
																 parameterId,
																 from)
															 .map(data -> ParameterDataWithinPeriodDto.of(data.getId(),
																 data.getParameterId(),
																 parameter.getName(),
																 parameter.getTool()
																		  .getName(),
																 data.getDataType(),
																 data.getValue(),
																 parameter.getUnit(),
																 data.isSpecLimitOver(),
																 data.getTraceAt()))
															 // 파라미터는 있지만 데이터가 없는 경우
															 .orElseGet(() -> ParameterDataWithinPeriodDto.of(null,
																 parameter.getId(),
																 parameter.getName(),
																 parameter.getTool()
																		  .getName(),
																 parameter.getDataType(),
																 null,
																 parameter.getUnit(),
																 null,
																 null));
						   })
						   .toList();

	}

	record RecipeWorkTime(LocalDateTime start, LocalDateTime end) {

	}

	record RecipeWorkTimeWithDataList(LocalDateTime start, LocalDateTime end, List<ParameterData> parameterDataList) {
		RecipeWorkTimeWithDataList(
			RecipeWorkTime recipeWorkTime,
			List<ParameterData> parameterDataList
		) {
			this(recipeWorkTime.start, recipeWorkTime.end, parameterDataList);
		}
	}

	record DateRange(LocalDateTime start, LocalDateTime end) {

	}
}
