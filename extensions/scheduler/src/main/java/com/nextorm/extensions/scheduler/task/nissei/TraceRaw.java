package com.nextorm.extensions.scheduler.task.nissei;

import java.time.LocalDateTime;

/**
 * Trace Raw 데이터 모델
 */
record TraceRaw(Long parameterId, Long id, String value, LocalDateTime traceDt) {
	public boolean isEmpty() {
		return id == null || value == null || traceDt == null;
	}
}
