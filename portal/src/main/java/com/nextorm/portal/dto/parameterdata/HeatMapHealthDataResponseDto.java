package com.nextorm.portal.dto.parameterdata;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class HeatMapHealthDataResponseDto {
	private double healthScore;
	private double latitude;
	private double longitude;

	private List<HealthData> healthData = List.of();

	@Getter
	@Builder
	public static class HealthData {
		private String parameterName;
		private int healthScore;
	}

	public static HeatMapHealthDataResponseDto of(
		GPSCoordinateDto gps,
		List<HealthData> healthDatas
	) {
		return HeatMapHealthDataResponseDto.builder()
										   .latitude(gps.getLatitude())
										   .longitude(gps.getLongitude())
										   .healthData(healthDatas)
										   .healthScore(healthDatas.stream()
																   .mapToInt(HeatMapHealthDataResponseDto.HealthData::getHealthScore)
																   .min()
																   .orElse(0))
										   .build();
	}

	public static HealthData of(
		int healthScore,
		String parameterName
	) {
		return HeatMapHealthDataResponseDto.HealthData.builder()
													  .parameterName(parameterName)
													  .healthScore(healthScore)
													  .build();
	}
}
