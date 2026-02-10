package com.nextorm.apcmodeling.controller;

import com.nextorm.apcmodeling.dto.*;
import com.nextorm.apcmodeling.service.ApcResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/apc/results")
@RequiredArgsConstructor
public class ApcResultController {

	private final ApcResultService apcResultService;

	// TODO:: 결과 조회하는 API가 현재 두개 존재하는데, 두개가 된 의도를 파악 후 가능하면 하나로 통합한다

	@GetMapping(path = "")
	public ResponseEntity<List<ApcRequestStatusResponseDto>> getApcRequestStatusList(ApcResultSearchRequestDto searchRequestDto) {
		return ResponseEntity.ok(apcResultService.getApcRequestStatusList(searchRequestDto));
	}

	@GetMapping(path = "/{requestId}")
	public ResponseEntity<List<ApcRequestResultDto>> getApcResultData(@PathVariable(name = "requestId") Long requestId) {
		return ResponseEntity.ok(apcResultService.getApcResultData(requestId));
	}

	@GetMapping("/trend")
	public ResponseEntity<List<ApcResultTrendResponseDto>> getResultParameterTrendData(
		ApcTrendSearchRequestDto apcTrendSearchRequestDto
	) {
		return ResponseEntity.ok(apcResultService.getResultParameterTrendData(apcTrendSearchRequestDto));
	}
}
