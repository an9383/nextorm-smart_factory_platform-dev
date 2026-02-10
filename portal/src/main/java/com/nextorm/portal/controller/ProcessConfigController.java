package com.nextorm.portal.controller;

import com.nextorm.portal.dto.processconfig.ProcessConfigCreateRequestDto;
import com.nextorm.portal.dto.processconfig.ProcessConfigResponseDto;
import com.nextorm.portal.dto.processconfig.ProcessConfigUpdateRequestDto;
import com.nextorm.portal.service.ProcessConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/process-configs")
@RequiredArgsConstructor
public class ProcessConfigController {
	private final ProcessConfigService processConfigService;

	@GetMapping(path = "")
	public List<ProcessConfigResponseDto> getProcessConfigs() {
		return processConfigService.getProcessConfigs();
	}

	@PostMapping("")
	public ProcessConfigResponseDto createProcessConfig(@RequestBody ProcessConfigCreateRequestDto processConfigCreateRequestDto) {
		return processConfigService.createProcessConfig(processConfigCreateRequestDto);
	}

	@PutMapping(path = "/{processConfigId}")
	public ProcessConfigResponseDto modifyProcessConfig(
		@PathVariable(name = "processConfigId") Long processConfigId,
		@RequestBody ProcessConfigUpdateRequestDto modifyRequestDto
	) {
		return processConfigService.modifyProcessConfig(processConfigId, modifyRequestDto);
	}

	@DeleteMapping(path = "/{processConfigId}")
	public ResponseEntity<Void> deleteProcessConfig(@PathVariable(name = "processConfigId") Long id) {
		processConfigService.deleteProcessConfig(id);
		return ResponseEntity.ok()
							 .build();
	}
}
