package com.nextorm.common.db.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservoir_capacity")
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class ReservoirCapacity extends BaseEntity {
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "location_id")
	@Comment("저수지 위치")
	private Location location;

	@Column(name = "reservoir_capacity")
	@Comment("저수량")
	private Double reservoirCapacity;

	@Column(name = "date")
	@Comment("일시")
	private LocalDateTime date;

	@Column(name = "description")
	@Comment("설명")
	private String description;

	@Column(name = "rain_fall")
	@Comment("강수량")
	private Double rainFall;

	public void modify(
		Double reservoirCapacity,
		Double rainFall,
		LocalDateTime date,
		String description
	) {
		this.reservoirCapacity = reservoirCapacity;
		this.rainFall = rainFall;
		this.date = date;
		this.description = description;
	}
}
