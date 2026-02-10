package com.nextorm.portal.controller;

import com.nextorm.portal.dto.tool.ToolStatusRequestDto;
import com.nextorm.portal.dto.tool.ToolStatusResponseDto;
import com.nextorm.portal.service.ToolStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tool-status")
@RequiredArgsConstructor
public class ToolStatusController {
	private final ToolStatusService toolStatusService;

	@GetMapping("")
	public ResponseEntity<List<ToolStatusResponseDto>> getToolStatus(
		ToolStatusRequestDto toolStatusRequestDto
	) {
		List<ToolStatusResponseDto> tools = toolStatusService.getToolStatus(toolStatusRequestDto);
		return ResponseEntity.ok(tools);
	}
}
