package com.nextorm.extensions.proxystatusgateway.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ServerStatus 클래스 테스트")
class ServerStatusTest {

	@Test
	@DisplayName("서버 상태 객체를 정상적으로 생성할 수 있다")
	void shouldCreateServerStatusSuccessfully() {
		// given
		String name = "test-server";
		String ip = "192.168.1.100";
		LocalDateTime createdAt = LocalDateTime.of(2025, 8, 6, 14, 30, 0);

		// when
		ServerStatus serverStatus = ServerStatus.create(name, ip, createdAt);

		// then
		assertThat(serverStatus.getName()).isEqualTo(name);
		assertThat(serverStatus.getIp()).isEqualTo(ip);
		assertThat(serverStatus.getStatus()).isEqualTo(ServerStatus.PingStatus.UNKNOWN);
		assertThat(serverStatus.getLastChecked()).isEqualTo(createdAt);
		assertThat(serverStatus.getResponseTime()).isNull();
		assertThat(serverStatus.getConsecutiveFailures()).isZero();
	}

	@Test
	@DisplayName("핑 성공 시 상태가 올바르게 업데이트된다")
	void shouldUpdateStatusToSuccessWhenPingSucceeds() {
		// given
		ServerStatus serverStatus = ServerStatus.create("test-server", "192.168.1.100", LocalDateTime.now());
		long responseTime = 50L;
		LocalDateTime updateTime = LocalDateTime.of(2025, 8, 6, 15, 30, 0);

		// when
		serverStatus.updateSuccess(responseTime, updateTime);

		// then
		assertThat(serverStatus.getStatus()).isEqualTo(ServerStatus.PingStatus.UP);
		assertThat(serverStatus.getResponseTime()).isEqualTo(responseTime);
		assertThat(serverStatus.getConsecutiveFailures()).isZero();
		assertThat(serverStatus.getLastChecked()).isEqualTo(updateTime);
	}

	@Test
	@DisplayName("핑 실패 시 상태가 올바르게 업데이트된다")
	void shouldUpdateStatusToFailureWhenPingFails() {
		// given
		ServerStatus serverStatus = ServerStatus.create("test-server", "192.168.1.100", LocalDateTime.now());
		LocalDateTime failureTime = LocalDateTime.of(2025, 8, 6, 15, 45, 0);

		// when
		serverStatus.updateFailure(failureTime);

		// then
		assertThat(serverStatus.getStatus()).isEqualTo(ServerStatus.PingStatus.DOWN);
		assertThat(serverStatus.getResponseTime()).isNull();
		assertThat(serverStatus.getConsecutiveFailures()).isEqualTo(1);
		assertThat(serverStatus.getLastChecked()).isEqualTo(failureTime);
	}

	@Test
	@DisplayName("연속 실패 시 consecutiveFailures가 증가한다")
	void shouldIncrementConsecutiveFailuresOnRepeatedFailures() {
		// given
		ServerStatus serverStatus = ServerStatus.create("test-server", "192.168.1.100", LocalDateTime.now());
		LocalDateTime firstFailure = LocalDateTime.of(2025, 8, 6, 16, 0, 0);
		LocalDateTime secondFailure = LocalDateTime.of(2025, 8, 6, 16, 5, 0);
		LocalDateTime thirdFailure = LocalDateTime.of(2025, 8, 6, 16, 10, 0);

		// when
		serverStatus.updateFailure(firstFailure);
		serverStatus.updateFailure(secondFailure);
		serverStatus.updateFailure(thirdFailure);

		// then
		assertThat(serverStatus.getConsecutiveFailures()).isEqualTo(3);
		assertThat(serverStatus.getStatus()).isEqualTo(ServerStatus.PingStatus.DOWN);
		assertThat(serverStatus.getLastChecked()).isEqualTo(thirdFailure);
	}

	@Test
	@DisplayName("실패 후 성공 시 consecutiveFailures가 0으로 리셋된다")
	void shouldResetConsecutiveFailuresOnSuccess() {
		// given
		ServerStatus serverStatus = ServerStatus.create("test-server", "192.168.1.100", LocalDateTime.now());
		LocalDateTime firstFailure = LocalDateTime.of(2025, 8, 6, 16, 0, 0);
		LocalDateTime secondFailure = LocalDateTime.of(2025, 8, 6, 16, 5, 0);
		LocalDateTime successTime = LocalDateTime.of(2025, 8, 6, 16, 10, 0);

		serverStatus.updateFailure(firstFailure);
		serverStatus.updateFailure(secondFailure);

		// when
		serverStatus.updateSuccess(30L, successTime);

		// then
		assertThat(serverStatus.getConsecutiveFailures()).isZero();
		assertThat(serverStatus.getStatus()).isEqualTo(ServerStatus.PingStatus.UP);
		assertThat(serverStatus.getResponseTime()).isEqualTo(30L);
		assertThat(serverStatus.getLastChecked()).isEqualTo(successTime);
	}

	@Test
	@DisplayName("PingStatus enum의 설명이 올바르게 설정되어 있다")
	void shouldHaveCorrectDescriptionsForPingStatus() {
		// then
		assertThat(ServerStatus.PingStatus.UP.getDescription()).isEqualTo("정상");
		assertThat(ServerStatus.PingStatus.DOWN.getDescription()).isEqualTo("장애");
		assertThat(ServerStatus.PingStatus.UNKNOWN.getDescription()).isEqualTo("미확인");
	}
}
