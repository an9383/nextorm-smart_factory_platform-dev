package com.nextorm.collector.sender;

public class KafkaConnectionFailException extends RuntimeException {
	public KafkaConnectionFailException(
		String message,
		Throwable cause
	) {
		super(message, cause);
	}
}
