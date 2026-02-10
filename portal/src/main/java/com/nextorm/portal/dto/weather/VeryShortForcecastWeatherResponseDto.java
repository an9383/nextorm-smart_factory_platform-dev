package com.nextorm.portal.dto.weather;

import com.nextorm.portal.dto.weather.enums.PrecipitationFoam;
import com.nextorm.portal.dto.weather.enums.SkyCondition;
import com.nextorm.portal.dto.weather.enums.WindDirection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class VeryShortForcecastWeatherResponseDto {
	private String fcstTime;
	private String temperature;
	private double rain1Hour;
	private SkyCondition skyCondition;
	private String eastWestWind;
	private String northSouthWind;
	private String humidity;
	private PrecipitationFoam precipitationFoam;
	private String lightning;
	private WindDirection directionWind;
	private String windSpeed;
}
