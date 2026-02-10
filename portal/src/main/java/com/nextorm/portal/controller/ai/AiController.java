package com.nextorm.portal.controller.ai;

import com.nextorm.common.db.dto.ai.ModelBuildRequestDto;
import com.nextorm.portal.dto.aimodel.AiInferenceResponseDto;
import com.nextorm.portal.dto.aimodel.ModelResponseDto;
import com.nextorm.portal.dto.aimodel.correlation.CorrelationRankDto;
import com.nextorm.portal.dto.aimodel.correlation.CorrelationRequest;
import com.nextorm.portal.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {
	private final AiService aiService;

	@PostMapping("/correlation")
	public List<CorrelationRankDto> calculateCorrelation(@RequestBody CorrelationRequest request) {
		return aiService.calculateCorrelationRanksAutoSelect(request);
	}

	@GetMapping("/models")
	public List<ModelResponseDto> getModels() {
		return aiService.getModels();
	}

	@GetMapping("/models/{id}")
	public ModelResponseDto getModel(@PathVariable(name = "id") Long id) {
		return aiService.getModelById(id);
	}

	@GetMapping("/models/{id}/inference-data")
	public AiInferenceResponseDto getInferenceData(
		@PathVariable(name = "id") Long modelId,
		@RequestParam("from") LocalDateTime from,
		@RequestParam("to") LocalDateTime to
	) {
		return aiService.getInferenceData(modelId, from, to);
	}

	@GetMapping("/models/tool/{toolId}")
	public List<ModelResponseDto> getModelsByToolId(@PathVariable(name = "toolId") Long toolId) {
		return aiService.getModelByToolId(toolId);
	}

	@PostMapping("/models")
	public ModelResponseDto buildModel(@RequestBody ModelBuildRequestDto request) {
		return aiService.buildModel(request);
	}

	@DeleteMapping("/models/{id}")
	public void deleteModel(@PathVariable(name = "id") Long id) {
		aiService.delete(id);
	}

}
