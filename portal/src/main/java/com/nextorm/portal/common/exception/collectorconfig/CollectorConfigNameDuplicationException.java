package com.nextorm.portal.common.exception.collectorconfig;

import com.nextorm.portal.common.exception.BusinessErrorCode;
import com.nextorm.portal.common.exception.BusinessException;

public class CollectorConfigNameDuplicationException extends BusinessException {
	public static final BusinessErrorCode errorCode = CollectorConfigErrorCode.COLLECT_CONFIG_NAME_DUPLICATION;

	public CollectorConfigNameDuplicationException(String collectorConfigName) {
		super(errorCode);
		addExtraData("collectorConfigName", collectorConfigName);
	}
}
