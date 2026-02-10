package com.nextorm.portal.dto.weather.enums;

public enum SkyCondition {
	SUNNY, CLOUDY, OVERCLOUD;

	public static SkyCondition of(String value) {
		return switch (value) {
			case "3" -> CLOUDY;
			case "4" -> OVERCLOUD;
			default -> SUNNY;
		};
	}
}