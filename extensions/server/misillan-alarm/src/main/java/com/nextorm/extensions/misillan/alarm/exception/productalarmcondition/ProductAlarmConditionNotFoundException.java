package com.nextorm.extensions.misillan.alarm.exception.productalarmcondition;

import com.nextorm.extensions.misillan.alarm.exception.BusinessException;

public class ProductAlarmConditionNotFoundException extends BusinessException {
	public ProductAlarmConditionNotFoundException() {
		super(ProductAlarmConditionErrorCode.PRODUCT_ALARM_CONDITION_NOT_FOUND);
	}
}
