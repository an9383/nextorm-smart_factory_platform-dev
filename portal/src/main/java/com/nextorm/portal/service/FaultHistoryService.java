package com.nextorm.portal.service;

import com.nextorm.common.db.entity.FaultHistory;
import com.nextorm.common.db.entity.Parameter;
import com.nextorm.common.db.repository.FaultHistoryRepository;
import com.nextorm.common.db.repository.ParameterRepository;
import com.nextorm.common.db.repository.dto.FaultCount;
import com.nextorm.portal.dto.FaultCountResponseDto;
import com.nextorm.portal.dto.FaultHistoryResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FaultHistoryService {
	private final FaultHistoryRepository faultHistoryRepository;
	private final ParameterRepository parameterRepository;

	@Transactional(readOnly = true)
	public List<FaultHistoryResponseDto> getFaultHistories(
		Long parameterId,
		LocalDateTime from,
		LocalDateTime to
	) {
		List<FaultHistory> faultHistories = faultHistoryRepository.findByParameterIdAndFaultAtBetweenOrderByFaultAtAsc(
			parameterId,
			from,
			to);

		return faultHistories.stream()
							 .map(FaultHistoryResponseDto::of)
							 .collect(Collectors.toList());
	}

	public FaultCountResponseDto getParameterFaultCountByToolIdAndFaultAtBetween(
		Long toolId,
		LocalDateTime from,
		LocalDateTime to
	) {
		Map<Long, Parameter> parameterMap = parameterRepository.findByToolId(toolId)
															   .stream()
															   .collect(Collectors.toMap(Parameter::getId,
																   parameter -> parameter));

		List<Long> parameterIds = new ArrayList<>(parameterMap.keySet());
		List<FaultCount> faultCounts = faultHistoryRepository.groupParameterIdByParameterIdAndFaultAtBetween(
			parameterIds,
			from,
			to);

		List<FaultCountResponseDto.Item> items = faultCounts.stream()
															.map(faultCount -> FaultCountResponseDto.Item.of(faultCount.getParameterId(),
																parameterMap.get(faultCount.getParameterId())
																			.getName(),
																faultCount.getCount()))
															.toList();

		return new FaultCountResponseDto(items);
	}
}
