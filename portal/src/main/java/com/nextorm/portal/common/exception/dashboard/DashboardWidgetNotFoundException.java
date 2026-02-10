package com.nextorm.portal.common.exception.dashboard;

import com.nextorm.portal.common.exception.BusinessException;

public class DashboardWidgetNotFoundException extends BusinessException {
	public DashboardWidgetNotFoundException() {
		super(DashboardErrorCode.DASHBOARD_WIDGET_NOT_FOUND);
	}

}