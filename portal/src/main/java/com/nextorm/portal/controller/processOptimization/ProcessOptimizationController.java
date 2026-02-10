package com.nextorm.portal.controller.processOptimization;

import com.nextorm.portal.dto.base.BaseTreeItem;
import com.nextorm.portal.dto.processoptimization.ProcessOptimizationCreateDto;
import com.nextorm.portal.dto.processoptimization.ProcessOptimizationResponseDto;
import com.nextorm.portal.entity.processoptimization.ProcessOptimization;
import com.nextorm.portal.service.processoptimization.ProcessOptimizationAnalysisService;
import com.nextorm.portal.service.processoptimization.ProcessOptimizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/process-optimization")
@RequiredArgsConstructor
@Slf4j
public class ProcessOptimizationController {

	private final ProcessOptimizationService processOptimizationService;
	private final ProcessOptimizationAnalysisService processOptimizationAnalysisService;

	@GetMapping(value = "/{id}")
	public ResponseEntity<ProcessOptimizationResponseDto> getOptimization(@PathVariable(name = "id") Long id) {
		return ResponseEntity.ok()
							 .body(processOptimizationService.getOptimization(id));
	}

	@GetMapping(value = "")
	public ResponseEntity<List<ProcessOptimizationResponseDto>> getOptimizations() {
		return ResponseEntity.ok()
							 .body(processOptimizationService.getOptimizations());
	}

	@GetMapping(value = "/tree")
	public ResponseEntity<List<BaseTreeItem>> getAiModelTree() {
		return ResponseEntity.ok()
							 .body(processOptimizationService.getAiModelTree());
	}

	@PostMapping(value = "")
	public ResponseEntity<ProcessOptimization> saveProcess(
		@RequestBody ProcessOptimizationCreateDto processOptimizationCreateDto
	) {
		return ResponseEntity.ok()
							 .body(processOptimizationService.saveProcess(processOptimizationCreateDto));
	}

	@GetMapping(value = "/optimal-y-values/{processOptimizationId}")
	public ResponseEntity<List<Map<String, Double>>> getOptimalYValueList(@PathVariable(name = "processOptimizationId") Long processOptimizationId) throws
		Exception {
		return ResponseEntity.ok()
							 .body(processOptimizationAnalysisService.getOptimalYValueList(processOptimizationId));
	}

	@DeleteMapping(value = "/{ids}")
	public ResponseEntity<Object> deleteProcess(@PathVariable("ids") List<Long> ids) {
		processOptimizationService.deleteProcess(ids);
		return ResponseEntity.ok()
							 .body("delete success!");
	}
}