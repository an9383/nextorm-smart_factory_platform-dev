package com.nextorm.portal.controller;

import com.nextorm.portal.dto.base.BaseTreeItem;
import com.nextorm.portal.dto.parameter.*;
import com.nextorm.portal.service.parameter.ParameterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parameters")
@RequiredArgsConstructor
public class ParameterController {
	private final ParameterService parameterService;

	@GetMapping(value = "")
	public List<ParameterResponseDto> getParameters(ParameterSearchRequestDto searchRequestDto) {
		return parameterService.getParameters(searchRequestDto);
	}

	@GetMapping("/{parameterId}")
	public ParameterResponseDto get(@PathVariable(name = "parameterId") Long parameterId) {
		return parameterService.getParameter(parameterId);
	}

	@PostMapping("")
	public ParameterResponseDto createParameter(@RequestBody ParameterCreateRequestDto parameterCreateRequestDto) {
		return parameterService.createParameter(parameterCreateRequestDto);
	}

	@PutMapping("/{parameterId}")
	public ParameterResponseDto modifyParameter(
		@PathVariable(name = "parameterId") Long parameterId,
		@RequestBody ParameterUpdateRequestDto parameterUpdateRequestDto
	) {
		return parameterService.modifyParameter(parameterId, parameterUpdateRequestDto);
	}

	@DeleteMapping("{id}")
	public ResponseEntity<String> deleteParameter(@PathVariable(name = "id") Long id) {
		parameterService.deleteParameter(id);
		return ResponseEntity.ok("ok");
	}

	/**
	 * tool-id를 참조하는 dcp에 할당된 파라미터 목록을 조회한다.
	 */
	@GetMapping("/dcp-assigned")
	public List<ParameterResponseDto> getToolAssignedParameters(@RequestParam(name = "toolId") Long toolId) {
		return parameterService.getToolAssignedParameters(toolId);
	}

	@GetMapping("/virtual/{id}")
	public VirtualParameterResponseDto getVirtualParameter(@PathVariable(name = "id") Long id) {
		return parameterService.getVirtualParameterById(id);
	}

	@PostMapping("/virtual")
	public VirtualParameterResponseDto createVirtualParameter(@RequestBody VirtualParameterCreateRequestDto virtualParameterDto) {
		return parameterService.createVirtualParameter(virtualParameterDto);
	}

	@PutMapping("/virtual/{id}")
	public VirtualParameterResponseDto modifyVirtualParameter(
		@PathVariable(name = "id") Long id,
		@RequestBody VirtualParameterUpdateRequestDto virtualParameterDto
	) {
		return parameterService.modifyVirtualParameter(id, virtualParameterDto);
	}

	@PostMapping("/copy-parameters")
	public List<ParameterResponseDto> copyParametersByToolIds(
		@RequestBody ParameterCopyRequestDto parameterCopyRequestDto

	) {
		return parameterService.copyParametersByToolIds(parameterCopyRequestDto);
	}

	@GetMapping("/extra-datas")
	public List<ParameterExtraDataResponseDto> getParameterExtraDataByIds(
		@RequestParam(name = "ids") List<Long> parameterIds
	) {
		return parameterService.getParameterExtraDataList(parameterIds);
	}

	@GetMapping(value = "/tool-tree")
	public ResponseEntity<List<BaseTreeItem>> getToolParameterTree() {
		return ResponseEntity.ok()
							 .body(parameterService.getToolParameterTree());
	}

	@GetMapping("/tools-by-parameters")
	public List<ParameterToolResponseDto> getToolsByParameters(
		@RequestParam(name = "ids") List<Long> parameterIds
	) {
		return parameterService.getToolsByParameters(parameterIds);
	}

	/**
	 * 메타데이터 타입 파라미터의 값 수정
	 */
	@PutMapping("/{parameterId}/meta-value")
	public ParameterResponseDto modifyMetaDataParameter(
		@PathVariable(name = "parameterId") Long parameterId,
		@RequestBody MetaDataParameterUpdateRequestDto requestDto
	) {
		return parameterService.modifyMetaDataParameter(parameterId, requestDto);
	}

	/**
	 * 메타데이터 타입 파라미터의 값 일괄 수정
	 */
	@PutMapping("/meta-value/bulk")
	public void modifyMetaDataParametersBulk(
		@RequestBody List<MetaDataParameterBulkUpdateRequestDto> requestList
	) {
		parameterService.modifyMetaDataParametersBulk(requestList);
	}

}
