package com.nextorm.extensions.misillan.alarm.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

/**
 * EQMS의 설비 정보와, SFP의 파라미터 정보를 매핑하는 엔티티
 * <p>
 * 그리고 맵핑한 파라미터가 어떤 조건(온도, 압력, 금속검출 등)에 해당하는지 저장
 */
@Entity
@Table(name = "tool_parameter_mappings")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToolParameterMapping {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@JoinColumn(name = "eqms_tool_id")
	@ManyToOne(fetch = FetchType.LAZY)
	private EqmsTool tool;
	@JoinColumn(name = "sfp_parameter_id")
	@ManyToOne(fetch = FetchType.LAZY)
	private SfpParameter parameter;

	@Enumerated(EnumType.STRING)
	@Comment("알람 감지 조건 타입")
	@Column(name = "condition_type")
	private ConditionType conditionType;

	public static ToolParameterMapping toEntity(
		EqmsTool eqmsTool,
		SfpParameter sfpParameter,
		ConditionType conditionType
	) {

		return ToolParameterMapping.builder()
								   .tool(eqmsTool)
								   .parameter(sfpParameter)
								   .conditionType(conditionType)
								   .build();
	}

	public void modify(
		EqmsTool eqmsTool,
		SfpParameter sfpParameter,
		ConditionType conditionType
	) {
		this.tool = eqmsTool;
		this.parameter = sfpParameter;
		this.conditionType = conditionType;
	}
}
