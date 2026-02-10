package com.nextorm.apcmodeling.common.exception.apcmodel;

import com.nextorm.apcmodeling.common.exception.BusinessErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ApcModelErrorCode implements BusinessErrorCode {
	APC_MODEL_NAME_DUPLICATE(HttpStatus.CONFLICT,
		"ERROR_APC_MODEL_NAME_DUPLICATE",
		"이름이 중복됩니다. 중복된 이름: {name}"), APC_MODEL_NOT_FOUND(HttpStatus.NOT_FOUND,
		"ERROR_APC_MODEL_NOT_FOUND",
		"APC Model을 찾을 수 없습니다");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
