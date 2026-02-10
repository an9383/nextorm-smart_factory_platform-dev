package com.nextorm.apc.exception;

import com.nextorm.common.apc.constant.ApcErrorCode;

public class InvalidRequestDataException extends BusinessException {
	private static final ApcErrorCode ERROR_CODE = ApcErrorCode.INVALID_REQUEST_DATA;

	public InvalidRequestDataException() {
		super(ERROR_CODE);
	}

	public InvalidRequestDataException(Throwable cause) {
		super(cause, ERROR_CODE);
	}
}
