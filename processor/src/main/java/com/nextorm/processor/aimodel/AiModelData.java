package com.nextorm.processor.aimodel;

import com.nextorm.common.db.entity.ai.AiModel;
import com.nextorm.common.db.entity.ai.ParameterDetail;
import lombok.*;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
public class AiModelData  {
	private Long id;
	private String name;
	private String description;
	private String algorithm;
	private String status;
	private String failureReason;
	private Long toolId;
	private String toolName;
	private String from;
	private String to;
	private Long yParameterId;
	private List<ParameterDetail> parameterDetails;

	public static AiModelData  of(AiModel aiModel) {
		return AiModelData .builder()
								  .id(aiModel.getId())
								  .name(aiModel.getName())
								  .description(aiModel.getDescription())
								  .algorithm(aiModel.getAlgorithm())
								  .status(aiModel.getStatus()
												 .name())
								  .failureReason(aiModel.getFailureReason())
								  .toolId(aiModel.getToolId())
								  .toolName(aiModel.getToolName())
								  .from(aiModel.getFrom()
											   .toString())
								  .to(aiModel.getTo()
											 .toString())
								  .yParameterId(aiModel.getYParameterId())
								  .parameterDetails(aiModel.getParameterDetails())
								  .build();
	}
}
