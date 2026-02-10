package com.nextorm.portal.dto.reservoircapacity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReservoirCapacityResponseTrendDto {
	private LocalDateTime date;
	private Double reservoirCapacity;
	private Double rainFall;
	private Boolean isPredicted;
}
