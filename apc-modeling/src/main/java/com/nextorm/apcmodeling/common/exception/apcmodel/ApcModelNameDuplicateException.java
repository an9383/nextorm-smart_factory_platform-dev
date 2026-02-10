package com.nextorm.apcmodeling.common.exception.apcmodel;

import com.nextorm.apcmodeling.common.exception.BusinessException;

public class ApcModelNameDuplicateException extends BusinessException {
	public ApcModelNameDuplicateException(String name) {
		super(ApcModelErrorCode.APC_MODEL_NAME_DUPLICATE);
		addExtraData("name", name);
	}
}
