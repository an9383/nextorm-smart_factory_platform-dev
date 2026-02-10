package com.nextorm.portal.common.exception.parameter;

import com.nextorm.portal.common.exception.BusinessErrorCode;
import com.nextorm.portal.common.exception.BusinessException;

public class MetaDataEmptyValueException extends BusinessException {
	public static final BusinessErrorCode errorCode = ParameterErrorCode.META_DATA_EMPTY_VALUE;

	public MetaDataEmptyValueException(String dataType) {
		super(errorCode);
		addExtraData("dataType", dataType);
	}
}