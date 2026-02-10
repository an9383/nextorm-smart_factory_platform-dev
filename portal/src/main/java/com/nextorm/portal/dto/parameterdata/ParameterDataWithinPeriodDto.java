package com.nextorm.portal.dto.parameterdata;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nextorm.common.db.entity.Parameter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ParameterDataWithinPeriodDto {
	private Long id;
	private Long parameterId;
	private String name;
	private String toolName;
	private Parameter.DataType dataType;
	private Object value;
	private String unit;
	private Boolean isSpecLimitOver;
	private LocalDateTime traceAt;

	public static ParameterDataWithinPeriodDto of(
		Long dataId,
		Long parameterId,
		String name,
		String toolName,
		Parameter.DataType dataType,
		Object value,
		String unit,
		Boolean isSpecLimitOver,
		LocalDateTime traceAt
	) {
		return ParameterDataWithinPeriodDto.builder()
										   .id(dataId)
										   .parameterId(parameterId)
										   .name(name)
										   .toolName(toolName)
										   .dataType(dataType)
										   .value(value)
										   .unit(unit)
										   .isSpecLimitOver(isSpecLimitOver)
										   .traceAt(traceAt)
										   .build();
	}
}
