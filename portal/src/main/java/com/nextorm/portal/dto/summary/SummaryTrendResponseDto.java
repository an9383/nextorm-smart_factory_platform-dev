package com.nextorm.portal.dto.summary;

import com.nextorm.common.db.entity.ParameterData;
import com.nextorm.common.db.entity.SummaryData;
import com.nextorm.portal.enums.SummaryDataKind;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class SummaryTrendResponseDto {
	private String type;
	private List<ParameterTrend> parameters;

	public static SummaryTrendResponseDto from(
		String type,
		List<ParameterTrend> parameters
	) {
		return SummaryTrendResponseDto.builder()
									  .type(type)
									  .parameters(parameters)
									  .build();
	}

	@Data
	@AllArgsConstructor
	public static class ParameterTrend {
		private String name;
		private List<Item> items = new ArrayList<>();
		private List<Item> rawItems = new ArrayList<>();

		// public static ParameterTrend fromSummaryData(
		// 	String name,
		// 	List<SummaryData> summaryDataList,
		// 	SummaryDataKind dataKind
		// ) {
		// 	return new ParameterTrend(name,
		// 		summaryDataList.stream()
		// 					   .map(it -> new Item(it.getTrxStartAt(), dataKind.fromValue(it)))
		// 					   .toList(),
		// 		List.of());
		// }

		public static ParameterTrend fromSummaryWithRawData(
			String name,
			List<SummaryData> summaryDataList,
			SummaryDataKind dataKind,
			List<ParameterData> parameterDataList
		) {
			List<Item> summaryItems = summaryDataList.stream()
													 .map(it -> new Item(it.getSumEndBaseAt(), dataKind.fromValue(it)))
													 .toList();

			List<Item> rawItems = parameterDataList.stream()
												   .map(it -> new Item(it.getTraceAt(), it.getNumberValue()))
												   .toList();

			return new ParameterTrend(name, summaryItems, rawItems);
		}
	}

	@Data
	@AllArgsConstructor
	public static class Item {
		private LocalDateTime time;
		private Number value;
	}
}
