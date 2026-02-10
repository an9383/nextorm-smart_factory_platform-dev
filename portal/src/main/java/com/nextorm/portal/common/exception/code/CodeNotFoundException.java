package com.nextorm.portal.common.exception.code;

import com.nextorm.portal.common.exception.BusinessErrorCode;
import com.nextorm.portal.common.exception.BusinessException;

public class CodeNotFoundException extends BusinessException {
	public static final BusinessErrorCode errorCode = CodeErrorCode.CODE_NOT_FOUND;

	public CodeNotFoundException() {
		super(errorCode);
	}

}
