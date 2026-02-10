package com.nextorm.portal.common.exception.jsonconverter;

import com.nextorm.portal.common.exception.BusinessException;

public class JsonConverterException extends BusinessException {
	public JsonConverterException() {
		super(JsonConverterErrorCode.JSON_CONVERTER_ERROR);
	}
}
