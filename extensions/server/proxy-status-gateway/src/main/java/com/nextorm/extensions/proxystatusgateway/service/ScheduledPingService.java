package com.nextorm.extensions.proxystatusgateway.service;

import com.nextorm.extensions.proxystatusgateway.config.ServerInfo;
import com.nextorm.extensions.proxystatusgateway.repository.ServerStatus;
import com.nextorm.extensions.proxystatusgateway.repository.StatusRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 스케줄링 기반 핑 체크 서비스
 * 매 초마다 모든 서버에 대한 핑 체크를 수행
 */
@Service
@Slf4j
public class ScheduledPingService {

	private final PingService pingService;
	private final StatusRepository statusRepository;
	private final StatusChangeLogger statusChangeLogger;
	private final List<ServerInfo> serverList;

	@Value("${proxy.status.ping.timeout:3000}")
	private int pingTimeout;

	@Value("${proxy.status.ping.max-concurrent:30}")
	private int maxConcurrent;

	public ScheduledPingService(
		PingService pingService,
		StatusRepository statusRepository,
		StatusChangeLogger statusChangeLogger,
		List<ServerInfo> serverList
	) {
		this.pingService = pingService;
		this.statusRepository = statusRepository;
		this.statusChangeLogger = statusChangeLogger;
		this.serverList = serverList;
	}

	/**
	 * 매 초마다 모든 서버에 대한 핑 체크 수행
	 * application.yml의 proxy.status.ping.interval 설정값으로 간격 조정 가능
	 */
	@Scheduled(fixedRateString = "${proxy.status.ping.interval:1000}")
	public void performScheduledPing() {
		if (serverList == null || serverList.isEmpty()) {
			log.warn("핑할 서버 목록이 비어있습니다");
			return;
		}

		log.debug("스케줄된 핑 체크 시작: {}개 서버", serverList.size());

		// 모든 서버에 대해 병렬로 핑 체크 수행
		Flux.fromIterable(serverList)
			.parallel(Math.min(maxConcurrent, serverList.size())) // 최대 동시 처리 수 제한
			.runOn(Schedulers.boundedElastic())
			.flatMap(this::pingServerAndUpdateStatus)
			.sequential()
			.collectList()
			.subscribe(results -> {
				long successCount = results.stream()
										   .mapToLong(status -> status.getStatus() == ServerStatus.PingStatus.UP
																? 1
																: 0)
										   .sum();
				long downCount = results.size() - successCount;

				log.debug("핑 체크 완료: 성공 {}/{}", successCount, results.size());

				// 주기적으로 모니터링 통계 로깅 (매 30초마다)
				if (System.currentTimeMillis() % 30000 < 1000) {
					statusChangeLogger.logMonitoringStats(results.size(), successCount, downCount);
				}
			}, error -> log.error("스케줄된 핑 체크 중 오류 발생", error));
	}

	/**
	 * 개별 서버에 대한 핑 체크 및 상태 업데이트
	 */
	private Mono<ServerStatus> pingServerAndUpdateStatus(ServerInfo serverInfo) {
		return statusRepository.findByName(serverInfo.getName())
							   .switchIfEmpty(
								   // 상태가 없으면 새로 생성
								   Mono.fromCallable(() -> ServerStatus.create(serverInfo.getName(),
									   serverInfo.getIp(),
									   LocalDateTime.now())))
							   .flatMap(serverStatus -> pingService.pingAndUpdateStatus(serverStatus, pingTimeout)
																   .flatMap(statusChangeLogger::logStatusChange) // 상태 변경 로깅 추가
																   .flatMap(statusRepository::save))
							   .doOnError(error -> log.warn("서버 {} 핑 체크 실패: {}",
								   serverInfo.getName(),
								   error.getMessage()))
							   .onErrorResume(error -> {
								   // 에러 발생 시 실패 상태로 저장
								   ServerStatus failedStatus = ServerStatus.create(serverInfo.getName(),
									   serverInfo.getIp(),
									   LocalDateTime.now());
								   failedStatus.updateFailure(LocalDateTime.now());
								   return statusChangeLogger.logStatusChange(failedStatus)
															.flatMap(statusRepository::save);
							   });
	}
}
