package com.nextorm.portal.dto.weather;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MiddleTempWeatherResponseDto {
	private String regId;
	private String after3MinTemp;
	private String after3MaxTemp;
	private String after4MinTemp;
	private String after4MaxTemp;
	private String after5MinTemp;
	private String after5MaxTemp;
	private String after6MinTemp;
	private String after6MaxTemp;
	private String after7MinTemp;
	private String after7MaxTemp;
	private String after8MinTemp;
	private String after8MaxTemp;
	private String after9MinTemp;
	private String after9MaxTemp;
	private String after10MinTemp;
	private String after10MaxTemp;
}
