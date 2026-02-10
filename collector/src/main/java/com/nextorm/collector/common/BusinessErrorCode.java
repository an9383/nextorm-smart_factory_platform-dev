package com.nextorm.collector.common;

import org.springframework.http.HttpStatus;

public interface BusinessErrorCode {
	String getCode();

	HttpStatus getHttpStatus();

	String getMessage();
}
