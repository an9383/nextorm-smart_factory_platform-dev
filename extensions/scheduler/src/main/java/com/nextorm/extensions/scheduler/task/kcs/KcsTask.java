package com.nextorm.extensions.scheduler.task.kcs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextorm.extensions.scheduler.task.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
public class KcsTask implements Task {
	private final SfpRepository sfpRepository;
	private final EqmsRepository eqmsRepository;
	private final ObjectMapper objectMapper;

	private final String productIdParameterName = "product_id";
	private final String incrementCycleCountFlagParmaeterName = "increment_cycle_count_flag";

	@Override
	public void execute() {
		TimeRange timeRange = calculateTimeRange();
		process(timeRange);
	}

	private void process(TimeRange timeRange) {
		long startTime = System.currentTimeMillis();
		log.info("KCS task 실행 기간: {} - {}", timeRange.from(), timeRange.to());

		// 설비 ID로 파라미터를 그룹화 (제품ID, 증가플래그)
		Map<Long, List<SfpParameterDto>> parametersGroupedByToolId = groupParametersByToolId();

		// 그룹별 처리 및 결과 수집
		Map<Long, Map<Long, Integer>> allResults = parametersGroupedByToolId.entrySet()
																			.stream()
																			.collect(Collectors.toMap(Map.Entry::getKey,
																				entry -> processToolGroup(entry.getKey(),
																					entry.getValue(),
																					timeRange)));

		// 모든 tool의 결과를 병합하여 상품ID별 카운트 합산
		// KEY: productId, VALUE: totalCount
		Map<Long, Integer> productIdTotalCounts = allResults.values()
															.stream()
															.flatMap(map -> map.entrySet()
																			   .stream())
															.collect(Collectors.groupingBy(Map.Entry::getKey,
																Collectors.summingInt(Map.Entry::getValue)));

		// 최종 결과 출력
		log.info("=== KCS Task 최종 결과 (상품ID별 집계) ===");
		productIdTotalCounts.forEach((productId, count) -> log.info("상품ID: {}, 총 개수: {}", productId, count));
		log.info("=== 처리 완료: 총 {} 개 상품 ===", productIdTotalCounts.size());

		eqmsRepository.findMoldProducts()
					  .forEach(product -> {
						  Integer productionCount = productIdTotalCounts.get(product.id());
						  if (productionCount == null) {
							  return;
						  }
						  addCycleCount(product, productionCount);
					  });
		long endTime = System.currentTimeMillis();
		log.info("KCS task 실행시간: {} ms", (endTime - startTime));
	}

	/**
	 * 설비 제품에 사이클 카운트를 추가한다
	 */
	private void addCycleCount(
		EqmsProductDto product,
		Integer productionCount
	) {
		String cycleCountKey = "cycleCount";
		String updateExtJsonString;
		try {
			Map map = objectMapper.readValue(product.extJsonString(), Map.class);
			map.put(cycleCountKey,
				(Integer.parseInt(map.getOrDefault(cycleCountKey, "0")
									 .toString()) + productionCount) + "");
			updateExtJsonString = objectMapper.writeValueAsString(map);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		eqmsRepository.updateProductExt(product.id(), updateExtJsonString);
	}

	/**
	 * 지정된 시간 범위에 대해 집계를 실행하기 위한 메서드
	 * from - to의 간격은 최대 24시간
	 *
	 * @param from 시작 시간
	 * @param to   종료 시간
	 */
	public void execute(
		LocalDateTime from,
		LocalDateTime to
	) {
		long hours = Duration.between(from, to)
							 .toHours();

		if (hours > 24) {
			throw new IllegalArgumentException("시간 범위는 최대 24시간 이내여야 합니다.");
		}

		TimeRange timeRange = new TimeRange(from, to);
		process(timeRange);
	}

	/**
	 * product_id 및 increment_cycle_count_flag 파라미터를 toolId별로 그룹화하여 반환
	 * KEY: toolId, VALUE: List<SfpParameterDto>
	 */
	public Map<Long, List<SfpParameterDto>> groupParametersByToolId() {
		// product_id 파라미터 조회
		List<SfpParameterDto> productIdParameters = sfpRepository.findAllParameters(productIdParameterName);

		// increment_cycle_count_flag 파라미터 조회
		List<SfpParameterDto> incrementFlagParameters = sfpRepository.findAllParameters(
			incrementCycleCountFlagParmaeterName);

		// 두 위의 두 파라미터 목록 하나의 리스트로 병합 후 toolId별 그룹화
		return Stream.of(productIdParameters, incrementFlagParameters)
					 .flatMap(List::stream)
					 .collect(Collectors.groupingBy(SfpParameterDto::toolId));
	}

	public Map<Long, Integer> processToolGroup(
		Long toolId,
		List<SfpParameterDto> parameters,
		TimeRange timeRange
	) {
		// 파라미터 리스트에서 필요한 파라미터 추출
		SfpParameterDto productIdParam = extractParameterByName(parameters, productIdParameterName);
		SfpParameterDto incrementFlagParam = extractParameterByName(parameters, incrementCycleCountFlagParmaeterName);

		if (productIdParam == null || incrementFlagParam == null) {
			log.warn("toolId {}에 필요한 파라미터가 없습니다. product_id: {}, increment_flag: {}",
				toolId,
				productIdParam != null,
				incrementFlagParam != null);
			return Map.of();
		}

		// 1. increment_cycle_count_flag가 1인 시간 목록을 조회
		List<LocalDateTime> incrementFlagTraceAtList = getTraceAtListWithIncrementFlag(incrementFlagParam.id(),
			timeRange.from(),
			timeRange.to());

		if (incrementFlagTraceAtList.isEmpty()) {
			log.info("toolId {}의 생산 플래그 활성 값은 없습니다", toolId);
			return Map.of();
		}

		// 2. product_id로 '1'에서 조회한 시간 목록에 해당하는 값을 조회
		List<SfpParameterDataDto> productDataList = sfpRepository.findParameterDataByIdAndTraceAtList(productIdParam.id(),
			incrementFlagTraceAtList);

		// 3. '2'에서 조회된 결과에서 iValue의 그룹별 개수를 계산하여 반환
		return productDataList.stream()
							  .collect(Collectors.groupingBy(data -> (long)data.iValue(),
								  Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));
	}

	private SfpParameterDto extractParameterByName(
		List<SfpParameterDto> parameters,
		String parameterName
	) {
		return parameters.stream()
						 .filter(p -> p.name()
									   .equals(parameterName))
						 .findFirst()
						 .orElse(null);
	}

	public List<LocalDateTime> getTraceAtListWithIncrementFlag(
		Long parameterId,
		LocalDateTime from,
		LocalDateTime to
	) {
		// SfpRepository를 통해 파라미터 데이터 조회
		var parameterDataList = sfpRepository.findParameterDataByIdAndDateRange(parameterId, from, to);

		// iValue가 1인 데이터의 traceAt 목록 추출
		return parameterDataList.stream()
								.filter(data -> data.iValue() == 1)
								.map(SfpParameterDataDto::traceAt)
								.toList();
	}

	/**
	 * 현재 시간을 기준으로 가장 가까운 30분 단위의 시간 범위를 계산하여 반환
	 * 현재 시간을 포함하지 않는 이전 30분 단위 범위
	 * 예: 현재 시간이 14:43인 경우, from은 14:00, to는 14:30
	 *
	 * @return 계산된 시간 범위
	 */
	private TimeRange calculateTimeRange() {
		LocalDateTime now = LocalDateTime.now();

		// 현재 시간에서 가장 가까운 30분 단위로 내림 (to 계산)
		int currentMinute = now.getMinute();
		int toMinute = (currentMinute / 30) * 30;
		LocalDateTime to = now.withMinute(toMinute)
							  .withSecond(0)
							  .withNano(0);

		// from은 to에서 30분을 뺀 값
		LocalDateTime from = to.minusMinutes(30);

		return new TimeRange(from, to);
	}

	record TimeRange(LocalDateTime from, LocalDateTime to) {
	}
}
