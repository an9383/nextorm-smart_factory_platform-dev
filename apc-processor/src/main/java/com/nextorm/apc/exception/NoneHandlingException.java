package com.nextorm.apc.exception;

import com.nextorm.common.apc.constant.ApcErrorCode;

/**
 * 비즈니스 로직상에서 핸들링하지 않는 에러는 이 클래스를 사용하여 facade 레이어 내에서 에러 기록용도로 사용합니다
 */
public class NoneHandlingException extends BusinessException {
	private static final ApcErrorCode ERROR_CODE = ApcErrorCode.INTERNAL_SERVER_ERROR;

	public NoneHandlingException() {
		super(ERROR_CODE);
	}

	public NoneHandlingException(Throwable cause) {
		super(cause, ERROR_CODE);
	}
}
