package com.nextorm.apc.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessResponseDto {
	private Long apcRequestId;
	@JsonProperty("success")
	private boolean success;

	public static ProcessResponseDto success(Long apcRequestId) {
		return new ProcessResponseDto(apcRequestId, true);
	}

	public static ProcessResponseDto fail(Long apcRequestId) {
		return new ProcessResponseDto(apcRequestId, false);
	}
}
