package com.nextorm.extensions.proxystatusgateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 서버 상태 조회 응답용 DTO
 * 외부 API에서 노출되는 서버 상태 정보
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServerStatusResponse {

	/**
	 * 서버 이름
	 */
	private String serverName;

	/**
	 * 서버 상태 (UP/DOWN/UNKNOWN)
	 */
	private String status;

	/**
	 * 마지막 조회 시간
	 */
	private LocalDateTime lastCheckedTime;

	/**
	 * ServerStatus 도메인 객체를 DTO로 변환하는 정적 팩토리 메서드
	 */
	public static ServerStatusResponse from(com.nextorm.extensions.proxystatusgateway.repository.ServerStatus serverStatus) {
		return new ServerStatusResponse(serverStatus.getName(),
			serverStatus.getStatus()
						.name(),
			serverStatus.getLastChecked());
	}
}
