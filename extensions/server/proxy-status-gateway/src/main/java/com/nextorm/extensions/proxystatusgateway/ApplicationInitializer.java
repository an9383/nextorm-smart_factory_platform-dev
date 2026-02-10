package com.nextorm.extensions.proxystatusgateway;

import com.nextorm.extensions.proxystatusgateway.config.ServerInfo;
import com.nextorm.extensions.proxystatusgateway.repository.ServerStatus;
import com.nextorm.extensions.proxystatusgateway.repository.StatusRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 애플리케이션 초기화 클래스
 * 애플리케이션 시작 시 JSON 파일에서 로드된 서버 목록을 메모리에 초기화
 */
@Component
@Slf4j
public class ApplicationInitializer implements ApplicationRunner {

	private final StatusRepository statusRepository;
	private final List<ServerInfo> serverList;

	public ApplicationInitializer(
		StatusRepository statusRepository,
		List<ServerInfo> serverList
	) {
		this.statusRepository = statusRepository;
		this.serverList = serverList;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		log.info("애플리케이션 초기화 시작");

		if (serverList == null || serverList.isEmpty()) {
			log.warn("서버 목록이 비어있습니다. 초기화를 건너뜁니다.");
			return;
		}

		// 기존 메모리 상태 삭제 (재시작 시 깔끔하게 초기화)
		statusRepository.deleteAll()
						.doOnSuccess(ignored -> log.debug("기존 서버 상태 삭제 완료"))
						.then(initializeServerStatuses())
						.subscribe(count -> {
							log.info("애플리케이션 초기화 완료: {}개 서버 상태 초기화", count);
							logServerSummary();
						}, error -> {
							log.error("애플리케이션 초기화 중 오류 발생", error);
							throw new RuntimeException("애플리케이션 초기화 실패", error);
						});
	}

	/**
	 * 서버 상태 초기화
	 */
	private Mono<Integer> initializeServerStatuses() {
		return Flux.fromIterable(serverList)
				   .map(serverInfo -> {
					   ServerStatus serverStatus = ServerStatus.create(serverInfo.getName(),
						   serverInfo.getIp(),
						   LocalDateTime.now());
					   log.debug("서버 상태 초기화: {} ({})", serverInfo.getName(), serverInfo.getIp());
					   return serverStatus;
				   })
				   .flatMap(statusRepository::save)
				   .count()
				   .map(Long::intValue);
	}

	/**
	 * 초기화된 서버 요약 정보 로깅
	 */
	private void logServerSummary() {
		statusRepository.count()
						.subscribe(count -> {
							log.info("=== 서버 상태 모니터링 시작 ===");
							log.info("총 모니터링 대상 서버: {}개", count);
							log.info("핑 체크 간격: application.yml 설정값 참조");
							log.info("핑 타임아웃: application.yml 설정값 참조");
							log.info("최대 동시 처리: application.yml 설정값 참조");
							log.info("==================================");
						});
	}
}
