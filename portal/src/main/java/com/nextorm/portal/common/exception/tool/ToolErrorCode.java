package com.nextorm.portal.common.exception.tool;

import com.nextorm.portal.common.exception.BusinessErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ToolErrorCode implements BusinessErrorCode {
	TOOL_NAME_DUPLICATION(HttpStatus.CONFLICT,
		"ERROR_TOOL_NAME_DUPLICATION",
		"이름이 중복됩니다. 중복되는 이름: {toolName}"), TOOL_NOT_FOUND(HttpStatus.NOT_FOUND,
		"ERROR_TOOL_NOT_FOUND",
		"설비 정보를 찾을 수 없습니다."), RELATE_TOOL_NOT_FOUND(HttpStatus.UNPROCESSABLE_ENTITY,
		"ERROR_RELATE_TOOL_NOT_FOUND",
		"설비 정보를 찾을 수 없습니다."), TOOL_NAME_VALID_FAIL(HttpStatus.BAD_REQUEST,
		"ERROR_TOOL_NAME_VALID_FAIL",
		"설비 이름은 영문,숫자,하이픈,언더스코어만 가능합니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
