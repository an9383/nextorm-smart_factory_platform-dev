package com.nextorm.portal.common.exception.processconfig;

import com.nextorm.portal.common.exception.BusinessErrorCode;
import com.nextorm.portal.common.exception.BusinessException;

public class ProcessConfigNotFoundException extends BusinessException {
	public static final BusinessErrorCode errorCode = ProcessConfigErrorCode.PROCESS_CONFIG_NOT_FOUND;

	public ProcessConfigNotFoundException() {
		super(errorCode);
	}

}
