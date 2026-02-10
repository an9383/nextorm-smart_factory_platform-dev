package com.nextorm.common.db.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Table(name = "tool_status")
@Getter
public class ToolStatus extends BaseEntity {
	@Column(name = "tool_status_date")
	private LocalDateTime toolStatusDate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tool_id")
	private Tool tool;

	@Column(name = "lead_time")
	private int leadTime;

	@Column(name = "operation_rate")
	private double operationRate;

}
