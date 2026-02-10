package com.nextorm.portal.common.exception.parameter;

import com.nextorm.portal.common.exception.BusinessErrorCode;
import com.nextorm.portal.common.exception.BusinessException;

public class MetaDataTypeMismatchException extends BusinessException {
	public static final BusinessErrorCode errorCode = ParameterErrorCode.META_DATA_TYPE_MISMATCH;

	public MetaDataTypeMismatchException(String message) {
		super(errorCode);
		addExtraData("message", message);
	}
}