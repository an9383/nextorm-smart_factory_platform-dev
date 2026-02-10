package com.nextorm.portal.common.exception.processconfig;

import com.nextorm.portal.common.exception.BusinessErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProcessConfigErrorCode implements BusinessErrorCode {

	PROCESS_CONFIG_DUPLICATION(HttpStatus.CONFLICT,
		"ERROR_PROCESS_CONFIG_NAME_DUPLICATION",
		"이름이 중복 됩니다. 중복된 이름: {processConfigName}"), PROCESS_CONFIG_NOT_FOUND(HttpStatus.NOT_FOUND,
		"ERROR_PROCESS_CONFIG_NOT_FOUND",
		"Process Id를 찾을 수 없습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
