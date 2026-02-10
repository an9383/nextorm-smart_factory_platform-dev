package com.nextorm.portal.config.security;

import lombok.Getter;

/**
 * JWT 인증 관련 Exception
 */
@Getter
public class JwtAuthenticationException extends RuntimeException {
	public static final String ATTRIBUTE_KEY = "Exception";

	private final JwtAuthenticationError error;

	public JwtAuthenticationException(JwtAuthenticationError error) {
		this.error = error;
	}

	public JwtAuthenticationException(
		JwtAuthenticationError error,
		Throwable cause
	) {
		super(cause);
		this.error = error;
	}
}
