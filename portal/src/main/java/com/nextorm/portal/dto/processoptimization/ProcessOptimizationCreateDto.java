package com.nextorm.portal.dto.processoptimization;

import com.nextorm.common.db.entity.ai.AiModel;
import com.nextorm.portal.entity.processoptimization.ListOptimizationParametersToJsonStringConverter;
import com.nextorm.portal.entity.processoptimization.OptimizationParameters;
import com.nextorm.portal.entity.processoptimization.ProcessOptimization;
import com.nextorm.portal.enums.ProcessOptimizationStatus;
import jakarta.persistence.Convert;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProcessOptimizationCreateDto {
	private String name;
	private Long toolId;
	private double optimalValue;
	private double targetValue;
	@Convert(converter = ListOptimizationParametersToJsonStringConverter.class)
	private List<OptimizationParameters> optimizationParameters;
	private double totalCount;
	private double completeCount;
	private ProcessOptimizationStatus status;
	private Long aiModelId;

	public static ProcessOptimization toEntity(
		ProcessOptimizationCreateDto processOptimizationCreateDto,
		AiModel aiModel
	) {
		return ProcessOptimization.builder()
								  .name(processOptimizationCreateDto.getName())
								  .toolId(processOptimizationCreateDto.getToolId())
								  .optimalValue(processOptimizationCreateDto.getOptimalValue())
								  .optimizationParameters(processOptimizationCreateDto.getOptimizationParameters())
								  .targetValue(processOptimizationCreateDto.getTargetValue())
								  .totalCount(processOptimizationCreateDto.getTotalCount())
								  .completeCount(processOptimizationCreateDto.getCompleteCount())
								  .status(processOptimizationCreateDto.getStatus())
								  .aiModel(aiModel)
								  .build();
	}
}
