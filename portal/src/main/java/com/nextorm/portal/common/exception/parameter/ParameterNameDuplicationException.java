package com.nextorm.portal.common.exception.parameter;

import com.nextorm.portal.common.exception.BusinessErrorCode;
import com.nextorm.portal.common.exception.BusinessException;

public class ParameterNameDuplicationException extends BusinessException {
	public static final BusinessErrorCode errorCode = ParameterErrorCode.PARAMETER_NAME_DUPLICATION;

	public ParameterNameDuplicationException(String parameterName) {
		super(errorCode);
		addExtraData("parameterName", parameterName);
	}
}
