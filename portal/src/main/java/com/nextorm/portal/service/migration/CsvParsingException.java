package com.nextorm.portal.service.migration;

public class CsvParsingException extends RuntimeException {
	private static final String MESSAGE = "CSV 파싱 중 에러가 발생하였습니다";

	public CsvParsingException(Throwable cause) {
		super(MESSAGE, cause);
	}
}
