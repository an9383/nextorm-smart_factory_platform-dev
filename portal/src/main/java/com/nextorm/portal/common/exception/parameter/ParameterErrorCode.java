package com.nextorm.portal.common.exception.parameter;

import com.nextorm.portal.common.exception.BusinessErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ParameterErrorCode implements BusinessErrorCode {
	PARAMETER_NOT_FOUND(HttpStatus.NOT_FOUND,
		"ERROR_PARAMETER_NOT_FOUND",
		"파라미터 정보를 찾을 수 없습니다."), RELATE_PARAMETER_NOT_FOUND(HttpStatus.UNPROCESSABLE_ENTITY,
		"ERROR_RELATE_PARAMETER_NOT_FOUND",
		"파라미터 정보를 찾을 수 없습니다."), PARAMETER_NAME_DUPLICATION(HttpStatus.CONFLICT,
		"ERROR_PARAMETER_NAME_DUPLICATION",
		"이름이 중복 됩니다. 중복된 이름: {parameterName}"), VIRTUAL_PARAMETER_RECURSIVE(HttpStatus.UNPROCESSABLE_ENTITY,
		"ERROR_VIRTUAL_PARAMETER_RECURSIVE",
		"가상 파라미터가 순환참조 됩니다."), NOT_SUPPORT_PARAMETER_TYPE(HttpStatus.UNPROCESSABLE_ENTITY,
		"ERROR_NOT_SUPPORT_PARAMETER_TYPE",
		"지원하지 않는 파라미터 타입입니다."), LOT_EVENT_PARAMETER_NOT_FOUND(HttpStatus.UNPROCESSABLE_ENTITY,
		"ERROR_LOT_EVENT_PARAMETER_NOT_FOUND",
		"LOT EVENT 파라미터를 찾을 수 없습니다."), META_DATA_INVALID_TYPE(HttpStatus.UNPROCESSABLE_ENTITY,
		"ERROR_META_DATA_INVALID_TYPE",
		"파라미터의 타입이 메타데이터가 아닙니다."), META_DATA_EMPTY_VALUE(HttpStatus.UNPROCESSABLE_ENTITY,
		"ERROR_META_DATA_EMPTY_VALUE",
		"{dataType} 타입의 메타데이터 값이 없거나 비어있습니다."), META_DATA_TYPE_MISMATCH(HttpStatus.UNPROCESSABLE_ENTITY,
		"ERROR_META_DATA_TYPE_MISMATCH",
		"메타데이터의 타입이 올바르지 않거나 값이 타입과 일치하지 않습니다: {message}");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
