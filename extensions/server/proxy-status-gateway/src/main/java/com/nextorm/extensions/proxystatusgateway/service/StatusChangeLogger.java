package com.nextorm.extensions.proxystatusgateway.service;

import com.nextorm.extensions.proxystatusgateway.repository.ServerStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 서버 상태 변경 감지 및 로깅 서비스
 * 서버 상태가 변경될 때마다 상세한 로그를 기록
 */
@Service
@Slf4j
public class StatusChangeLogger {

	// 이전 상태를 저장하여 변경 감지
	private final ConcurrentHashMap<String, ServerStatus.PingStatus> previousStatuses = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<String, LocalDateTime> lastChangeTime = new ConcurrentHashMap<>();

	/**
	 * 서버 상태 변경 감지 및 로깅
	 */
	public Mono<ServerStatus> logStatusChange(ServerStatus currentStatus) {
		return Mono.fromCallable(() -> {
			String serverName = currentStatus.getName();
			ServerStatus.PingStatus currentState = currentStatus.getStatus();
			ServerStatus.PingStatus previousState = previousStatuses.get(serverName);

			// 상태 변경이 있는 경우만 로깅
			if (previousState != null && previousState != currentState) {
				logStateChange(currentStatus, previousState);
				updateChangeTime(serverName);
			} else if (previousState == null) {
				// 처음 상태 설정 시
				log.info("서버 상태 초기 설정: {} -> {}", serverName, currentState.getDescription());
				updateChangeTime(serverName);
			}

			// 연속 실패 시 경고 로깅
			if (currentState == ServerStatus.PingStatus.DOWN) {
				logConsecutiveFailures(currentStatus);
			}

			// 복구 시 로깅
			if (previousState == ServerStatus.PingStatus.DOWN && currentState == ServerStatus.PingStatus.UP) {
				logRecovery(currentStatus);
			}

			// 현재 상태 저장
			previousStatuses.put(serverName, currentState);

			return currentStatus;
		});
	}

	/**
	 * 상태 변경 로깅
	 */
	private void logStateChange(
		ServerStatus status,
		ServerStatus.PingStatus previousState
	) {
		String serverName = status.getName();
		ServerStatus.PingStatus currentState = status.getStatus();

		Duration timeSinceLastChange = getTimeSinceLastChange(serverName);

		if (currentState == ServerStatus.PingStatus.UP) {
			log.info("서버 상태 변경 [{}]: {} -> {} (응답시간: {}ms, 지속시간: {})",
				serverName,
				previousState.getDescription(),
				currentState.getDescription(),
				status.getResponseTime(),
				formatDuration(timeSinceLastChange));
		} else {
			log.warn("서버 상태 변경 [{}]: {} -> {} (연속실패: {}회, 지속시간: {})",
				serverName,
				previousState.getDescription(),
				currentState.getDescription(),
				status.getConsecutiveFailures(),
				formatDuration(timeSinceLastChange));
		}
	}

	/**
	 * 연속 실패 경고 로깅
	 */
	private void logConsecutiveFailures(ServerStatus status) {
		int failures = status.getConsecutiveFailures();
		String serverName = status.getName();

		if (failures == 5) {
			log.warn("서버 연속 실패 주의 [{}]: {}회 연속 실패", serverName, failures);
		} else if (failures == 10) {
			log.error("서버 연속 실패 경고 [{}]: {}회 연속 실패", serverName, failures);
		} else if (failures > 10 && failures % 30 == 0) {
			log.error("서버 장기 장애 [{}]: {}회 연속 실패 ({}분 경과)", serverName, failures, failures / 60);
		}
	}

	/**
	 * 서버 복구 로깅
	 */
	private void logRecovery(ServerStatus status) {
		String serverName = status.getName();
		Duration downtime = getTimeSinceLastChange(serverName);

		log.info("서버 복구 완료 [{}]: 다운타임 {}, 응답시간 {}ms", serverName, formatDuration(downtime), status.getResponseTime());
	}

	/**
	 * 마지막 변경 시점부터 경과 시간 계산
	 */
	private Duration getTimeSinceLastChange(String serverName) {
		LocalDateTime lastChange = lastChangeTime.get(serverName);
		return lastChange != null
			   ? Duration.between(lastChange, LocalDateTime.now())
			   : Duration.ZERO;
	}

	/**
	 * 변경 시간 업데이트
	 */
	private void updateChangeTime(String serverName) {
		lastChangeTime.put(serverName, LocalDateTime.now());
	}

	/**
	 * Duration을 읽기 쉬운 형태로 포맷
	 */
	private String formatDuration(Duration duration) {
		long seconds = duration.getSeconds();
		if (seconds < 60) {
			return seconds + "초";
		} else if (seconds < 3600) {
			return (seconds / 60) + "분 " + (seconds % 60) + "초";
		} else {
			long hours = seconds / 3600;
			long minutes = (seconds % 3600) / 60;
			return hours + "시간 " + minutes + "분";
		}
	}

	/**
	 * 현재 모니터링 통계 로깅
	 */
	public void logMonitoringStats(
		long totalServers,
		long activeServers,
		long downServers
	) {
		double upPercentage = totalServers > 0
							  ? (double)activeServers / totalServers * 100
							  : 0;

		log.info("모니터링 통계 - 전체: {}개, 정상: {}개 ({}%), 장애: {}개",
			totalServers,
			activeServers,
			String.format("%.2f", upPercentage),
			downServers);

		if (downServers > 0) {
			log.warn("현재 {}개 서버에 장애가 감지되었습니다", downServers);
		}
	}
}
