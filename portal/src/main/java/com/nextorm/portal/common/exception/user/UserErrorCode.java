package com.nextorm.portal.common.exception.user;

import com.nextorm.portal.common.exception.BusinessErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements BusinessErrorCode {
	USER_NOT_FOUND(HttpStatus.NOT_FOUND,
		"ERROR_USER_NOT_FOUND",
		"회원을 찾을 수 없습니다."), USER_ID_DUPLICATION(HttpStatus.CONFLICT,
		"ERROR_USER_ID_DUPLICATION",
		"중복된 ID 입니다. 중복된 ID: {userId}");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
