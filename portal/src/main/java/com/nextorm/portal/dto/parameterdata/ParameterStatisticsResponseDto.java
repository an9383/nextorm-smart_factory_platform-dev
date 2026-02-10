package com.nextorm.portal.dto.parameterdata;

import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class ParameterStatisticsResponseDto {
	private String name;
	private List<ChartItem> chartItems = new ArrayList<>();

	public static ParameterStatisticsResponseDto of(
		String name,
		List<ChartItem> chartItems
	) {
		return new ParameterStatisticsResponseDto(name, chartItems);
	}

	@Getter
	@AllArgsConstructor
	@Builder
	@ToString
	public static class ChartItem {
		private String date;
		private Double value;

		public static ChartItem from(
			LocalDate localDate,
			Double value
		) {
			return new ChartItem(localDate.toString(), value);
		}

		public static ChartItem from(
			String date,
			Double value
		) {
			return new ChartItem(date, value);
		}
	}
}
