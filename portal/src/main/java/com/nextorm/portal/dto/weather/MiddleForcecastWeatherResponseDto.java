package com.nextorm.portal.dto.weather;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MiddleForcecastWeatherResponseDto {

	private String regId;

	private String rainAfter3Am;
	private String rainAfter3Pm;
	private String rainAfter4Am;
	private String rainAfter4Pm;
	private String rainAfter5Am;
	private String rainAfter5Pm;
	private String rainAfter6Am;
	private String rainAfter6Pm;
	private String rainAfter7Am;
	private String rainAfter7Pm;
	private String rainAfter8;
	private String rainAfter9;
	private String rainAfter10;

	private String weather3Am;
	private String weather3Pm;
	private String weather4Am;
	private String weather4Pm;
	private String weather5Am;
	private String weather5Pm;
	private String weather6Am;
	private String weather6Pm;
	private String weather7Am;
	private String weather7Pm;
	private String weather8;
	private String weather9;
	private String weather10;

}
