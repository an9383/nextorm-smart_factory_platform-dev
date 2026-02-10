package com.nextorm.portal.dto.reservoircapacity;

import lombok.Data;

import java.util.List;

@Data
public class ReservoirCapacityBulkDeleteRequestDto {
	private List<Long> ids;
}
