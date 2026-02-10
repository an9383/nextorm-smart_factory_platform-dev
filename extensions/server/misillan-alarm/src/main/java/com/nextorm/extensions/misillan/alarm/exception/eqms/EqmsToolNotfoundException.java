package com.nextorm.extensions.misillan.alarm.exception.eqms;

import com.nextorm.extensions.misillan.alarm.exception.BusinessException;

public class EqmsToolNotfoundException extends BusinessException {
	public EqmsToolNotfoundException() {
		super(EqmsToolProductErrorCode.EQMS_TOOL_NOT_FOUND);
	}
}
