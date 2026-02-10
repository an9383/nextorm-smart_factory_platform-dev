package com.nextorm.common.db.entity;

import com.nextorm.common.db.entity.enums.OcapAlarmCondition;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Entity
@Table(name = "ocap_alarm_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@SuperBuilder
public class OcapAlarmHistory extends BaseEntity {
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ocap_alarm_id")
	private OcapAlarm ocapAlarm;

	@Column(name = "parameter_id")
	private Long parameterId;

	@Column(name = "fault_at")
	private LocalDateTime faultAt;

	@Comment("알람 발생 조건")
	@Enumerated(EnumType.STRING)
	@Column(name = "alarm_condition", columnDefinition = "varchar(20)")
	private OcapAlarmCondition alarmCondition;
}
