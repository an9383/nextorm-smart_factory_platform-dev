package com.nextorm.processor.event.application;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public class FaultEvent extends ApplicationEvent {
	private final Message message;

	public FaultEvent(
		Object source,
		Message message
	) {
		super(source);
		this.message = message;
	}

	@Builder
	@Getter
	@ToString
	public static class Message {
		private Long parameterId;
		private boolean isControlSpecOver;
		private boolean isSpecOver;
		private LocalDateTime traceAt;
	}
}
