package com.nextorm.apc.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimulationResponseDto {
	private Long apcRequestId;
	@JsonProperty("success")
	private boolean success;

	public static SimulationResponseDto success(Long apcRequestId) {
		return new SimulationResponseDto(apcRequestId, true);
	}

	public static SimulationResponseDto fail(Long apcRequestId) {
		return new SimulationResponseDto(apcRequestId, false);
	}
}
