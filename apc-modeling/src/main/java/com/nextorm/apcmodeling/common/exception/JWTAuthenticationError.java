package com.nextorm.apcmodeling.common.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * JWT 관련 에러 응답 유형
 */
@RequiredArgsConstructor
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum JWTAuthenticationError {
	ID_PASSWORD_INVALID(HttpServletResponse.SC_UNAUTHORIZED, "ID or password is invalid."),

	ACCESS_TOKEN_EXPIRED(HttpServletResponse.SC_UNAUTHORIZED, "Access token is expired."),

	REFRESH_TOKEN_EXPIRED(HttpServletResponse.SC_UNAUTHORIZED, "Refresh token is expired."),

	TOKEN_INVALID(HttpServletResponse.SC_UNAUTHORIZED, "Token is invalid.");

	@JsonIgnore
	private final int httpStatusCode; //HTTP 상태 코드
	private final String message;

	public String getStatus() {
		return this.name();
	}
}
