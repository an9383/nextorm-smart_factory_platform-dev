package com.nextorm.portal.dto.parameterdata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParameterRawDataDto<T> {
	private Long parameterId;
	private Double ucl;
	private Double lcl;
	private Double usl;
	private Double lsl;
	private LocalDateTime traceAt;
	private T value;
}
