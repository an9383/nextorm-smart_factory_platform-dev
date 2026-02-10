package com.nextorm.portal.dto.summary;

import com.nextorm.common.db.entity.ParameterData;
import com.nextorm.common.db.entity.SummaryData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Data
@AllArgsConstructor
@Builder
public class FastTrendResponseDto {
	private String type;
	private List<ParameterTrend> parameters;

	public static FastTrendResponseDto from(
		String type,
		List<ParameterTrend> parameters
	) {
		return FastTrendResponseDto.builder()
								   .type(type)
								   .parameters(parameters)
								   .build();
	}

	@Data
	@AllArgsConstructor
	public static class ParameterTrend {
		private String name;
		private List<Item> items = new ArrayList<>();

		public static ParameterTrend fromSummaryData(
			String name,
			List<SummaryData> summaryDataList
		) {
			List<Item> summaryItems = summaryDataList.stream()
													 .flatMap(it -> {
														 final LocalDateTime sumEndBaseAt = it.getSumEndBaseAt();
														 return Stream.of(it.getStartValue(),
																		  it.getEndValue(),
																		  it.getMin(),
																		  it.getMax(),
																		  it.getAvg())
																	  .map(value -> new Item(sumEndBaseAt, value));
													 })
													 .toList();
			return new ParameterTrend(name, summaryItems);
		}

		public static ParameterTrend fromRawData(
			String name,
			List<ParameterData> parameterDataList
		) {
			List<Item> rawItems = parameterDataList.stream()
												   .map(it -> new Item(it.getTraceAt(), it.getNumberValue()))
												   .toList();

			return new ParameterTrend(name, rawItems);
		}
	}

	@Data
	@AllArgsConstructor
	public static class Item {
		private LocalDateTime time;
		private Number value;
	}
}
