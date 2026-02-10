package com.nextorm.portal.service;

import com.nextorm.common.db.entity.*;
import com.nextorm.common.db.entity.enums.SummaryPeriodType;
import com.nextorm.common.db.repository.*;
import com.nextorm.portal.common.exception.summarydata.SummaryDataPdfGenerateFailException;
import com.nextorm.portal.dto.parameterdata.ParameterDataReportPdfDto;
import com.nextorm.portal.dto.summary.*;
import com.nextorm.portal.enums.SummaryDataKind;
import com.nextorm.portal.util.PdfGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.nextorm.portal.util.PdfGenerator.HtmlTemplate.PARAMETER_DATA_REPORT;

@Slf4j
@Service
@RequiredArgsConstructor
public class SummaryDataService {
	private final SummaryDataRepository summaryDataRepository;
	private final ParameterRepository parameterRepository;
	private final ParameterDataRepository parameterDataRepository;
	private final UiLanguageRepository uiLanguageRepository;
	private final LocationRepository locationRepository;
	private final PdfGenerator pdfGenerator;

	public SummaryTrendResponseDto getTrendData(
		SummaryTrendRequestDto requestDto
	) {
		List<Parameter> parameters = parameterRepository.findByIdIn(requestDto.getParameterIds());
		final LocalDateTime from = requestDto.getFrom();
		final LocalDateTime to = requestDto.getTo();

		SummaryPeriodType periodType = calculatePeriodType(from, to, requestDto.getChartWidth());
		boolean includeRawData = periodType == null;
		if (periodType == null) {    // raw데이터가 타겟인 경우, 1분 단위 섬머리 데이터를 조회하도록 한다
			periodType = SummaryPeriodType.ONE_MINUTE;
		}

		List<SummaryTrendResponseDto.ParameterTrend> trends = new ArrayList<>();
		for (Parameter parameter : parameters) {
			trends.add(getParameterTrend(from, to, parameter, periodType, requestDto.getDataType(), includeRawData));
		}
		return SummaryTrendResponseDto.from(periodType.getDisplayName(), trends);
	}

	/**
	 * @return 요약하지 않아도 되는 시간대이면 null을 반환한다
	 */
	private SummaryPeriodType calculatePeriodType(
		LocalDateTime from,
		LocalDateTime to,
		Integer chartWidth
	) {
		long diffMinutes = ChronoUnit.MINUTES.between(from, to);
		if (chartWidth < (diffMinutes / SummaryPeriodType.DAILY.getMinutes())) {
			return SummaryPeriodType.DAILY;
		}
		if (chartWidth < (diffMinutes / SummaryPeriodType.SIX_HOURLIES.getMinutes())) {
			return SummaryPeriodType.SIX_HOURLIES;
		}
		if (chartWidth < (diffMinutes / SummaryPeriodType.HOURLY.getMinutes())) {
			return SummaryPeriodType.HOURLY;
		}
		if (chartWidth < (diffMinutes / SummaryPeriodType.TEN_MINUTES.getMinutes())) {
			return SummaryPeriodType.TEN_MINUTES;
		}
		if (chartWidth < (diffMinutes)) {
			return SummaryPeriodType.ONE_MINUTE;
		}
		return null;
	}

	private SummaryTrendResponseDto.ParameterTrend getParameterTrend(
		LocalDateTime from,
		LocalDateTime to,
		Parameter parameter,
		SummaryPeriodType periodType,
		SummaryDataKind dataKind,
		boolean isIncludeRawData
	) {
		List<ParameterData> rawDataList = new ArrayList<>();
		if (isIncludeRawData) {
			rawDataList = parameterDataRepository.findByParameterIdAndTraceAtBetween(parameter.getId(),
				from,
				to,
				Sort.by(Sort.Order.asc("traceAt")));
		}

		List<SummaryData> summaryDataList = summaryDataRepository.findAllByParameterIdAndPeriodTypeAndTrxStartAtBetweenOrderByTrxStartAtAsc(
			parameter.getId(),
			periodType,
			from,
			to);

		return SummaryTrendResponseDto.ParameterTrend.fromSummaryWithRawData(parameter.getName(),
			summaryDataList,
			dataKind,
			rawDataList);
	}

	public FastTrendResponseDto getFastTrendData(
		FastTrendRequestDto requestDto
	) {
		List<Parameter> parameters = parameterRepository.findByIdIn(requestDto.getParameterIds());
		final LocalDateTime from = requestDto.getFrom();
		final LocalDateTime to = requestDto.getTo();

		SummaryPeriodType periodType = calculatePeriodType(from, to, requestDto.getChartWidth());
		boolean includeRawData = periodType == null;
		if (periodType == null) {    // raw데이터가 타겟인 경우, 1분 단위 섬머리 데이터를 조회하도록 한다
			periodType = SummaryPeriodType.ONE_MINUTE;
		}

		List<FastTrendResponseDto.ParameterTrend> trends = new ArrayList<>();
		for (Parameter parameter : parameters) {
			FastTrendResponseDto.ParameterTrend trend = null;
			if (includeRawData) {
				trend = FastTrendResponseDto.ParameterTrend.fromRawData(parameter.getName(),
					parameterDataRepository.findByParameterIdAndTraceAtBetween(parameter.getId(),
						from,
						to,
						Sort.by(Sort.Order.asc("traceAt"))));
			} else {
				trend = FastTrendResponseDto.ParameterTrend.fromSummaryData(parameter.getName(),
					summaryDataRepository.findAllByParameterIdAndPeriodTypeAndTrxStartAtBetweenOrderByTrxStartAtAsc(
						parameter.getId(),
						periodType,
						from,
						to));
			}
			trends.add(trend);
		}
		String dataTypeName = includeRawData
							  ? "RawData"
							  : periodType.getDisplayName();
		return FastTrendResponseDto.from(dataTypeName, trends);
	}

	public List<SummaryMonthlyDataResponseDto> getMonthlySummaryData(
		Long parameterId,
		Long toYear,
		Long fromYear
	) {
		LocalDateTime from = LocalDateTime.of(fromYear.intValue(), 1, 1, 0, 0);
		LocalDateTime to = LocalDateTime.of(toYear.intValue(), 12, 31, 23, 59, 59, 99999);

		List<SummaryData> summaryData = summaryDataRepository.findAllByParameterIdAndPeriodTypeAndTrxStartAtBetween(
			parameterId,
			SummaryPeriodType.DAILY,
			from,
			to);

		// 월별로 그룹화하고 연도별 평균값 계산
		Map<Integer, Map<Integer, Double>> groupedData = summaryData.stream()
																	.collect(Collectors.groupingBy(data -> data.getTrxStartAt()
																											   .getMonthValue(),
																		// 월별 그룹화
																		Collectors.groupingBy(data -> data.getTrxStartAt()
																										  .getYear(),
																			// 연도별 그룹화
																			Collectors.collectingAndThen(Collectors.reducing(
																					new double[2],
																					// [0]: 합계, [1]: 카운트
																					data -> new double[] {
																						data.getAvg() * data.getRealDataCount(),
																						data.getRealDataCount()},
																					(a, b) -> new double[] {a[0] + b[0],
																						a[1] + b[1]}),
																				result -> result[0] / result[1]
																				// 최종 평균 계산
																			))));

		return groupedData.entrySet()
						  .stream()
						  .map(entry -> SummaryMonthlyDataResponseDto.builder()
																	 .month(entry.getKey())
																	 .years(entry.getValue()
																				 .entrySet()
																				 .stream()
																				 .map(yearEntry -> SummaryMonthlyDataResponseDto.MonthlyDataByYear.builder()
																																				  .year(
																																					  yearEntry.getKey())
																																				  .value(
																																					  yearEntry.getValue())
																																				  .build())
																				 .toList())
																	 .build())
						  .toList();
	}

	public List<SummaryDataReportResponseDto> getSummaryReportData(
		List<Long> parameterIds,
		LocalDateTime fromDate,
		LocalDateTime toDate,
		SummaryPeriodType periodType
	) {
		List<SummaryData> summaryDatas = getSummaryDataBetweenSumBaseAtByPeriodType(parameterIds,
			fromDate,
			toDate,
			periodType);
		return toSummaryReportData(parameterIds, summaryDatas);
	}

	private List<SummaryData> getSummaryDataBetweenSumBaseAtByPeriodType(
		List<Long> parameterIds,
		LocalDateTime fromDate,
		LocalDateTime toDate,
		SummaryPeriodType periodType
	) {
		LocalDateTime[] fromTo = convertTime(periodType, fromDate, toDate);
		LocalDateTime from = fromTo[0];
		LocalDateTime to = fromTo[1];

		return summaryDataRepository.findByParameterIdInAndSumStartBaseAtGreaterThanEqualAndSumStartBaseAtLessThanAndPeriodType(
			parameterIds,
			from,
			to,
			periodType);
	}

	private List<SummaryDataReportResponseDto> toSummaryReportData(
		List<Long> parameterIds,
		List<SummaryData> summaryDatas
	) {
		List<Parameter> parameters = parameterRepository.findByIdIn(parameterIds);

		return parameters.stream()
						 .map(parameter -> SummaryDataReportResponseDto.builder()
																	   .parameterId(parameter.getId())
																	   .name(parameter.getName())
																	   .dataType(parameter.getDataType())
																	   .summaryReportDatas(toSummaryReportData(
																		   summaryDatas,
																		   parameter))
																	   .build())
						 .toList();
	}

	private List<SummaryDataReportResponseDto.SummaryReportData> toSummaryReportData(
		List<SummaryData> summaryDatas,
		Parameter parameter
	) {
		return summaryDatas.stream()
						   .filter(summaryData -> summaryData.getParameterId()
															 .equals(parameter.getId()))
						   .map(SummaryDataReportResponseDto::of)
						   .toList();
	}

	public byte[] getParameterReportPdf(
		List<Long> parameterIds,
		LocalDateTime fromDate,
		LocalDateTime toDate,
		SummaryPeriodType periodType,
		String note
	) {
		//임시 한국어 고정
		String language = "ko";

		DateTimeFormatter dateFormat = convertDateFormat(periodType);

		List<Parameter> parameters = parameterRepository.findByIdIn(parameterIds)
														.stream()
														.toList();

		Tool tool = parameters.stream()
							  .map(Parameter::getTool)
							  .findFirst()
							  .orElseThrow(() -> new IllegalArgumentException(""));

		String locationName = locationRepository.findById(tool.getLocation()
															  .getId())
												.orElseThrow(() -> new IllegalArgumentException(""))
												.getName();

		List<SummaryData> summaryDatas = getSummaryDataBetweenSumBaseAtByPeriodType(parameterIds,
			fromDate,
			toDate,
			periodType);

		Map<String, String> languageMap = uiLanguageRepository.findAllByLang(language)
															  .stream()
															  .collect(Collectors.toMap(UiLanguage::getKey,
																  UiLanguage::getMessage));

		List<ParameterDataReportPdfDto.ParameterSummaryData> parameterSummaryDatas = toParameterSummaryData(parameters,
			summaryDatas,
			dateFormat,
			languageMap);

		//임시 한글화
		String translatedPeriodType = translatePeriodType(periodType);

		ParameterDataReportPdfDto parameterDataReportPdfData = ParameterDataReportPdfDto.of(fromDate.format(dateFormat),
			toDate.format(dateFormat),
			locationName,
			tool.getName(),
			note,
			translatedPeriodType,
			parameterSummaryDatas);

		try {
			return pdfGenerator.generatePdfFromHtml(PARAMETER_DATA_REPORT, parameterDataReportPdfData);
		} catch (IOException e) {
			log.error("error", e);
			throw new SummaryDataPdfGenerateFailException(); //따로 처리 필요 pdf 생성 실패
		}

	}

	private List<ParameterDataReportPdfDto.ParameterSummaryData> toParameterSummaryData(
		List<Parameter> parameters,
		List<SummaryData> summaryDatas,
		DateTimeFormatter dateFormat,
		Map<String, String> languageMap
	) {
		return parameters.stream()
						 .map(parameter -> {

							 List<ParameterDataReportPdfDto.StatisticsData> statisticsData = toStatisticsData(parameter,
								 summaryDatas,
								 dateFormat);

							 return ParameterDataReportPdfDto.of(parameter, languageMap, statisticsData);

						 })
						 .toList();
	}

	private List<ParameterDataReportPdfDto.StatisticsData> toStatisticsData(
		Parameter parameter,
		List<SummaryData> summaryDatas,
		DateTimeFormatter dateFormat
	) {
		List<SummaryData> filteredSummaryData = summaryDatas.stream()
															.filter(summaryData -> summaryData.getParameterId()
																							  .equals(parameter.getId()))
															.toList();

		return filteredSummaryData.stream()
								  .map(summaryData -> ParameterDataReportPdfDto.of(summaryData, dateFormat))
								  .toList();
	}

	private String translatePeriodType(
		SummaryPeriodType periodType
	) {
		//임시 하드코딩
		return periodType == SummaryPeriodType.DAILY
			   ? "일자별"
			   : "시간별";

	}

	private DateTimeFormatter convertDateFormat(SummaryPeriodType periodType) {
		final String DATE_FORMAT = "yyyy-MM-dd";
		final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
		return periodType == SummaryPeriodType.DAILY
			   ? DateTimeFormatter.ofPattern(DATE_FORMAT)
			   : DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
	}

	private LocalDateTime[] convertTime(
		SummaryPeriodType periodType,
		LocalDateTime fromDate,
		LocalDateTime toDate
	) {
		LocalDateTime from;
		LocalDateTime to;
		if (periodType == SummaryPeriodType.DAILY) {
			from = fromDate.withHour(0)
						   .withMinute(0)
						   .withSecond(0)
						   .withNano(0);
			to = toDate.withHour(0)
					   .withMinute(0)
					   .withSecond(0)
					   .withNano(0);
		} else {
			from = fromDate.withMinute(0)
						   .withSecond(0)
						   .withNano(0);
			to = toDate.withMinute(0)
					   .withSecond(0)
					   .withNano(0);
		}
		return new LocalDateTime[] {from, to};
	}

}
