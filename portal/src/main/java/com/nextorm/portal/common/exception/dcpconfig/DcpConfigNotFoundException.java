package com.nextorm.portal.common.exception.dcpconfig;

import com.nextorm.portal.common.exception.BusinessException;

public class DcpConfigNotFoundException extends BusinessException {
	public DcpConfigNotFoundException() {
		super(DcpConfigErrorCode.DCP_CONFIG_NOT_FOUND);
	}

}
