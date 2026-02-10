package com.nextorm.portal.dto.location;

import lombok.Data;

import java.util.List;

@Data
public class LocationBulkDeleteRequestDto {
	private List<Long> ids;
}
