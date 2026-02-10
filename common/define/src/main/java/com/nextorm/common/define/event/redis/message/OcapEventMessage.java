package com.nextorm.common.define.event.redis.message;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@ToString
public class OcapEventMessage {

	public enum Type {
		CREATE, MODIFY, DELETE
	}

	private Type type;
	private Long ocapAlarmId;
	private LocalDateTime createAt;

	public static OcapEventMessage createEvent(
		Long ocapAlarmId
	) {
		return new OcapEventMessage(Type.CREATE, ocapAlarmId, LocalDateTime.now());
	}

	public static OcapEventMessage modifyEvent(
		Long ocapAlarmId
	) {
		return new OcapEventMessage(Type.MODIFY, ocapAlarmId, LocalDateTime.now());
	}

	public static OcapEventMessage deleteEvent(
		Long ocapAlarmId
	) {
		return new OcapEventMessage(Type.DELETE, ocapAlarmId, LocalDateTime.now());
	}
}
