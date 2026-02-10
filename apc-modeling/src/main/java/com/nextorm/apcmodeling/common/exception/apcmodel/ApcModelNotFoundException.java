package com.nextorm.apcmodeling.common.exception.apcmodel;

import com.nextorm.apcmodeling.common.exception.BusinessException;

public class ApcModelNotFoundException extends BusinessException {
	public ApcModelNotFoundException() {
		super(ApcModelErrorCode.APC_MODEL_NOT_FOUND);
	}
}
