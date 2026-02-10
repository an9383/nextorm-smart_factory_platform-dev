package com.nextorm.extensions.proxystatusgateway.service.impl;

import com.nextorm.extensions.proxystatusgateway.repository.ServerStatus;
import com.nextorm.extensions.proxystatusgateway.service.PingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.InetAddress;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 핑 체크 서비스 구현체
 * InetAddress.isReachable()을 사용한 리액티브 핑 체크
 */
@Service
@Slf4j
public class PingServiceImpl implements PingService {

	@Override
	public Mono<Long> ping(
		String ip,
		int timeoutMs
	) {
		return Mono.fromCallable(() -> {
					   long startTime = System.currentTimeMillis();

					   try {
						   InetAddress inet = InetAddress.getByName(ip);
						   boolean isReachable = inet.isReachable(timeoutMs);

						   if (isReachable) {
							   long responseTime = System.currentTimeMillis() - startTime;
							   log.debug("핑 성공: {} - {}ms", ip, responseTime);
							   return responseTime;
						   } else {
							   log.debug("핑 실패: {} - 응답 없음", ip);
							   return -1L; // null 대신 -1L 반환 (실패 표시)
						   }
					   } catch (Exception e) {
						   log.debug("핑 실패: {} - {}", ip, e.getMessage());
						   return -1L; // null 대신 -1L 반환 (실패 표시)
					   }
				   })
				   .subscribeOn(Schedulers.boundedElastic())
				   .timeout(Duration.ofMillis(timeoutMs))
				   .onErrorReturn(-1L); // null 대신 -1L 반환
	}

	@Override
	public Mono<ServerStatus> pingAndUpdateStatus(
		ServerStatus serverStatus,
		int timeoutMs
	) {
		return ping(serverStatus.getIp(), timeoutMs).map(responseTime -> {
														if (responseTime >= 0) { // 0 이상인 경우 성공 (0ms도 성공)
															serverStatus.updateSuccess(responseTime, LocalDateTime.now());
															log.trace("서버 상태 업데이트 - 성공: {}", serverStatus.getName());
														} else { // -1인 경우만 실패
															serverStatus.updateFailure(LocalDateTime.now());
															log.trace("서버 상태 업데이트 - 실패: {}", serverStatus.getName());
														}
														return serverStatus;
													})
													.doOnError(error -> {
														log.warn("핑 처리 중 오류 발생: {} - {}",
															serverStatus.getName(),
															error.getMessage());
														serverStatus.updateFailure(LocalDateTime.now());
													})
													.onErrorReturn(serverStatus);
	}
}
