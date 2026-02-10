package com.nextorm.common.db.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Objects;

@Entity
@Table(name = "reservoir_layout")
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ReservoirLayout extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tool_id")
	private Tool tool;

	@Column(name = "data", columnDefinition = "LONGTEXT")
	private String data;

	public void modify(String data) {
		if (Objects.nonNull(data)) {
			this.data = data;
		}
	}
}
