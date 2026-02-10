package com.nextorm.portal.dto.parameter;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MappingParameterDto {
	private Long id;
	@JsonProperty("isUsingCalculation")
	private boolean isUsingCalculation;
}
