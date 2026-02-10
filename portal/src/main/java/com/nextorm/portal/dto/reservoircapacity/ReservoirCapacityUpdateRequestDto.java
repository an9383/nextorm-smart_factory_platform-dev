package com.nextorm.portal.dto.reservoircapacity;

import com.nextorm.common.db.entity.Location;
import com.nextorm.common.db.entity.ReservoirCapacity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservoirCapacityUpdateRequestDto {
	private Long locationId;
	private Double reservoirCapacity;
	private Double rainFall;
	private LocalDateTime date;
	private String description;

	public ReservoirCapacity toEntity(Location location) {
		return ReservoirCapacity.builder()
								.location(location)
								.reservoirCapacity(reservoirCapacity)
								.rainFall(rainFall)
								.date(date)
								.description(description)
								.build();
	}
}
