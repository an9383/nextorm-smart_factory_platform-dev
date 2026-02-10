package com.nextorm.extensions.misillan.alarm.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Data
@ToString(exclude = "alarmHistory")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "alarm_read_user", uniqueConstraints = @UniqueConstraint(columnNames = {"alarm_id", "user_id"}))
public class AlarmReadUser {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "alarm_id", nullable = false)
	private AlarmHistory alarmHistory;

	@Column(name = "user_id", nullable = false)
	private Long userId;
}