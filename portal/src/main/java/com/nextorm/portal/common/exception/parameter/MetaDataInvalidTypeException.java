package com.nextorm.portal.common.exception.parameter;

import com.nextorm.portal.common.exception.BusinessErrorCode;
import com.nextorm.portal.common.exception.BusinessException;

public class MetaDataInvalidTypeException extends BusinessException {
	public static final BusinessErrorCode errorCode = ParameterErrorCode.META_DATA_INVALID_TYPE;

	public MetaDataInvalidTypeException() {
		super(errorCode);
	}
}