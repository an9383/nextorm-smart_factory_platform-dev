package com.nextorm.apcmodeling.common.exception;

import lombok.Getter;

/**
 * JWT 인증 관련 Exception
 */
@Getter
public class JWTAuthenticationException extends RuntimeException {
	public static final String ATTRIBUTE_KEY = "Exception";

	private final JWTAuthenticationError error;

	public JWTAuthenticationException(JWTAuthenticationError error) {
		this.error = error;
	}
}
