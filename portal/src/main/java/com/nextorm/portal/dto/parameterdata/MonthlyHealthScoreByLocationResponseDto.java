package com.nextorm.portal.dto.parameterdata;

import lombok.Builder;
import lombok.Data;

import java.time.YearMonth;
import java.util.List;

@Data
@Builder
public class MonthlyHealthScoreByLocationResponseDto {
	private YearMonth yearMonth;
	private int score;
	private double latitude;
	private double longitude;
	private List<HealthData> healthData;

	@Data
	@Builder
	public static class HealthData {
		private int healthScore;
		private String parameterName;
	}

	public static MonthlyHealthScoreByLocationResponseDto of(
		YearMonth yearMonth,
		int score,
		GPSCoordinateDto gps,
		List<HealthData> healthData
	) {
		return MonthlyHealthScoreByLocationResponseDto.builder()
													  .yearMonth(yearMonth)
													  .score(score)
													  .latitude(gps.getLatitude())
													  .longitude(gps.getLongitude())
													  .healthData(healthData)
													  .build();
	}

	public static HealthData of(
		int healthScore,
		String parameterName
	) {
		return MonthlyHealthScoreByLocationResponseDto.HealthData.builder()
																 .healthScore(healthScore)
																 .parameterName(parameterName)
																 .build();
	}
}
