package com.nextorm.extensions.misillan.alarm.cdc;

import com.nextorm.extensions.misillan.alarm.event.AlarmConditionChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 알람 조건 변경 이벤트를 처리하여 CDC 캐시를 업데이트하는 핸들러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlarmConditionChangeEventHandler {

	private final AlarmDataCache cache;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleAlarmConditionChanged(AlarmConditionChangedEvent event) {
		switch (event.operation()) {
			// 수정 시 캐시 업데이트
			case UPDATE -> cache.updateDetectCondition(event.alarmConditionId());
			// 삭제 시 캐시에서 제거
			case DELETE -> cache.removeByParameterId(event.parameterId());
			default -> log.warn("Unknown operation: {}", event.operation());
		}
	}
}
