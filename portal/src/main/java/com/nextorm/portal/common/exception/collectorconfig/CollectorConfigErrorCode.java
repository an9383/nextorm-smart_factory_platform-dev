package com.nextorm.portal.common.exception.collectorconfig;

import com.nextorm.portal.common.exception.BusinessErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CollectorConfigErrorCode implements BusinessErrorCode {
	COLLECT_CONFIG_NAME_DUPLICATION(HttpStatus.CONFLICT,
		"ERROR_COLLECTOR_CONFIG_NAME_DUPLICATION",
		"같은 이름의 Collector가 존재합니다. 중복되는 이름: {collectorConfigName}");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
