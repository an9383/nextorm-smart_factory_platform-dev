package com.nextorm.portal.dto.aidas;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ParameterDataAnalysisResponseDto {
	private Long parameterId;
	private String parameterName;
	private Double average;
	private Double min;
	private Double max;
	private List<SpecOut> specOuts = List.of();

	@Getter
	@Builder
	public static class SpecOut {
		private LocalDateTime dateTime;
		private Double lsl;
		private Double usl;
		private Double value;
	}
}
