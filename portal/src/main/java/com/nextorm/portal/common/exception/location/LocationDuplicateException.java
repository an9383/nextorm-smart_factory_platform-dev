package com.nextorm.portal.common.exception.location;

import com.nextorm.portal.common.exception.BusinessErrorCode;
import com.nextorm.portal.common.exception.BusinessException;

public class LocationDuplicateException extends BusinessException {
	public static final BusinessErrorCode errorCode = LocationErrorCode.LOCATION_DUPLICATION_NAME_TYPE_PARENT;

	public LocationDuplicateException(String name) {
		super(errorCode);
		addExtraData("name", name);
	}
}
