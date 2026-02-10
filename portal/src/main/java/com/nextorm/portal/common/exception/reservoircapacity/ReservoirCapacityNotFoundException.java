package com.nextorm.portal.common.exception.reservoircapacity;

import com.nextorm.portal.common.exception.BusinessErrorCode;
import com.nextorm.portal.common.exception.BusinessException;

public class ReservoirCapacityNotFoundException extends BusinessException {
	public static final BusinessErrorCode errorCode = ReservoirCapacityErrorCode.RESERVOIR_CAPACITY_NOT_FOUND;

	public ReservoirCapacityNotFoundException() {
		super(errorCode);
	}
}
