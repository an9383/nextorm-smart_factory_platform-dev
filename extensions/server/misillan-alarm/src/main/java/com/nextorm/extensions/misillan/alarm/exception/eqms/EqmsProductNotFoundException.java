package com.nextorm.extensions.misillan.alarm.exception.eqms;

import com.nextorm.extensions.misillan.alarm.exception.BusinessException;

public class EqmsProductNotFoundException extends BusinessException {
	public EqmsProductNotFoundException() {
		super(EqmsToolProductErrorCode.EQMS_PRODUCT_NOT_FOUND);
	}
}
