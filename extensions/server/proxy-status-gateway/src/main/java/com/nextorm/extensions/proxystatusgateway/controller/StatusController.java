package com.nextorm.extensions.proxystatusgateway.controller;

import com.nextorm.extensions.proxystatusgateway.dto.ServerStatusResponse;
import com.nextorm.extensions.proxystatusgateway.exception.ServerNotFoundException;
import com.nextorm.extensions.proxystatusgateway.exception.StatusServiceException;
import com.nextorm.extensions.proxystatusgateway.service.StatusQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 서버 상태 조회를 위한 REST API 컨트롤러
 * WebFlux 기반 리액티브 엔드포인트 제공
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class StatusController {

	private final StatusQueryService statusQueryService;

	/**
	 * 전체 서버 상태 조회
	 * GET /api/status
	 */
	@GetMapping(value = "/status")
	public Flux<ServerStatusResponse> getAllServerStatus() {
		log.debug("전체 서버 상태 조회 요청");

		return statusQueryService.getAllServerStatus()
								 .doOnSubscribe(subscription -> log.trace("전체 서버 상태 조회 시작"))
								 .doOnComplete(() -> log.trace("전체 서버 상태 조회 완료"))
								 .onErrorMap(throwable -> {
									 log.error("전체 서버 상태 조회 중 오류", throwable);
									 return new StatusServiceException("서버 상태 조회 중 오류가 발생했습니다", throwable);
								 });
	}

	/**
	 * 특정 서버 상태 조회
	 * GET /api/status/{serverName}
	 */
	@GetMapping(value = "/status/{serverName}")
	public Mono<ServerStatusResponse> getServerStatus(@PathVariable String serverName) {
		log.debug("서버 상태 조회 요청: {}", serverName);

		return statusQueryService.getServerStatus(serverName)
								 .doOnNext(status -> log.trace("서버 상태 조회 성공: {}", serverName))
								 .onErrorMap(throwable -> {
									 log.error("서버 상태 조회 중 오류: {}", serverName, throwable);

									 // NoSuchElementException 등 서버를 찾을 수 없는 경우
									 if (throwable.getMessage() != null && (throwable.getMessage()
																					 .contains("찾을 수 없습니다") || throwable instanceof java.util.NoSuchElementException)) {
										 return new ServerNotFoundException(serverName);
									 }
									 // 기타 서버 오류
									 return new StatusServiceException("서버 상태 조회 중 오류가 발생했습니다", throwable);
								 });
	}
}
