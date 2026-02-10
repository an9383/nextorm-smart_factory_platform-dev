package com.nextorm.portal.dto.summary;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class FastTrendRequestDto {
	private List<Long> parameterIds = new ArrayList<>();
	private LocalDateTime from;
	private LocalDateTime to;
	private Integer chartWidth;
}
