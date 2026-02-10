package com.nextorm.extensions.misillan.alarm.scheduler;

import com.nextorm.extensions.misillan.alarm.cdc.AlarmDataCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 알람 조건 캐시를 주기적으로 갱신하는 스케줄러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlarmDataCacheRefreshScheduler {
	private final AlarmDataCache cache;

	@Scheduled(cron = "${scheduler.alarm-detect-condition-refresh.cron: 0 0/1 * * * *}")
	public void reCacheAll() {
		log.info("알람 감지 조건 캐시 갱신 시작");
		cache.reCacheAll();
		log.info("알람 감지 조건 캐시 갱신 종료");
	}
}
