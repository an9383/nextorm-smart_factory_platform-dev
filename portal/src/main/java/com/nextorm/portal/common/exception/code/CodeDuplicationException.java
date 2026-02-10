package com.nextorm.portal.common.exception.code;

import com.nextorm.portal.common.exception.BusinessErrorCode;
import com.nextorm.portal.common.exception.BusinessException;

public class CodeDuplicationException extends BusinessException {
	public static final BusinessErrorCode errorCode = CodeErrorCode.CODE_DUPLICATION;

	public CodeDuplicationException(String code) {
		super(errorCode);
		addExtraData("code", code);
	}
}
