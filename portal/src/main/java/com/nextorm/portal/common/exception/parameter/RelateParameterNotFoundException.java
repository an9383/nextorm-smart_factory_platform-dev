package com.nextorm.portal.common.exception.parameter;

import com.nextorm.portal.common.exception.BusinessErrorCode;
import com.nextorm.portal.common.exception.BusinessException;

public class RelateParameterNotFoundException extends BusinessException {
	public static final BusinessErrorCode errorCode = ParameterErrorCode.RELATE_PARAMETER_NOT_FOUND;

	public RelateParameterNotFoundException(Long parameterId) {
		super(errorCode);
		addExtraData("parameterId", parameterId);
	}

}
