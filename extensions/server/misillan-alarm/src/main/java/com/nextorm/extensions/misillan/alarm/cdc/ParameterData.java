package com.nextorm.extensions.misillan.alarm.cdc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
public class ParameterData {
	public enum DataType {
		DOUBLE, INTEGER,
	}

	@JsonProperty("parameter_id")
	private Long parameterId;
	@JsonProperty("data_type")
	private DataType dataType;

	@JsonProperty("d_value")
	private Double dValue;
	@JsonProperty("s_value")
	private String sValue;
	@JsonProperty("i_value")
	private Integer iValue;

	@JsonProperty("trace_at")
	private LocalDateTime traceAt;

	public void setTraceAt(long microSecond) {
		Instant inst = Instant.ofEpochMilli(microSecond / 1000L);
		this.traceAt = LocalDateTime.ofInstant(inst, ZoneId.of("UTC"));
	}

	public Double getValue() {
		return switch (dataType) {
			case DOUBLE -> dValue;
			case INTEGER -> iValue != null
							? iValue.doubleValue()
							: null;
			default -> null;
		};
	}
}