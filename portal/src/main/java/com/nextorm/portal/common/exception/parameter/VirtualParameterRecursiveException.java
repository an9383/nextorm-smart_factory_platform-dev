package com.nextorm.portal.common.exception.parameter;

import com.nextorm.portal.common.exception.BusinessException;

public class VirtualParameterRecursiveException extends BusinessException {
	public VirtualParameterRecursiveException() {
		super(ParameterErrorCode.VIRTUAL_PARAMETER_RECURSIVE);
	}

}
