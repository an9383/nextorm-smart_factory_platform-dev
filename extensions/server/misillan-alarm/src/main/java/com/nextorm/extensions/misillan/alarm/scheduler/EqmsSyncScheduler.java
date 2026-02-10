package com.nextorm.extensions.misillan.alarm.scheduler;

import com.nextorm.extensions.misillan.alarm.service.EqmsDataSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EqmsSyncScheduler {
	private final EqmsDataSyncService syncService;

	@Scheduled(cron = "${scheduler.eqms-sync.cron: 0 0/1 * * * *}")
	public void syncTool() {
		log.info("EQMS 설비 데이터 동기화 시작");
		syncService.syncTool();
		log.info("EQMS 설비 데이터 동기화 종료");
	}

	@Scheduled(cron = "${scheduler.eqms-sync.cron: 0 0/1 * * * *}")
	public void syncProduct() {
		log.info("EQMS 품목 데이터 동기화 시작");
		syncService.syncProduct();
		log.info("EQMS 품목 데이터 동기화 시작");
	}
}
