package com.nextorm.extensions.misillan.alarm.scheduler;

import com.nextorm.extensions.misillan.alarm.service.SfpDataSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SfpSyncScheduler {
	private final SfpDataSyncService syncService;

	@Scheduled(cron = "${scheduler.sfp-sync.cron: 0 0/1 * * * *}")
	public void syncParameter() {
		log.info("SFP 파라미터 동기화 시작");
		syncService.syncParameter();
		log.info("SFP 파라미터 동기화 종료");
	}
}
