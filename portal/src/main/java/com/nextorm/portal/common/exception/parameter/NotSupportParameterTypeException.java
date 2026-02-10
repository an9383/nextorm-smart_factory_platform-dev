package com.nextorm.portal.common.exception.parameter;

import com.nextorm.portal.common.exception.BusinessErrorCode;
import com.nextorm.portal.common.exception.BusinessException;

public class NotSupportParameterTypeException extends BusinessException {
	public static final BusinessErrorCode errorCode = ParameterErrorCode.NOT_SUPPORT_PARAMETER_TYPE;

	public NotSupportParameterTypeException() {
		super(errorCode);
	}
}
