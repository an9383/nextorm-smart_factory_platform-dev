package com.nextorm.extensions.misillan.alarm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

/**
 * 알람 조건 정보르 저장하는 엔티티
 * <p>
 * - 각 알람 조건은 특정 설비-파라미터 조합에 대해 설정됨
 * - 품목의 알람 조건값 (온도, 압력 등)은 저장 시점에 화면에 입력한 값을 캡쳐하여 저장한다
 * ( => ProductAlarmCondition Entity의 값을 사용하지 않는다는 의미 )
 */
@Entity
@Table(name = "alarm_conditions")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlarmCondition {
	@Id
	@Comment("알람 조건 ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Comment("알람 조건을 체크할 설비-파라미터 정보")
	@JoinColumn(name = "tool_parameter_mapping_id")
	@ManyToOne(fetch = FetchType.LAZY)
	private ToolParameterMapping toolParameterMapping;

	@Comment("알람 조건을 체크할 품목-알람조건 정보")
	@JoinColumn(name = "product_alarm_condition_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
	@ManyToOne(fetch = FetchType.LAZY)
	private ProductAlarmCondition productAlarmCondition;

	@Comment("온도")
	@Column(name = "temperature")
	private Double temperature;

	@Comment("압력")
	@Column(name = "pressure")
	private Double pressure;

	@Comment("알람 활성화 여부")
	@Column(name = "is_active", nullable = false)
	private boolean isActive;

	@Comment("알람 활성화 시간")
	@Column(name = "active_time")
	private LocalDateTime activeTime;

	@Comment("타이머")
	@Column(name = "timer")
	private Long timer;

	/**
	 * 알람 조건 수정 메서드
	 */
	public void modify(
		Double temperature,
		Double pressure,
		Long timer,
		ToolParameterMapping toolParameterMapping,
		ProductAlarmCondition productAlarmCondition
	) {
		this.temperature = temperature;
		this.pressure = pressure;
		this.timer = timer;
		this.toolParameterMapping = toolParameterMapping;
		this.productAlarmCondition = productAlarmCondition;
		toggleActive(false);
	}

	/**
	 * 알람 활성/비활성 전환 메서드
	 * - ON  -> activeTime = now
	 * - OFF -> activeTime = null
	 */
	public void toggleActive(
		boolean isActive
	) {
		this.isActive = isActive;
		this.activeTime = isActive
						  ? LocalDateTime.now()
						  : null;
	}

}
