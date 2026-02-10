package com.nextorm.extensions.misillan.alarm.exception.sfp;

import com.nextorm.extensions.misillan.alarm.exception.BusinessException;

public class SfpParameterNotFoundException extends BusinessException {
	public SfpParameterNotFoundException() {
		super(SfpErrorCode.SFP_PARAMETER_NOT_FOUND);
	}
}
