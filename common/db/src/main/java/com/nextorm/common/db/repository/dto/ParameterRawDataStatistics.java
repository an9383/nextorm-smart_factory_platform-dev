package com.nextorm.common.db.repository.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ParameterRawDataStatistics {
	private Long parameterId;
	private String parameterName;
	private Double average;
	private Double min;
	private Double max;
}
