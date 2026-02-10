package com.nextorm.extensions.misillan.alarm.event;

/**
 * 알람 조건 변경 이벤트
 * Service 계층에서 발행하고 CDC 캐시 등에서 구독하는 도메인 이벤트
 */
public record AlarmConditionChangedEvent(Long alarmConditionId, Operation operation, Long parameterId) {
	public enum Operation {
		UPDATE, DELETE
	}

	public static AlarmConditionChangedEvent update(
		Long alarmConditionId,
		Long parameterId
	) {
		return new AlarmConditionChangedEvent(alarmConditionId, Operation.UPDATE, parameterId);
	}

	public static AlarmConditionChangedEvent delete(
		Long alarmConditionId,
		Long parameterId
	) {
		return new AlarmConditionChangedEvent(alarmConditionId, Operation.DELETE, parameterId);
	}
}
