package com.nextorm.extensions.proxystatusgateway.exception;

/**
 * 서버 상태 조회 중 발생하는 서비스 에러
 */
public class StatusServiceException extends RuntimeException {

	public StatusServiceException(String message) {
		super(message);
	}

	public StatusServiceException(
		String message,
		Throwable cause
	) {
		super(message, cause);
	}
}
