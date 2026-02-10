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
public class ShortForcecastWeatherResponseDto {
	private String fcstDate;
	private String fcstTime;
	private String temp1Hour;
	private double rain1Hour;
	private double snow1Hour;
	private String rainPercent;
	private SkyCondition skyCondition;
	private String eastWestWind;
	private String northSouthWind;
	private String humidity;
	private PrecipitationFoam precipitationFoam;
	private WindDirection directionWind;
	private String windSpeed;
	private String wave;
	private String dayMinTemp;
	private String dayMaxTemp;

}
