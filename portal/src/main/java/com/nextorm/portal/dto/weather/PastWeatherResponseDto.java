package com.nextorm.portal.dto.weather;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PastWeatherResponseDto {
	private String stationName;
	private String date;
	private String avgTemp;
	private String minTemp;
	private String maxTemp;
	private String avgWind;
	private String maxWind;
	private double sumRain;
}
