package com.nextorm.portal.common.exception.parameter;

import com.nextorm.portal.common.exception.BusinessErrorCode;
import com.nextorm.portal.common.exception.BusinessException;

public class ParameterNotFoundException extends BusinessException {
	public static final BusinessErrorCode errorCode = ParameterErrorCode.PARAMETER_NOT_FOUND;

	public ParameterNotFoundException() {
		super(errorCode);
	}

	public ParameterNotFoundException(Long parameterId) {
		super(errorCode);
		addExtraData("parameterId", parameterId);
	}

	public ParameterNotFoundException(
		Long toolId,
		String toolName
	) {
		super(errorCode);
		addExtraData("toolId", toolId);
		addExtraData("toolName", toolName);
	}

}
