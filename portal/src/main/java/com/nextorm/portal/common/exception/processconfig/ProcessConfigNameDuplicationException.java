package com.nextorm.portal.common.exception.processconfig;

import com.nextorm.portal.common.exception.BusinessErrorCode;
import com.nextorm.portal.common.exception.BusinessException;

public class ProcessConfigNameDuplicationException extends BusinessException {
	public static final BusinessErrorCode errorCode = ProcessConfigErrorCode.PROCESS_CONFIG_DUPLICATION;

	public ProcessConfigNameDuplicationException(String processConfigName) {
		super(errorCode);
		addExtraData("processConfigName", processConfigName);
	}

}
