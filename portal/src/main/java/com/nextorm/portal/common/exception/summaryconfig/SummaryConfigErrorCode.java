package com.nextorm.portal.common.exception.summaryconfig;

import com.nextorm.portal.common.exception.BusinessErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SummaryConfigErrorCode implements BusinessErrorCode {
	SUMMARY_NAME_DUPLICATION(HttpStatus.CONFLICT,
		"ERROR_SUMMARY_NAME_DUPLICATION",
		"이름이 중복됩니다. 중복된 이름: {summaryConfigName}");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
