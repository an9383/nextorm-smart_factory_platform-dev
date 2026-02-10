package com.nextorm.extensions.misillan.alarm.exception.eqms;

import com.nextorm.extensions.misillan.alarm.exception.BusinessErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
@RequiredArgsConstructor
@Getter
public enum EqmsToolProductErrorCode implements BusinessErrorCode {
	EQMS_TOOL_NOT_FOUND("ERROR_EQMS_TOOL_NOT_FOUND", HttpStatus.NOT_FOUND, "EQMS Tool을 찾을 수 없습니다."),

	EQMS_PRODUCT_NOT_FOUND("ERROR_EQMS_PRODUCT_NOT_FOUND", HttpStatus.NOT_FOUND, "EQMS Product를 찾을 수 없습니다.");


	private final String code;
	private final HttpStatus httpStatus;
	private final String message;


}
