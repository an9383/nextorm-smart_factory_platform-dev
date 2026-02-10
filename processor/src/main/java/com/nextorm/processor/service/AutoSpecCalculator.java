package com.nextorm.processor.service;

import com.nextorm.common.db.entity.Parameter;
import com.nextorm.common.db.entity.ParameterData;
import com.nextorm.common.db.repository.ParameterDataRepository;
import com.nextorm.common.db.repository.ParameterRepository;
import com.nextorm.processor.parametercontainer.ParameterContainer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AutoSpecCalculator {
	private static final String PARAMETER_NOT_FOUND_EXCEPTION_MESSAGE_FORMAT = "자동스펙 계산을 수행할 파라미터 정보를 찾지 못함 (id: %s)";
	private static final String AUTO_SPEC_CALCULATE_PERIOD_INVALID_MSG = "자동 계산 기간이 잘못되었습니다.";
	private static final String AUTO_SPEC_CALCULATE_DATA_EMPTY_MSG = "자동 계산 기간에 데이터가 존재하지 않아 스펙 자동 계산을 할 수 없습니다.";

	private static final double THREE_SIGMA = 3d;
	private static final double SIX_SIGMA = 6d;

	private final ParameterRepository parameterRepository;
	private final ParameterDataRepository parameterDataRepository;
	private final ParameterContainer parameterContainer;

	public void calculate(Long parameterId) {
		Parameter parameter = parameterRepository.findById(parameterId)
												 .orElseThrow(() -> new RuntimeException(
													 PARAMETER_NOT_FOUND_EXCEPTION_MESSAGE_FORMAT.formatted(parameterId)));

		if (parameter.isInvalidAutoCalcPeriod()) {
			log.warn(AUTO_SPEC_CALCULATE_PERIOD_INVALID_MSG);
			return;
		}

		long start = System.currentTimeMillis();

		List<Double> values = getValuesBeforeDay(parameterId, parameter.getAutoCalcPeriod());
		if (values.isEmpty()) {
			log.info(AUTO_SPEC_CALCULATE_DATA_EMPTY_MSG);
			return;
		}

		List<Double> filteredValues = filterOutlier(values);

		double mean = calculateMean(filteredValues);
		double stdDev = calculateStdDev(filteredValues, mean);
		double ucl = mean + THREE_SIGMA * stdDev;
		double lcl = mean - THREE_SIGMA * stdDev;
		double usl = mean + SIX_SIGMA * stdDev;
		double lsl = mean - SIX_SIGMA * stdDev;

		parameter.modifySpecsAndAutoCalculateInfo(ucl, lcl, usl, lsl);
		log.info("autoSpecCalculate: {}ms, calculatePeriod: {}",
			System.currentTimeMillis() - start,
			parameter.getAutoCalcPeriod());

		parameterContainer.updateParametersByToolId(parameter.getTool()
															 .getId());
	}

	/**
	 * 현재 날짜로부터 beforeDay 이전까지의 데이터 조회하여 values 반환
	 */
	private List<Double> getValuesBeforeDay(
		Long parameterId,
		Integer beforeDay
	) {
		LocalDateTime to = LocalDateTime.now();
		LocalDateTime from = to.minusDays(beforeDay);
		return parameterDataRepository.findByParameterIdAndTraceAtBetween(parameterId,
										  from,
										  to,
										  Sort.by(Sort.Direction.ASC, "traceAt"))
									  .parallelStream()
									  .map(ParameterData::getDValue)
									  .toList();
	}

	/**
	 * Outlier 값 제외 (이상 치 제거)
	 */
	private List<Double> filterOutlier(
		List<Double> values
	) {
		double mean = calculateMean(values);
		double stdDev = calculateStdDev(values, mean);

		final double ucl = mean + THREE_SIGMA * stdDev;
		final double lcl = mean - THREE_SIGMA * stdDev;
		// final Predicate<Double> isNotOutlier = value -> (value < lcl || ucl < value);
		final Predicate<Double> isNotOutlier = value -> (value >= lcl && value <= ucl);

		List<Double> filteredValues = values.parallelStream()
											.filter(isNotOutlier)
											.toList();

		if (filteredValues.size() < values.size() / 2) {
			/*
			   만약 너무 많은 values가 filtered out되면 오리지널 values를 리턴
			 */
			return values;
		}

		return filteredValues;
	}

	/**
	 * 표준편차 계산
	 */
	private double calculateStdDev(
		List<Double> values,
		final double mean
	) {
		if (values.size() < 2) {
			return 0d;
		}
		double variance = values.parallelStream()
								.mapToDouble(value -> Math.pow(value - mean, 2))
								.average()
								.orElse(0d);

		return Math.sqrt(variance);
	}

	/**
	 * 평균 계산
	 */
	private double calculateMean(List<Double> values) {
		double sum = 0;
		for (Double value : values) {
			sum += value;
		}
		return sum / values.size();
	}
}
