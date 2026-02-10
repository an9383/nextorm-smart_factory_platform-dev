package com.nextorm.common.db.exception.jsonconverter;

import com.nextorm.common.db.exception.BusinessErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum JsonConverterErrorCode implements BusinessErrorCode {

	JSON_CONVERTER_ERROR(HttpStatus.CONFLICT, "ERROR_JSON_CONVERTER_ERROR", "JSON 변환 중 오류가 발생했습니다.");

	private final HttpStatus httpStatus;
	private final String message;
	private final String code;
}
