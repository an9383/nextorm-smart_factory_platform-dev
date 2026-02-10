package com.nextorm.extensions.misillan.alarm.exception.toolparametermapping;

import com.nextorm.extensions.misillan.alarm.exception.BusinessErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ToolParameterMappingErrorCode implements BusinessErrorCode {

	TOOL_PARAMETER_MAPPING_NOT_FOUND("ERROR_MAPPING_NOT_FOUND", HttpStatus.NOT_FOUND, "매핑을 찾을 수 업습니다");

	private final String code;
	private final HttpStatus httpStatus;
	private final String message;
}
