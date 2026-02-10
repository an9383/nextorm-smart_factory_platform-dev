package com.nextorm.portal.controller;

import com.nextorm.portal.dto.tool.*;
import com.nextorm.portal.service.ToolService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tools")
@RequiredArgsConstructor
public class ToolController {
	private final ToolService toolService;

	@GetMapping("")
	public ResponseEntity<List<ToolResponseDto>> getTools(
		ToolSearchRequestDto toolSearchRequestDto
	) {
		List<ToolResponseDto> tools = toolService.getTools(toolSearchRequestDto);
		return ResponseEntity.ok(tools);
	}

	@GetMapping(path = "/{id}")
	public ResponseEntity<ToolResponseDto> getTool(@PathVariable(name = "id") Long id) {
		return ResponseEntity.ok()
							 .body(toolService.getTool(id));
	}

	@PostMapping("")
	public ToolResponseDto createTool(@RequestBody ToolCreateRequestDto toolCreateRequestDto) {
		return toolService.createTool(toolCreateRequestDto);
	}

	@PutMapping("/{toolId}")
	public ToolResponseDto modifyTool(
		@PathVariable(name = "toolId") Long toolId,
		@RequestBody ToolUpdateRequestDto toolUpdateRequestDto
	) {
		return toolService.modifyTool(toolId, toolUpdateRequestDto);
	}

	@DeleteMapping("/{toolId}")
	public ResponseEntity<String> deleteTool(@PathVariable(name = "toolId") Long toolId) {
		toolService.deleteTool(toolId);
		return ResponseEntity.ok("delete success!!");
	}

	@PostMapping("/bulk-delete")
	public ResponseEntity<String> bulkDeleteTool(@RequestBody ToolBulkDeleteRequestDto requestDto) {
		toolService.deleteTools(requestDto.getIds());
		return ResponseEntity.ok("delete success!!");
	}

	@GetMapping(path = "/{toolId}/kafka")
	public ResponseEntity<ToolKafkaResponseDto> getToolKafka(@PathVariable(name = "toolId") Long toolId) {
		return ResponseEntity.ok()
							 .body(toolService.getToolKafka(toolId));
	}

	@PostMapping(path = "/{toolId}/kafka")
	public ResponseEntity<String> createToolKafka(
		@PathVariable(name = "toolId") Long toolId,
		@RequestBody ToolKafkaCreateRequestDto requestDto
	) {
		toolService.createToolKafka(toolId, requestDto);
		return ResponseEntity.ok()
							 .body(String.valueOf(HttpStatus.CREATED));
	}

	@PutMapping(path = "/{toolId}/kafka")
	public ResponseEntity<String> modifyToolKafka(
		@PathVariable(name = "toolId") Long toolId,
		@RequestBody ToolKafkaUpdateRequestDto requestDto
	) {
		toolService.modifyToolKafka(toolId, requestDto);
		return ResponseEntity.ok()
							 .body(String.valueOf(HttpStatus.OK));
	}

	@GetMapping(path = "/{toolId}/kafka/is-exists")
	public ResponseEntity<Boolean> isExistsToolKafka(@PathVariable(name = "toolId") Long toolId) {
		return ResponseEntity.ok()
							 .body(toolService.checkExistsToolKafka(toolId));
	}

	@GetMapping("/tool-collect-status")
	public ResponseEntity<List<ToolCollectStatusResponseDto>> getToolCollectStatus() {
		return ResponseEntity.ok(toolService.getToolCollectStatus());
	}
}