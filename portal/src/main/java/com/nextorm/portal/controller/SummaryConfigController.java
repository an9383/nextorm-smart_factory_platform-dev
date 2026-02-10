package com.nextorm.portal.controller;

import com.nextorm.portal.dto.summaryconfig.SummaryConfigCreateRequestDto;
import com.nextorm.portal.dto.summaryconfig.SummaryConfigResponseDto;
import com.nextorm.portal.dto.summaryconfig.SummaryConfigUpdateRequestDto;
import com.nextorm.portal.service.SummaryConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/summary-configs")
@RequiredArgsConstructor
public class SummaryConfigController {
	private final SummaryConfigService summaryConfigService;

	@GetMapping(path = "")
	public List<SummaryConfigResponseDto> getSummaryConfigs() {
		return summaryConfigService.getSummaryConfigs();
	}

	@PostMapping("")
	public SummaryConfigResponseDto createSummaryConfig(@RequestBody SummaryConfigCreateRequestDto summaryConfigCreateRequestDto) {
		return summaryConfigService.createSummaryConfig(summaryConfigCreateRequestDto);
	}

	@PutMapping(path = "/{summaryConfigId}")
	public SummaryConfigResponseDto modifySummaryConfig(
		@PathVariable(name = "summaryConfigId") Long summaryConfigId,
		@RequestBody SummaryConfigUpdateRequestDto modifyRequestDto
	) {
		return summaryConfigService.modifySummaryConfig(summaryConfigId, modifyRequestDto);
	}

	@DeleteMapping(path = "/{summaryConfigId}")
	public ResponseEntity<Void> deleteSummaryConfig(@PathVariable(name = "summaryConfigId") Long id) {
		summaryConfigService.deleteSummaryConfig(id);
		return ResponseEntity.ok()
							 .build();
	}
}
