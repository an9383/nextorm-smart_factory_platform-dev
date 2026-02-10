package com.nextorm.extensions.misillan.alarm.controller;

import com.nextorm.extensions.misillan.alarm.dto.toolparametermapping.ToolParameterMappingCreateDto;
import com.nextorm.extensions.misillan.alarm.dto.toolparametermapping.ToolParameterMappingModifyDto;
import com.nextorm.extensions.misillan.alarm.dto.toolparametermapping.ToolParameterMappingResponseDto;
import com.nextorm.extensions.misillan.alarm.service.ToolParameterMappingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/misillan-alarm/tool-parameter-mappings")
@RequiredArgsConstructor
public class ToolParameterMappingController {
	private final ToolParameterMappingService toolParameterMappingService;

	@GetMapping("")
	public List<ToolParameterMappingResponseDto> getAll() {
		return toolParameterMappingService.getMappingList();
	}

	@PostMapping("")
	public void createMapping(
		@RequestBody ToolParameterMappingCreateDto toolParameterMappingCreateDto
	) {
		toolParameterMappingService.createMapping(toolParameterMappingCreateDto);
	}

	@PutMapping("/{id}")
	public void modifyMapping(
		@PathVariable Long id,
		@RequestBody ToolParameterMappingModifyDto toolParameterMappingModifyDto
	) {
		toolParameterMappingService.modifyToolParameterMapping(id, toolParameterMappingModifyDto);
	}

	@DeleteMapping("/{id}")
	public void deleteMapping(@PathVariable Long id) {
		toolParameterMappingService.deleteMapping(id);
	}

}
