package com.nextorm.apcmodeling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@Builder
public class ApcResultTrendResponseDto {

	Map<String, String> requestData;
	Map<String, String> requestResult;
	LocalDateTime createAt;

	public static ApcResultTrendResponseDto from(
		Map<String, String> requestMap,
		LocalDateTime createAt,
		Map<String, String> resultMap
	) {
		return ApcResultTrendResponseDto.builder()
										.requestData(requestMap)
										.requestResult(resultMap)
										.createAt(createAt)
										.build();
	}
}
