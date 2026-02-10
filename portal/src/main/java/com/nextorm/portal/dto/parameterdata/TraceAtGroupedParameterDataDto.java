package com.nextorm.portal.dto.parameterdata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class TraceAtGroupedParameterDataDto {
	private LocalDateTime traceAt;
	private List<ParameterDataDto> parameterDatas;
}
