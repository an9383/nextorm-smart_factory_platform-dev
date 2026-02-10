package com.nextorm.portal.common.exception.summarydata;

import com.nextorm.portal.common.exception.BusinessErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SummaryDataErrorCode implements BusinessErrorCode {
	SUMMARY_DATA_PDF_GENERATE_FAIL(HttpStatus.UNPROCESSABLE_ENTITY,
		"ERROR_SUMMARY_DATA_PDF_GENERATE_FAIL",
		"PDF 파일 생성에 실패했습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
