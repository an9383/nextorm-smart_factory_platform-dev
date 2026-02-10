package com.nextorm.apcmodeling.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ApcTrendSearchRequestDto {
	private Long versionId;
	private LocalDateTime from;
	private LocalDateTime to;
	@JsonProperty("isIncludeSimulation")
	private boolean isIncludeSimulation;
}
