package com.nextorm.portal.service.aidas;

import com.nextorm.common.db.repository.ParameterRepository;
import com.nextorm.portal.dto.aidas.ParameterDataAnalysisResponseDto;
import com.nextorm.portal.dto.parameterdata.ParameterDataTrendDto;
import com.nextorm.portal.dto.parameterdata.ParameterRawDataDto;
import com.nextorm.portal.service.ParameterDataService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Transactional
@Service
@RequiredArgsConstructor
public class AIDasService {

	private final ParameterDataService parameterDataService;
	private final ParameterRepository parameterRepository;

	private final Logger logger = LoggerFactory.getLogger(AIDasService.class);

	/**
	 * 파라미터 분석 결과를 계산하고 차트 이미지를 생성하여 리턴한다.
	 *
	 * @param sessionId   Flowise Session ID
	 * @param parameterId 파라미터 ID
	 * @param startDt     조회시작일시
	 * @param endDt       조회종료일시
	 * @return 파라미터 분석결과(Min, Max, Avg) 및 DB에 저장된 차트 이미지 ID
	 */
	public ParameterDataAnalysisResponseDto getParameterDataAnalysis(
		String sessionId,
		Long parameterId,
		LocalDateTime startDt,
		LocalDateTime endDt
	) {
		ParameterDataTrendDto parameterDataTrend = parameterDataService.getParameterDataTrend(parameterId,
			startDt,
			endDt);

		List<ParameterRawDataDto> rawDatas = parameterDataTrend.getRawDatas();

		//Min, Max, 평균을 구함
		DoubleSummaryStatistics statistics = rawDatas.stream()
													 .map(it -> (Double)it.getValue())
													 .collect(Collectors.summarizingDouble(Double::doubleValue));

		Predicate<ParameterRawDataDto> isSpecOut = it -> it.getLsl() != null && it.getUsl() != null && ((Double)it.getValue() < it.getLsl() || (Double)it.getValue() > it.getUsl());

		//Spec out 목록을 구함
		List<ParameterDataAnalysisResponseDto.SpecOut> specOuts = rawDatas.stream()
																		  .filter(isSpecOut)
																		  .map(it -> ParameterDataAnalysisResponseDto.SpecOut.builder()
																															 .usl(
																																 it.getUsl())
																															 .lsl(
																																 it.getLsl())
																															 .value(
																																 (Double)it.getValue())
																															 .dateTime(
																																 it.getTraceAt())
																															 .build())
																		  .toList();

		return ParameterDataAnalysisResponseDto.builder()
											   .parameterId(parameterId)
											   .parameterName(parameterDataTrend.getParameterName())
											   .min(statistics.getMin())
											   .max(statistics.getMax())
											   .average(statistics.getAverage())
											   .specOuts(specOuts)
											   .build();
	}
}
