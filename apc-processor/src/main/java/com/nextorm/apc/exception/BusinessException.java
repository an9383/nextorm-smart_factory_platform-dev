package com.nextorm.apc.exception;

import com.nextorm.common.apc.constant.ApcErrorCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
	private final ApcErrorCode errorCode;

	public BusinessException(ApcErrorCode errorCode) {
		this.errorCode = errorCode;
	}

	public BusinessException(
		String message,
		ApcErrorCode errorCode
	) {
		super(message);
		this.errorCode = errorCode;
	}

	public BusinessException(
		Throwable cause,
		ApcErrorCode errorCode
	) {
		super(cause);
		this.errorCode = errorCode;
	}
}
