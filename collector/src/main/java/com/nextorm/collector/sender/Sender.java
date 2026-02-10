package com.nextorm.collector.sender;

public interface Sender {
	default void send(SendMessage message) {
		throw new UnsupportedOperationException("Not implemented");
	}

	default void send(
		String topic,
		SendMessage message
	) {
		throw new UnsupportedOperationException("Not implemented");
	}
}
