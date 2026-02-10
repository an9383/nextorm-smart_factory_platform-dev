package com.nextorm.portal.common.exception.code;

import com.nextorm.portal.common.exception.BusinessErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CodeErrorCode implements BusinessErrorCode {
	CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "ERROR_CODE_NOT_FOUND_EXCEPTION", "코드 정보를 찾을 수 없습니다."), CODE_DUPLICATION(
		HttpStatus.CONFLICT,
		"ERROR_CODE_CATEGORY_DUPLICATION",
		"코드가 중복됩니다, 중복된 코드: {code}");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
