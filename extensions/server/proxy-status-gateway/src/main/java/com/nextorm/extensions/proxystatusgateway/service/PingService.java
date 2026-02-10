package com.nextorm.extensions.proxystatusgateway.service;

import com.nextorm.extensions.proxystatusgateway.repository.ServerStatus;
import reactor.core.publisher.Mono;

/**
 * 핑 체크 서비스 인터페이스
 * 리액티브 프로그래밍을 활용한 비동기 핑 체크 기능 제공
 */
public interface PingService {

	/**
	 * 단일 서버에 대한 핑 체크
	 *
	 * @param ip        핑할 서버의 IP 주소
	 * @param timeoutMs 타임아웃 (밀리초)
	 * @return 핑 결과 (응답 시간 또는 null)
	 */
	Mono<Long> ping(
		String ip,
		int timeoutMs
	);

	/**
	 * 서버 상태 업데이트와 함께 핑 체크
	 *
	 * @param serverStatus 업데이트할 서버 상태 객체
	 * @param timeoutMs    타임아웃 (밀리초)
	 * @return 업데이트된 서버 상태
	 */
	Mono<ServerStatus> pingAndUpdateStatus(
		ServerStatus serverStatus,
		int timeoutMs
	);
}
