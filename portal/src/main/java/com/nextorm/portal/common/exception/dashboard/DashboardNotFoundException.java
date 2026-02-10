package com.nextorm.portal.common.exception.dashboard;

import com.nextorm.portal.common.exception.BusinessException;

public class DashboardNotFoundException extends BusinessException {
	public DashboardNotFoundException() {
		super(DashboardErrorCode.DASHBOARD_NOT_FOUND);
	}

}