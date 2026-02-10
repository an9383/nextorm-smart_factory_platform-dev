package com.nextorm.portal.common.exception.dcpconfig;

import com.nextorm.portal.common.exception.BusinessErrorCode;
import com.nextorm.portal.common.exception.BusinessException;

public class RelateDcpConfigNotFoundException extends BusinessException {
	public static final BusinessErrorCode errorCode = DcpConfigErrorCode.RELATE_DCP_CONFIG_NOT_FOUND;

	public RelateDcpConfigNotFoundException() {
		super(errorCode);
	}

	public RelateDcpConfigNotFoundException(Long dcpConfigId) {
		super(errorCode);
		addExtraData("dcpConfigId", dcpConfigId);
	}

}
