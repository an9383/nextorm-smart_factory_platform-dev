package com.nextorm.portal.dto.weather.enums;

public enum PrecipitationFoam {
	NONE, RAINY, RAINYSNOW, SNOW, SUDDENSHOWER, RAINDROP, RAINDROPSNOWFLAKE, SNOWFLAKE;

	public static PrecipitationFoam of(
		String value
	) {
		return switch (value) {
			case "1" -> RAINY;
			case "2" -> RAINYSNOW;
			case "3" -> SNOW;
			case "4" -> SUDDENSHOWER;
			case "5" -> RAINDROP;
			case "6" -> RAINDROPSNOWFLAKE;
			case "7" -> SNOWFLAKE;
			default -> NONE;
		};
	}
}