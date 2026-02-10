package com.nextorm.extensions.proxystatusgateway.repository;

import lombok.*;

import java.time.LocalDateTime;

/**
 * 서버 상태 정보를 담는 DTO 클래스
 * 메모리에 저장될 서버의 현재 상태 정보
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class ServerStatus {

	private String name;
	private String ip;
	private PingStatus status;
	private LocalDateTime lastChecked;
	private Long responseTime; // 밀리초 단위
	private int consecutiveFailures;

	public static ServerStatus create(
		String name,
		String ip,
		LocalDateTime createdAt
	) {
		return new ServerStatus(name, ip, PingStatus.UNKNOWN, createdAt, null, 0);
	}

	/**
	 * 핑 성공 시 상태 업데이트
	 */
	public void updateSuccess(
		long responseTime,
		LocalDateTime successTime
	) {
		this.status = PingStatus.UP;
		this.lastChecked = successTime;
		this.responseTime = responseTime;
		this.consecutiveFailures = 0;
	}

	/**
	 * 핑 실패 시 상태 업데이트
	 */
	public void updateFailure(LocalDateTime failureTime) {
		this.status = PingStatus.DOWN;
		this.lastChecked = failureTime;
		this.responseTime = null;
		this.consecutiveFailures++;
	}

	/**
	 * 서버 상태 열거형
	 */
	@Getter
	public enum PingStatus {
		UP("정상"), DOWN("장애"), UNKNOWN("미확인");

		private final String description;

		PingStatus(String description) {
			this.description = description;
		}
	}
}
