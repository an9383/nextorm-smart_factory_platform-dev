package com.nextorm.apcmodeling.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApcSimulationListSearchRequestDto {
	private String condition;

	private LocalDateTime from;
	private LocalDateTime to;
}
