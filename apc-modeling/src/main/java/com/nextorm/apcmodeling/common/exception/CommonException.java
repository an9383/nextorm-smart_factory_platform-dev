package com.nextorm.apcmodeling.common.exception;

import com.nextorm.apcmodeling.constant.ErrorCode;
import lombok.Getter;

import java.util.Map;

@Getter
public class CommonException extends RuntimeException {
	private ErrorCode errorCode;
	private Map<String, Object> extraData;

	public CommonException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

	public CommonException(
		ErrorCode errorCode,
		Map<String, Object> extraData
	) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
		this.extraData = extraData;
	}
}
