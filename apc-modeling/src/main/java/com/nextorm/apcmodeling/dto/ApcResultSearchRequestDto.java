package com.nextorm.apcmodeling.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApcResultSearchRequestDto {
	private String condition;
	private LocalDateTime from;
	private LocalDateTime to;
	@JsonProperty("isIncludeSimulation")
	private Boolean isIncludeSimulation;
}
