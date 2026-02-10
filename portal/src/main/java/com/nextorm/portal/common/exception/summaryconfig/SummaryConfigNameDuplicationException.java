package com.nextorm.portal.common.exception.summaryconfig;

import com.nextorm.portal.common.exception.BusinessErrorCode;
import com.nextorm.portal.common.exception.BusinessException;

public class SummaryConfigNameDuplicationException extends BusinessException {
	public static final BusinessErrorCode errorCode = SummaryConfigErrorCode.SUMMARY_NAME_DUPLICATION;

	public SummaryConfigNameDuplicationException(String summaryConfigName) {
		super(errorCode);
		addExtraData("summaryConfigName", summaryConfigName);
	}
}
