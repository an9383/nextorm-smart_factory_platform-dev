package com.nextorm.portal.common.exception.dashboard;

import com.nextorm.portal.common.exception.BusinessErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DashboardErrorCode implements BusinessErrorCode {
	DASHBOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "ERROR_DASHBOARD_NOT_FOUND", "대시보드 정보를 찾을 수 없습니다."),
	DASHBOARD_WIDGET_NOT_FOUND(HttpStatus.NOT_FOUND, "ERROR_DASHBOARD_WIDGET_NOT_FOUND", "대시보드 위젯 정보를 찾을 수 없습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}