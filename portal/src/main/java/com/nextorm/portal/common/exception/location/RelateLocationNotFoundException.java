package com.nextorm.portal.common.exception.location;

import com.nextorm.portal.common.exception.BusinessErrorCode;
import com.nextorm.portal.common.exception.BusinessException;

public class RelateLocationNotFoundException extends BusinessException {
	public static final BusinessErrorCode errorCode = LocationErrorCode.RELATE_LOCATION_NOT_FOUND;

	public RelateLocationNotFoundException() {
		super(errorCode);
	}
}
