package com.nextorm.extensions.misillan.alarm.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@ToString(exclude = "readUsers")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "alarm_history")
public class AlarmHistory {
	@Getter
	@RequiredArgsConstructor
	public enum TriggerType {
		VALUE_OVER("값 초과"), TIME_OVER("시간 초과");

		private final String displayName;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Comment("설비 이름")
	@Column(name = "tool_name")
	private String toolName;

	@Column(name = "tool_id")
	private Long toolId;

	@Comment("품목 이름")
	@Column(name = "product_name")
	private String productName;

	@Column(name = "product_id")
	private Long productId;

	@Column(name = "parameter_id")
	private Long parameterId;

	@Comment("파라미터 이름")
	@Column(name = "parameter_name")
	private String parameterName;

	@Comment("온도/압력/금속검출 값")
	private Double value;

	@Comment("알람 발생 일시")
	@Column(name = "alarm_dt")
	private LocalDateTime alarmDt;

	@Comment("조건 기준 값")
	@Column(name = "threshold")
	private Double threshold;

	@Enumerated(EnumType.STRING)
	@Comment("알람 발생 타입")
	@Column(name = "condition_type")
	private ConditionType conditionType;

	@Enumerated(EnumType.STRING)
	@Comment("알람 감지 조건 타입")
	@Column(name = "trigger_type")
	private AlarmHistory.TriggerType triggerType;

	@OneToMany(mappedBy = "alarmHistory", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<AlarmReadUser> readUsers = new HashSet<>();
}