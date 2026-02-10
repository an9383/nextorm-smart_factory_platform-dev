package com.nextorm.portal.common.exception.location;

import com.nextorm.portal.common.exception.BusinessErrorCode;
import com.nextorm.portal.common.exception.BusinessException;

public class ParentLocationNotFoundException extends BusinessException {
	public static final BusinessErrorCode errorCode = LocationErrorCode.PARENT_LOCATION_NOT_FOUND;

	public ParentLocationNotFoundException() {
		super(errorCode);
	}
}
