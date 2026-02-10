package com.nextorm.portal.common.exception.parameter;

import com.nextorm.portal.common.exception.BusinessException;

public class LotEventParameterNotFoundException extends BusinessException {
	public LotEventParameterNotFoundException() {
		super(ParameterErrorCode.LOT_EVENT_PARAMETER_NOT_FOUND);
	}
}
