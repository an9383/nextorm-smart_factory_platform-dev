package com.nextorm.portal.controller;

import com.nextorm.portal.dto.FaultCountResponseDto;
import com.nextorm.portal.dto.FaultHistoryResponseDto;
import com.nextorm.portal.service.FaultHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/fault-histories")
@RequiredArgsConstructor
public class FaultHistoryController {
	private final FaultHistoryService faultHistoryService;

	@GetMapping
	public List<FaultHistoryResponseDto> getFaultHistories(
		@RequestParam("parameterId") Long parameterId,
		@RequestParam("from") LocalDateTime from,
		@RequestParam("to") LocalDateTime to
	) {
		return faultHistoryService.getFaultHistories(parameterId, from, to);
	}

	@GetMapping("/count")
	public FaultCountResponseDto getParameterFaultCountByToolIdAndFaultAtBetween(
		@RequestParam("toolId") Long toolId,
		@RequestParam("from") LocalDateTime from,
		@RequestParam("to") LocalDateTime to
	) {
		return faultHistoryService.getParameterFaultCountByToolIdAndFaultAtBetween(toolId, from, to);
	}
}
