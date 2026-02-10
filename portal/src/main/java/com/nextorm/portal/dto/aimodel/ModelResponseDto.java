package com.nextorm.portal.dto.aimodel;

import com.nextorm.common.db.entity.ai.AiModel;
import com.nextorm.common.db.entity.ai.ParameterDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ModelResponseDto {

	private Long id;
	private String name;
	private String description;
	private String algorithm;
	private String status;
	private LocalDateTime createdAt;

	private Long toolId;
	private String toolName;

	private LocalDateTime from;
	private LocalDateTime to;

	private Long yParameterId;
	private String yParameterName;
	private List<ParameterDetail> parameterDetails;

	private String failureReason;

	public static ModelResponseDto toModelResponseDto(AiModel aiModel) {
		return ModelResponseDto.builder()
							   .id(aiModel.getId())
							   .name(aiModel.getName())
							   .description(aiModel.getDescription())
							   .algorithm(aiModel.getAlgorithm())
							   .status(aiModel.getStatus()
											  .getDisplayName())
							   .createdAt(aiModel.getCreateAt())
							   .toolId(aiModel.getToolId())
							   .toolName(aiModel.getToolName())
							   .from(aiModel.getFrom())
							   .to(aiModel.getTo())
							   .yParameterId(aiModel.getYParameterId())
							   .parameterDetails(aiModel.getParameterDetails())
							   .failureReason(aiModel.getFailureReason())
							   .build();
	}
}
