package com.nextorm.common.db.entity;

import com.nextorm.common.db.entity.enums.OcapAlarmNotificationType;
import com.nextorm.common.db.entity.enums.OcapRecipientType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "ocap_alarm_recipient")
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class OcapAlarmRecipient extends BaseEntity {
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ocap_alarm_id")
	private OcapAlarm ocapAlarm;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "ocap_alarm_notification_type", columnDefinition = "varchar(20)", nullable = false)
	@Comment("알림 타입 선택(이메일, 문자, SNS)")
	@Enumerated(EnumType.STRING)
	private OcapAlarmNotificationType ocapAlarmNotificationType;

	public static OcapAlarmRecipient create(
		OcapAlarm ocapAlarm,
		Long userId,
		String notificationType
	) {
		return OcapAlarmRecipient.builder()
								 .ocapAlarm(ocapAlarm)
								 .userId(userId)
								 .ocapAlarmNotificationType(OcapAlarmNotificationType.valueOf(notificationType))
								 .build();
	}
}
