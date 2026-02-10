package com.nextorm.portal.dto.parameterdata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class RecentParameterDataResponseDto {
	private Long id;
	private String parameterName;
	private Object value;
	private LocalDateTime traceAt;

	public static RecentParameterDataResponseDto of(
		Long dataId,
		String parameterName,
		Object value,
		LocalDateTime traceAt
	) {
		return RecentParameterDataResponseDto.builder()
											 .id(dataId)
											 .parameterName(parameterName)
											 .value(value)
											 .traceAt(traceAt)
											 .build();
	}
}
