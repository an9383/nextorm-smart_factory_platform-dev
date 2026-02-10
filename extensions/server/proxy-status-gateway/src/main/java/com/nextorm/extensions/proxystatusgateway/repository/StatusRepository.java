package com.nextorm.extensions.proxystatusgateway.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 서버 상태 메모리 저장소
 * ConcurrentHashMap을 사용한 스레드 안전한 상태 관리
 */
@Repository
@Slf4j
public class StatusRepository {

	private final ConcurrentHashMap<String, ServerStatus> serverStatusMap = new ConcurrentHashMap<>();

	/**
	 * 서버 상태 저장/업데이트
	 */
	public Mono<ServerStatus> save(ServerStatus serverStatus) {
		return Mono.fromCallable(() -> {
			serverStatusMap.put(serverStatus.getName(), serverStatus);
			log.trace("서버 상태 저장: {}", serverStatus.getName());
			return serverStatus;
		});
	}

	/**
	 * 서버 이름으로 상태 조회
	 */
	public Mono<ServerStatus> findByName(String serverName) {
		return Mono.fromCallable(() -> serverStatusMap.get(serverName))
				   .doOnNext(status -> {
					   if (status != null) {
						   log.trace("서버 상태 조회 성공: {}", serverName);
					   } else {
						   log.debug("서버 상태 조회 실패: {} (존재하지 않음)", serverName);
					   }
				   });
	}

	/**
	 * 모든 서버 상태 조회
	 */
	public Flux<ServerStatus> findAll() {
		return Flux.fromIterable(serverStatusMap.values())
				   .doOnSubscribe(subscription -> log.trace("전체 서버 상태 조회 시작"))
				   .doOnComplete(() -> log.trace("전체 서버 상태 조회 완료: {}개", serverStatusMap.size()));
	}

	/**
	 * 모든 서버 상태 삭제
	 */
	public Mono<Void> deleteAll() {
		return Mono.fromRunnable(() -> {
			int size = serverStatusMap.size();
			serverStatusMap.clear();
			log.info("전체 서버 상태 삭제: {}개", size);
		});
	}

	/**
	 * 현재 저장된 서버 개수
	 */
	public Mono<Integer> count() {
		return Mono.fromCallable(serverStatusMap::size);
	}
}
