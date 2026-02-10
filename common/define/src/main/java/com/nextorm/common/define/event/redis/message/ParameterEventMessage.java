package com.nextorm.common.define.event.redis.message;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@ToString
public class ParameterEventMessage {

	public enum Type {
		MODIFY
	}

	private Type type;
	private Long parameterId;
	private LocalDateTime createAt;

	public static ParameterEventMessage modifyEvent(
		Long parameterId
	) {
		return new ParameterEventMessage(Type.MODIFY, parameterId, LocalDateTime.now());
	}
}
