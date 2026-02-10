package com.nextorm.portal.dto.parameterdata;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class MonthlyHealthDataDto {
	private int month;
	private List<Item> parameters;
	private boolean isForecast;

	@Getter
	@Builder
	public static class Item {
		private String name;
		private double score;
	}

	public static MonthlyHealthDataDto of(
		int month,
		Map<String, Double> parameterScore
	) {
		return MonthlyHealthDataDto.builder()
								   .month(month)
								   .parameters(parameterScore.entrySet()
															 .stream()
															 .map(nameEntry -> Item.builder()
																				   .name(nameEntry.getKey())
																				   .score(nameEntry.getValue()
																								   .intValue())
																				   .build())
															 .toList())
								   .isForecast(false)
								   .build();
	}

}
