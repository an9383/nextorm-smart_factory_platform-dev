package com.nextorm.portal.dto.processoptimization;

import com.nextorm.portal.entity.processoptimization.OptimizationParameters;
import com.nextorm.portal.entity.processoptimization.ProcessOptimization;
import com.nextorm.portal.enums.ProcessOptimizationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessOptimizationResponseDto {

	private Long id;
	private LocalDateTime createAt;
	private String name;
	private Long toolId;
	private double targetValue;
	private double optimalValue;
	private double totalCount;
	private double completeCount;
	private ProcessOptimizationStatus status;
	private String failureReason;
	private String aiModelName;
	private Long aiModelId;
	private List<OptimizationParametersDto> optimizationParameterList;

	public static ProcessOptimizationResponseDto from(
		ProcessOptimization processOptimization
	) {
		List<OptimizationParametersDto> optimizationParametersDtoList = new ArrayList<>();
		long[] parameterIdList = processOptimization.getOptimizationParameters()
													.stream()
													.map(OptimizationParameters::getParameterId)
													.mapToLong(Long::longValue)
													.toArray();

		for (int i = 0; i < parameterIdList.length; i++) {
			OptimizationParametersDto optimizationParametersDto = OptimizationParametersDto.builder()
																						   .parameterId(
																							   processOptimization.getParameterId()[i])
																						   .minScaleX(
																							   processOptimization.getMinScaleX()[i])
																						   .maxScaleX(
																							   processOptimization.getMaxScaleX()[i])
																						   .step(processOptimization.getStep()[i])
																						   .build();
			optimizationParametersDtoList.add(optimizationParametersDto);
		}

		return ProcessOptimizationResponseDto.builder()
											 .id(processOptimization.getId())
											 .createAt(processOptimization.getCreateAt())
											 .name(processOptimization.getName())
											 .toolId(processOptimization.getToolId())
											 .targetValue(processOptimization.getTargetValue())
											 .optimalValue(processOptimization.getOptimalValue())
											 .status(processOptimization.getStatus())
											 .failureReason(processOptimization.getFailureReason())
											 .totalCount(processOptimization.getTotalCount())
											 .completeCount(processOptimization.getCompleteCount())
											 .aiModelName(processOptimization.getAiModel()
																			 .getName())
											 .aiModelId(processOptimization.getAiModel()
																		   .getId())
											 .optimizationParameterList(optimizationParametersDtoList)
											 .build();
	}
}

