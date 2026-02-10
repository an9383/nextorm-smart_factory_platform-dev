package com.nextorm.common.db.entity.ai;

import com.nextorm.common.db.dto.ai.ModelBuildRequestDto;
import com.nextorm.common.db.entity.BaseEntity;
import com.nextorm.common.db.entity.Tool;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ai_model")
@SuperBuilder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AiModel extends BaseEntity {

	@Getter
	@AllArgsConstructor
	public enum Status {
		RUNNING("학습중"), COMPLETE("완료"), FAIL("실패");

		private final String displayName;
	}

	@Column(name = "name", nullable = false)
	private String name;
	@Column(name = "description")
	private String description;
	@Column(name = "algorithm", nullable = false)
	private String algorithm;

	@Column(columnDefinition = "varchar(50)")
	@Enumerated(EnumType.STRING)
	private Status status;

	@Comment("학습 실패 사유")
	@Column(name = "failure_reason", columnDefinition = "varchar(1000)")
	private String failureReason;

	@Column(name = "tool_id", nullable = false)
	private Long toolId;
	@Column(name = "tool_name", nullable = false)
	private String toolName;

	@Column(name = "from_date")
	private LocalDateTime from;

	@Column(name = "to_date")
	private LocalDateTime to;
	@Column(name = "y_parameter_id")
	private Long yParameterId;

	@Column(name = "parameter_details", length = 1000)
	@Convert(converter = ListParameterDetailToJsonStringConverter.class)
	private List<ParameterDetail> parameterDetails;

	public void buildComplete() {
		this.status = Status.COMPLETE;
	}

	public static AiModel toAiModel(
		ModelBuildRequestDto request,
		Tool tool,
		List<ParameterDetail> parameterDetails
	) {
		return AiModel.builder()
					  .name(request.getName())
					  .description(request.getDescription())
					  .algorithm(request.getAlgorithm())
					  .status(AiModel.Status.RUNNING)
					  .toolId(tool.getId())
					  .toolName(tool.getName())
					  .from(request.getFrom())
					  .to(request.getTo())
					  .yParameterId(request.getYParameterId())
					  .parameterDetails(parameterDetails)
					  .build();
	}

	public void updateFailure(String failureReason) {
		this.status = Status.FAIL;
		this.failureReason = failureReason;
	}
}
