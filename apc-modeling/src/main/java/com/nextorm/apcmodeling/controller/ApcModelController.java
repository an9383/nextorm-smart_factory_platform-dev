package com.nextorm.apcmodeling.controller;

import com.nextorm.apcmodeling.dto.*;
import com.nextorm.apcmodeling.service.ApcModelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/apc/models")
@RequiredArgsConstructor
public class ApcModelController {
	private final ApcModelService apcModelService;

	@GetMapping(value = "")
	public ResponseEntity<List<ApcModelCreateResponseDto>> getApcModels() {
		return ResponseEntity.ok(apcModelService.getApcModels());
	}

	@GetMapping(value = "/{apcModelId}/versions")
	public ResponseEntity<List<ApcModelVersionListResponseDto>> getModelVersionsByModelId(@PathVariable(name = "apcModelId") Long apcModelId) {
		return ResponseEntity.ok(apcModelService.getModelVersionsByModelId(apcModelId));
	}

	@GetMapping(value = "/versions/{apcModelVersionId}")
	public ResponseEntity<ApcModelVersionResponseDto> getApcModelVersion(
		@PathVariable(name = "apcModelVersionId") Long apcModelVersionId
	) {
		return ResponseEntity.ok(apcModelService.getApcModelVersion(apcModelVersionId));
	}

	@PostMapping(value = "")
	public ResponseEntity<ApcModelCreateResponseDto> createApcModel(@RequestBody ApcModelCreateRequestDto apcModelCreateRequestDto) {
		return ResponseEntity.ok(apcModelService.createApcModel(apcModelCreateRequestDto));
	}

	@PostMapping(value = "/{apcModelId}/versions")
	public ResponseEntity<ApcModelVersionCreateResponseDto> createApcModelVersion(
		@PathVariable(name = "apcModelId") Long apcModelId,
		@RequestBody ApcModelVersionCreateRequestDto apcModelVersionCreateRequestDto
	) {
		return ResponseEntity.ok(apcModelService.createApcModelVersion(apcModelId, apcModelVersionCreateRequestDto));
	}

	@PutMapping(value = "/{apcModelId}")
	public ResponseEntity<ApcModelUpdateResponseDto> modifyApcModel(
		@PathVariable(name = "apcModelId") Long apcModelId,
		@RequestBody ApcModelUpdateRequestDto apcModelUpdateRequestDto
	) {
		return ResponseEntity.ok(apcModelService.modifyApcModel(apcModelId, apcModelUpdateRequestDto));
	}

	@DeleteMapping(value = "/{apcModelId}")
	public ResponseEntity<String> deleteApcModel(@PathVariable(name = "apcModelId") Long apcModelId) {
		return apcModelService.deleteApcModel(apcModelId);
	}

}
