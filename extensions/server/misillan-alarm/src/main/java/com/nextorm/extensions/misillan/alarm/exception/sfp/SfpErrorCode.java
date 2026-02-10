package com.nextorm.extensions.misillan.alarm.exception.sfp;

import com.nextorm.extensions.misillan.alarm.exception.BusinessErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SfpErrorCode implements BusinessErrorCode {
	SFP_PARAMETER_NOT_FOUND("ERROR_SFP_PARAMETER_NOT_FOUND", HttpStatus.NOT_FOUND, "SFP parameter를 찾을 수 없습니다.");


	private final String code;
	private final HttpStatus httpStatus;
	private final String message;

}
