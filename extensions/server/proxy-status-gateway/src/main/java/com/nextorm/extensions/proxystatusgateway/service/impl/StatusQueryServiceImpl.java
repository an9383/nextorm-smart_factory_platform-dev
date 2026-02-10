package com.nextorm.extensions.proxystatusgateway.service.impl;

import com.nextorm.extensions.proxystatusgateway.dto.ServerStatusResponse;
import com.nextorm.extensions.proxystatusgateway.repository.StatusRepository;
import com.nextorm.extensions.proxystatusgateway.service.StatusQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 서버 상태 조회 서비스 구현체
 * 레파지토리와 컨트롤러 사이의 비즈니스 로직 처리
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StatusQueryServiceImpl implements StatusQueryService {

	private final StatusRepository statusRepository;

	@Override
	public Flux<ServerStatusResponse> getAllServerStatus() {
		log.debug("전체 서버 상태 조회 서비스 시작");

		return statusRepository.findAll()
							   .map(ServerStatusResponse::from)
							   .doOnNext(response -> log.trace("서버 상태 변환 완료: {}", response.getServerName()))
							   .doOnComplete(() -> log.debug("전체 서버 상태 조회 서비스 완료"))
							   .doOnError(error -> log.error("전체 서버 상태 조회 서비스 중 오류", error));
	}

	@Override
	public Mono<ServerStatusResponse> getServerStatus(String serverName) {
		log.debug("서버 상태 조회 서비스 시작: {}", serverName);

		return statusRepository.findByName(serverName)
							   .map(ServerStatusResponse::from)
							   .doOnNext(response -> log.debug("서버 상태 조회 서비스 성공: {}", serverName))
							   .doOnError(error -> log.error("서버 상태 조회 서비스 중 오류: {}", serverName, error))
							   .switchIfEmpty(Mono.defer(() -> {
								   log.warn("서버를 찾을 수 없음: {}", serverName);
								   return Mono.error(new RuntimeException("서버를 찾을 수 없습니다: " + serverName));
							   }));
	}
}
