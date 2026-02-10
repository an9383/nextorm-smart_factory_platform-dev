package com.nextorm.extensions.scheduler.task.nissei;

/**
 * CSV 파일 처리 중 발생하는 예외를 나타내는 커스텀 예외 클래스
 */
public class CsvProcessingException extends RuntimeException {

	public CsvProcessingException(String message) {
		super(message);
	}

	public CsvProcessingException(
		String message,
		Throwable cause
	) {
		super(message, cause);
	}
}
