package com.nextorm.portal.dto.summary;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SummaryMonthlyDataResponseDto {
	private int month;
	private List<MonthlyDataByYear> years;

	@Getter
	@Builder
	public static class MonthlyDataByYear {
		private int year;
		private double value;
	}
}
