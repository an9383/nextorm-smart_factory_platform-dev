package com.nextorm.common.db.entity;

import com.nextorm.common.db.entity.system.code.Code;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "ocap_alarm")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@SuperBuilder
public class OcapAlarm extends BaseEntity {
	private String name;

	@Comment("컨트롤 스펙오버 알림 사용여부")
	@Column(name = "is_alert_control_spec_over")
	private boolean isAlarmControlSpecOver;

	@Comment("스펙오버 알림 사용여부")
	@Column(name = "is_alert_spec_over")
	private boolean isAlarmSpecOver;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "alarm_interval_code", nullable = false)
	private Code alarmIntervalCode;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tool_id", nullable = false)
	private Tool tool;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parameter_id", nullable = false)
	private Parameter parameter;

	public static OcapAlarm create(
		String name,
		boolean isAlarmControlSpecOver,
		boolean isAlarmSpecOver,
		Code alarmIntervalCode,
		Tool tool,
		Parameter parameter
	) {
		return OcapAlarm.builder()
						.name(name)
						.isAlarmControlSpecOver(isAlarmControlSpecOver)
						.isAlarmSpecOver(isAlarmSpecOver)
						.alarmIntervalCode(alarmIntervalCode)
						.tool(tool)
						.parameter(parameter)
						.build();
	}

	public void modify(
		String name,
		boolean isAlarmControlSpecOver,
		boolean isAlarmSpecOver,
		Code alarmIntervalCode,
		Tool tool,
		Parameter parameter
	) {
		this.name = name;
		this.isAlarmControlSpecOver = isAlarmControlSpecOver;
		this.isAlarmSpecOver = isAlarmSpecOver;
		this.alarmIntervalCode = alarmIntervalCode;
		this.tool = tool;
		this.parameter = parameter;
	}
}
