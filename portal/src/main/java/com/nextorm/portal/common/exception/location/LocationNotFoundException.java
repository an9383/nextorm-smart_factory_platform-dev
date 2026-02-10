package com.nextorm.portal.common.exception.location;

import com.nextorm.portal.common.exception.BusinessException;

public class LocationNotFoundException extends BusinessException {
	public LocationNotFoundException() {
		super(LocationErrorCode.LOCATION_NOT_FOUND);
	}
}
