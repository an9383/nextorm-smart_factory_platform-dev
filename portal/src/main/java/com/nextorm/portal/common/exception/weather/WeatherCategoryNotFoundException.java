package com.nextorm.portal.common.exception.weather;

import com.nextorm.portal.common.exception.BusinessException;

public class WeatherCategoryNotFoundException extends BusinessException {
	public WeatherCategoryNotFoundException() {
		super(WeatherErrorCode.WEATHER_CATEGORY_NOT_FOUND);
	}
}
