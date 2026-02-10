package com.nextorm.portal.dto.reservoircapacity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReservoirCapacitySearchDto {
	private LocalDateTime startDt;
	private LocalDateTime endDt;

}
