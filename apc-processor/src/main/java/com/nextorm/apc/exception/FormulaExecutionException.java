package com.nextorm.apc.exception;

import com.nextorm.common.apc.constant.ApcErrorCode;

public class FormulaExecutionException extends BusinessException {
	private static final ApcErrorCode ERROR_CODE = ApcErrorCode.FORMULA_EXECUTION_ERROR;

	public FormulaExecutionException(String message) {
		super(message, ERROR_CODE);
	}

	public FormulaExecutionException(Throwable cause) {
		super(cause, ERROR_CODE);
	}
}
