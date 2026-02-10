package com.nextorm.portal.controller;

import com.nextorm.portal.dto.collectorconfig.CollectorConfigCreateRequestDto;
import com.nextorm.portal.dto.collectorconfig.CollectorConfigResponseDto;
import com.nextorm.portal.dto.collectorconfig.CollectorConfigUpdateRequestDto;
import com.nextorm.portal.service.CollectorConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/collector-configs")
@RequiredArgsConstructor
public class CollectorConfigController {
	private final CollectorConfigService collectorConfigService;

	@GetMapping(path = "")
	public List<CollectorConfigResponseDto> getAll() {
		return collectorConfigService.getAll();
	}

	@PostMapping("")
	public CollectorConfigResponseDto createSummaryConfig(@RequestBody CollectorConfigCreateRequestDto collectorConfigCreateRequestDto) {
		return collectorConfigService.create(collectorConfigCreateRequestDto);
	}

	@PutMapping(path = "/{configId}")
	public CollectorConfigResponseDto modify(
		@PathVariable(name = "configId") Long configId,
		@RequestBody CollectorConfigUpdateRequestDto modifyRequestDto
	) {
		return collectorConfigService.modify(configId, modifyRequestDto);
	}

	@DeleteMapping(path = "/{configId}")
	public ResponseEntity<Void> delete(@PathVariable(name = "configId") Long id) {
		collectorConfigService.delete(id);
		return ResponseEntity.ok()
							 .build();
	}
}
