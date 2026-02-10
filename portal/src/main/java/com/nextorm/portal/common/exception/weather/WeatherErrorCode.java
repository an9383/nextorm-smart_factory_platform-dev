package com.nextorm.portal.common.exception.weather;

import com.nextorm.portal.common.exception.BusinessErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum WeatherErrorCode implements BusinessErrorCode {
	WEATHER_CATEGORY_NOT_FOUND(HttpStatus.UNPROCESSABLE_ENTITY,
		"ERROR_WEATHER_CATEGORY_NOT_FOUND",
		"날씨 카테고리를 찾을 수 없습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
