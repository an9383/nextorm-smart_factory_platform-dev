package com.nextorm.processor.scriptengine.executor.sample;

import com.nextorm.common.db.entity.Parameter;
import com.nextorm.common.db.entity.ParameterData;
import com.nextorm.common.db.repository.ParameterDataRepository;
import com.nextorm.processor.scriptengine.BindingMember;
import com.nextorm.processor.scriptengine.ExecuteContext;
import com.nextorm.processor.scriptengine.executor.VpCalculateExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class CalculateProductionLeadTime implements VpCalculateExecutor {

	private final ParameterDataRepository parameterDataRepository;
	private final ExecuteContext executeContext;

	@Override
	public String getScript() {
		return """
			function calculateProductionLeadTime(productionCountId, searchRangeSecond) {
				return self_calculateProductionLeadTime.calculate(productionCountId, searchRangeSecond);
			}
			""";
	}

	@Override
	public BindingMember getBindingMember() {
		return BindingMember.create("self_calculateProductionLeadTime", this);
	}

	public Integer calculate(
		String productionCountIdString,
		String searchRangeSecondString
	) {
		Long productionCountId = Long.parseLong(productionCountIdString);

		Object productionCountValue = executeContext.getParamIdValueMap()
													.get(productionCountId);

		Object previousProductionCountValue = executeContext.getPreviousParamIdValueMap()
															.get(productionCountId);

		if (productionCountValue == null || previousProductionCountValue == null) {
			return null;
		}

		Integer curr = Integer.parseInt(productionCountValue.toString());
		Integer previous = Integer.parseInt(previousProductionCountValue.toString());

		// 현재값과 이전값이 동일하면 리드타임 계산하지 않음
		if (curr.equals(previous)) {
			return null;
		}

		LocalDateTime now = LocalDateTime.now();
		LocalDateTime searchStartTime = now.minusSeconds(Long.parseLong(searchRangeSecondString));
		Sort sort = Sort.by(Sort.Direction.DESC, "traceAt");

		List<ParameterData> productCountRaws = parameterDataRepository.findByParameterIdAndTraceAtBetween(
			productionCountId,
			searchStartTime,
			now,
			sort);

		List<ParameterData> previousProductionCountRaw = productCountRaws.stream()
																		 .filter(it -> previous.equals(it.getValue(
																			 Parameter.DataType.INTEGER)))
																		 .toList();

		// 이전 생산량과 동일한 값을 찾지 못하면 null 반환
		if (previousProductionCountRaw.isEmpty()) {
			return null;
		}

		LocalDateTime endTime = previousProductionCountRaw.get(0)
														  .getTraceAt();
		LocalDateTime startTime = previousProductionCountRaw.get(previousProductionCountRaw.size() - 1)
															.getTraceAt();

		return Math.toIntExact(Duration.between(startTime, endTime)
									   .getSeconds());
	}
}
