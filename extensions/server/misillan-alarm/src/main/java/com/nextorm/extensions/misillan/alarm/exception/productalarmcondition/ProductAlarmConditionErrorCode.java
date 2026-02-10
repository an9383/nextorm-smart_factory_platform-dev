package com.nextorm.extensions.misillan.alarm.exception.productalarmcondition;

import com.nextorm.extensions.misillan.alarm.exception.BusinessErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum ProductAlarmConditionErrorCode implements BusinessErrorCode {

	PRODUCT_ALARM_CONDITION_NOT_FOUND("ERROR_PRODUCT_ALARM_CONDITION_NOT_FOUND",
		HttpStatus.NOT_FOUND,
		"Product Alarm Condition을 찾을 수 없습니다.");

	private final String code;
	private final HttpStatus httpStatus;
	private final String message;

}
