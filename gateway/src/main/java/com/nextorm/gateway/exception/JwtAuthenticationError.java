package com.nextorm.gateway.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * JWT 관련 에러 응답 유형
 */
@RequiredArgsConstructor
@Getter
public enum JwtAuthenticationError {

	ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "ACCESS_TOKEN_EXPIRED", "Access token is expired."),

	TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "TOKEN_INVALID", "Token is invalid.");

	private final HttpStatus httpStatusCode; //HTTP 상태 코드
	private final String code;
	private final String message;
}
