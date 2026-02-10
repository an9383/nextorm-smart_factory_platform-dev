package com.nextorm.extensions.proxystatusgateway.service;

import com.nextorm.extensions.proxystatusgateway.dto.ServerStatusResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 서버 상태 조회 서비스 인터페이스
 * 컨트롤러와 레파지토리 사이의 서비스 계층
 */
public interface StatusQueryService {

	/**
	 * 전체 서버 상태 조회
	 *
	 * @return 전체 서버 상태 목록
	 */
	Flux<ServerStatusResponse> getAllServerStatus();

	/**
	 * 특정 서버 상태 조회
	 *
	 * @param serverName 조회할 서버 이름
	 * @return 서버 상태 정보
	 */
	Mono<ServerStatusResponse> getServerStatus(String serverName);

}
