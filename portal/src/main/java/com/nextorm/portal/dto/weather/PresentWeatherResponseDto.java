package com.nextorm.portal.dto.weather;

import com.nextorm.portal.dto.weather.enums.PrecipitationFoam;
import com.nextorm.portal.dto.weather.enums.WindDirection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class PresentWeatherResponseDto {

	private PrecipitationFoam precipitationFoam;
	private String humidity;
	private double rain1Hour;
	private String temperature;
	private String eastWestWind;
	private WindDirection directionWind;
	private String northSouthWind;
	private String windSpeed;
}
