package com.nextorm.extensions.misillan.alarm.exception;

import org.springframework.http.HttpStatus;

public interface BusinessErrorCode {
	String getCode();

	HttpStatus getHttpStatus();

	String getMessage();
}
