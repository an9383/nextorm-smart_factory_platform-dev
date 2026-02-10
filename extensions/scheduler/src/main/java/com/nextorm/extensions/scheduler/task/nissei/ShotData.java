package com.nextorm.extensions.scheduler.task.nissei;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 샷 데이터 모델
 * CSV 파일에서 읽어온 샷 데이터를 표현하는 불변 객체
 */
public record ShotData(LocalDate date, LocalTime time, int shotCount) {
	/**
	 * 날짜와 시간을 결합하여 LocalDateTime 반환
	 *
	 * @return 결합된 LocalDateTime
	 */
	public LocalDateTime traceDt() {
		return LocalDateTime.of(date, time);
	}
}
