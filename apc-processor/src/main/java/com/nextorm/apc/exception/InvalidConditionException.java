package com.nextorm.apc.exception;

import com.nextorm.common.apc.constant.ApcErrorCode;

public class InvalidConditionException extends BusinessException {
	private static final ApcErrorCode ERROR_CODE = ApcErrorCode.INVALID_CONDITION;

	public InvalidConditionException() {
		super(ERROR_CODE);
	}

	public InvalidConditionException(Throwable cause) {
		super(cause, ERROR_CODE);
	}
}
