package com.nextorm.portal.common.exception.summarydata;

import com.nextorm.portal.common.exception.BusinessException;

public class SummaryDataPdfGenerateFailException extends BusinessException {
	public SummaryDataPdfGenerateFailException() {
		super(SummaryDataErrorCode.SUMMARY_DATA_PDF_GENERATE_FAIL);
	}
}
