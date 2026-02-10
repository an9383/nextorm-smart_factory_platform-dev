package com.nextorm.portal.common.exception.reservoircapacity;

import com.nextorm.portal.common.exception.BusinessErrorCode;
import com.nextorm.portal.common.exception.BusinessException;

public class ReservoirCapacityDuplicationException extends BusinessException {
	public static final BusinessErrorCode errorCode = ReservoirCapacityErrorCode.RESERVOIR_CAPACITY_DUPLICATION;

	public ReservoirCapacityDuplicationException() {
		super(errorCode);
	}
}
