package com.nextorm.common.db.exception.jsonconverter;

import com.nextorm.common.db.exception.BusinessException;

public class JsonConverterException extends BusinessException {
	public JsonConverterException() {
		super(JsonConverterErrorCode.JSON_CONVERTER_ERROR);
	}
}
