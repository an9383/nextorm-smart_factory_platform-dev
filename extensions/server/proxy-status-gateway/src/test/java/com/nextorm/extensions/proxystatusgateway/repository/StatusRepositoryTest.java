package com.nextorm.extensions.proxystatusgateway.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("StatusRepository 클래스 테스트")
class StatusRepositoryTest {

	private StatusRepository statusRepository;

	@BeforeEach
	void setUp() {
		statusRepository = new StatusRepository();
	}

	@Test
	@DisplayName("서버 상태를 정상적으로 저장할 수 있다")
	void shouldSaveServerStatusSuccessfully() {
		// given
		ServerStatus serverStatus = ServerStatus.create("test-server", "192.168.1.100", LocalDateTime.now());

		// when & then
		StepVerifier.create(statusRepository.save(serverStatus)) // Publisher를 구독하고 테스트 시작
					.expectNext(serverStatus) // 다음 방출값이 serverStatus와 같은지 검증
					.verifyComplete(); // Publisher가 정상적으로 완료되는지 검증
	}

	@Test
	@DisplayName("저장된 서버 상태를 이름으로 조회할 수 있다")
	void shouldFindServerStatusByName() {
		// given
		ServerStatus serverStatus = ServerStatus.create("test-server", "192.168.1.100", LocalDateTime.now());
		statusRepository.save(serverStatus)
						.block(); // 동기적으로 실행하여 저장 완료까지 대기

		// when & then
		StepVerifier.create(statusRepository.findByName("test-server")) // Publisher를 구독하고 테스트 시작
					.expectNext(serverStatus) // 다음 방출값이 serverStatus와 같은지 검증
					.verifyComplete(); // Publisher가 정상적으로 완료되는지 검증
	}

	@Test
	@DisplayName("존재하지 않는 서버 이름으로 조회 시 빈 결과를 반환한다")
	void shouldReturnEmptyWhenServerNotFound() {
		// when & then
		StepVerifier.create(statusRepository.findByName("non-existent-server")) // Publisher를 구독하고 테스트 시작
					.expectNextCount(0) // 방출되는 요소의 개수가 0개인지 검증
					.verifyComplete(); // Publisher가 정상적으로 완료되는지 검증
	}

	@Test
	@DisplayName("모든 서버 상태를 조회할 수 있다")
	void shouldFindAllServerStatuses() {
		// given
		ServerStatus server1 = ServerStatus.create("server1", "192.168.1.100", LocalDateTime.now());
		ServerStatus server2 = ServerStatus.create("server2", "192.168.1.101", LocalDateTime.now());
		ServerStatus server3 = ServerStatus.create("server3", "192.168.1.102", LocalDateTime.now());

		statusRepository.save(server1)
						.block(); // 동기적으로 실행하여 저장 완료까지 대기
		statusRepository.save(server2)
						.block(); // 동기적으로 실행하여 저장 완료까지 대기
		statusRepository.save(server3)
						.block(); // 동기적으로 실행하여 저장 완료까지 대기

		// when & then
		StepVerifier.create(statusRepository.findAll()) // Publisher를 구독하고 테스트 시작
					.expectNextCount(3) // 방출되는 요소의 개수가 3개인지 검증
					.verifyComplete(); // Publisher가 정상적으로 완료되는지 검증
	}

	@Test
	@DisplayName("저장된 서버가 없을 때 findAll은 빈 Flux를 반환한다")
	void shouldReturnEmptyFluxWhenNoServersStored() {
		// when & then
		StepVerifier.create(statusRepository.findAll()) // Publisher를 구독하고 테스트 시작
					.expectNextCount(0) // 방출되는 요소의 개수가 0개인지 검증
					.verifyComplete(); // Publisher가 정상적으로 완료되는지 검증
	}

	@Test
	@DisplayName("서버 상태를 업데이트할 수 있다")
	void shouldUpdateExistingServerStatus() {
		// given
		ServerStatus serverStatus = ServerStatus.create("test-server", "192.168.1.100", LocalDateTime.now());
		statusRepository.save(serverStatus)
						.block(); // 동기적으로 실행하여 저장 완료까지 대기

		// 상태 업데이트
		LocalDateTime updateTime = LocalDateTime.of(2025, 8, 6, 17, 0, 0);
		serverStatus.updateSuccess(50L, updateTime);

		// when
		statusRepository.save(serverStatus)
						.block(); // 동기적으로 실행하여 저장 완료까지 대기

		// then
		StepVerifier.create(statusRepository.findByName("test-server")) // Publisher를 구독하고 테스트 시작
					.assertNext(status -> { // 다음 방출값에 대해 복잡한 검증 로직 실행
						assertThat(status.getStatus()).isEqualTo(ServerStatus.PingStatus.UP);
						assertThat(status.getResponseTime()).isEqualTo(50L);
						assertThat(status.getLastChecked()).isEqualTo(updateTime);
					})
					.verifyComplete(); // Publisher가 정상적으로 완료되는지 검증
	}

	@Test
	@DisplayName("모든 서버 상태를 삭제할 수 있다")
	void shouldDeleteAllServerStatuses() {
		// given
		ServerStatus server1 = ServerStatus.create("server1", "192.168.1.100", LocalDateTime.now());
		ServerStatus server2 = ServerStatus.create("server2", "192.168.1.101", LocalDateTime.now());

		statusRepository.save(server1)
						.block(); // 동기적으로 실행하여 저장 완료까지 대기
		statusRepository.save(server2)
						.block(); // 동기적으로 실행하여 저장 완료까지 대기

		// when
		StepVerifier.create(statusRepository.deleteAll()) // Publisher를 구독하고 테스트 시작
					.verifyComplete(); // Publisher가 정상적으로 완료되는지 검증 (반환값 없음)

		// then
		StepVerifier.create(statusRepository.count()) // Publisher를 구독하고 테스트 시작
					.expectNext(0) // 다음 방출값이 0과 같은지 검증
					.verifyComplete(); // Publisher가 정상적으로 완료되는지 검증

		StepVerifier.create(statusRepository.findAll()) // Publisher를 구독하고 테스트 시작
					.expectNextCount(0) // 방출되는 요소의 개수가 0개인지 검증
					.verifyComplete(); // Publisher가 정상적으로 완료되는지 검증
	}

	@Test
	@DisplayName("동일한 이름의 서버를 여러 번 저장하면 덮어쓰기된다")
	void shouldOverwriteServerWithSameName() {
		// given
		ServerStatus server1 = ServerStatus.create("test-server", "192.168.1.100", LocalDateTime.now());
		ServerStatus server2 = ServerStatus.create("test-server", "192.168.1.200", LocalDateTime.now());

		// when
		statusRepository.save(server1)
						.block(); // 동기적으로 실행하여 저장 완료까지 대기
		statusRepository.save(server2)
						.block(); // 동기적으로 실행하여 저장 완료까지 대기

		// then
		StepVerifier.create(statusRepository.count()) // Publisher를 구독하고 테스트 시작
					.expectNext(1) // 다음 방출값이 1과 같은지 검증
					.verifyComplete(); // Publisher가 정상적으로 완료되는지 검증

		StepVerifier.create(statusRepository.findByName("test-server")) // Publisher를 구독하고 테스트 시작
					.assertNext(status -> assertThat(status.getIp()).isEqualTo("192.168.1.200")) // 다음 방출값에 대해 검증 로직 실행
					.verifyComplete(); // Publisher가 정상적으로 완료되는지 검증
	}

	@Test
	@DisplayName("여러 스레드에서 동시 접근해도 안전하다")
	void shouldBeSafeForConcurrentAccess() {
		// given
		int numberOfServers = 100;
		int availableProcessors = Runtime.getRuntime()
										 .availableProcessors(); // 시스템의 CPU 코어 수
		int concurrency = Math.min(10, availableProcessors); // 시스템 코어 수와 10 중 작은 값 사용

		// when
		Flux<ServerStatus> saveFlux = Flux.range(1, numberOfServers) // 1부터 numberOfServers까지의 정수 스트림 생성
										  .flatMap(i -> { // 각 정수를 병렬로 처리 (동시성 테스트)
											  ServerStatus server = ServerStatus.create("server" + i,
												  "192.168.1." + i,
												  LocalDateTime.now());
											  return statusRepository.save(server)
																	 .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic()); // 별도 스레드에서 실행하여 진정한 동시성 보장
										  }, concurrency); // 시스템에 적합한 동시성 레벨 사용

		// then
		StepVerifier.create(saveFlux) // Publisher를 구독하고 테스트 시작
					.expectNextCount(numberOfServers) // 방출되는 요소의 개수가 numberOfServers개인지 검증
					.verifyComplete(); // Publisher가 정상적으로 완료되는지 검증

		StepVerifier.create(statusRepository.count()) // Publisher를 구독하고 테스트 시작
					.expectNext(numberOfServers) // 다음 방출값이 numberOfServers와 같은지 검증
					.verifyComplete(); // Publisher가 정상적으로 완료되는지 검증
	}
}
