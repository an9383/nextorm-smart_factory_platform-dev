package com.nextorm.extensions.misillan.alarm.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ThreadLocalRandom;

@Configuration
@Slf4j
public class DebeziumConfiguration {

	@Value("${source.mariadb.host:localhost}")
	private String dbHost;

	@Value("${source.mariadb.port:3306}")
	private String dbPort;

	@Value("${source.mariadb.database}")
	private String database;

	@Value("${source.mariadb.username}")
	private String username;

	@Value("${source.mariadb.password}")
	private String password;

	@Value("${cdc.data-dir:./cdc-data}")
	private String dataDir;

	@Bean
	@ConditionalOnProperty(value = "cdc.enabled", havingValue = "true", matchIfMissing = true)
	public io.debezium.config.Configuration debeziumConnectorConfig() {

		String offsetFileName = dataDir + "/mariadb_offsets.dat";
		String schemaHistoryFileName = dataDir + "/mariadb_schema_history.dat";

		return io.debezium.config.Configuration.create()
											   // Debezium 커넥터 구현 클래스 지정 (MariaDB용)
											   .with("connector.class",
												   "io.debezium.connector.mariadb.MariaDbConnector")
											   // 커넥터 인스턴스 이름 (로그/모니터링 식별용)
											   .with("name", "mariadb-connector")

											   // DB 호스트명: 바이너리 로그(리플리케이션 스트림) 접근 대상 호스트
											   .with("database.hostname", dbHost)
											   // DB 포트: 바이너리 로그 접근 포트
											   .with("database.port", dbPort)
											   // DB 접속 사용자
											   .with("database.user", username)
											   // DB 접속 비밀번호
											   .with("database.password", password)

											   // SSL 모드 설정 (disabled, REQUIRED, VERIFY_CA, VERIFY_FULL 등)
											   .with("database.ssl.mode", "disabled")

											   // 연결 재시도 관련 설정
											   // 연결 재시도 횟수 (기본값: 3)
											   .with("database.reconnect.attempts", "-1")  // -1은 무제한
											   // 재시도 간격 (밀리초)
											   .with("database.reconnect.delay.ms", "10000")
											   // 연결 타임아웃
											   .with("database.connect.timeout.ms", "30000")

											   // DB 서버 시간대 설정 (DB에서 사용하는 시간대 지정)
											   .with("database.serverTimezone", "Asia/Seoul")
											   // 커넥션에 적용할 시간대
											   .with("database.connectionTimeZone", "Asia/Seoul")
											   // 커넥션 시간대를 세션에 강제 적용할지 여부 (true/false)
											   .with("database.forceConnectionTimeZoneToSession", "false")

											   // 오프셋 저장 방식: 파일 기반 오프셋 백엔드 사용
											   // .with("offset.storage",
											   //    "org.apache.kafka.connect.storage.FileOffsetBackingStore")
											   // // 오프셋을 저장할 파일 경로
											   // .with("offset.storage.file.filename", offsetFileName)

											   // 메모리 기반 오프셋 (실시간 변경 사항만 추적한다. 프로그램이 시작 전의 이력은 추적 불가)
											   .with("offset.storage",
												   "org.apache.kafka.connect.storage.MemoryOffsetBackingStore")

											   // 스키마 히스토리 저장 클래스 (파일 기반)
											   .with("schema.history.internal",
												   "io.debezium.storage.file.history.FileSchemaHistory")
											   // 스키마 히스토리 파일 경로
											   .with("schema.history.internal.file.filename", schemaHistoryFileName)

											   // DB 서버 ID: MySQL/MariaDB에서 고유한 서버/클라이언트 ID (복제 충돌 방지)
											   .with("database.server.id",
												   String.valueOf(ThreadLocalRandom.current()
																				   .nextInt(5400, 6400)))
											   // CDC 대상으로 포함할 데이터베이스 목록
											   .with("database.include.list", database)
											   // CDC 대상으로 포함할 테이블 목록 (여기서는 특정 테이블)
											   .with("table.include.list", database + ".parameter_data")
											   // Kafka 토픽 접두사: Debezium이 생성하는 토픽 이름 앞에 붙임
											   .with("topic.prefix", "cdc-mariadb")
											   // 스냅샷 모드: Debezium이 시작 시 데이터베이스의 초기 스냅샷을 어떻게 처리할지 결정
											   // - initial: 시작 시 전체 테이블의 스냅샷을 수행한 뒤 변경 이벤트를 스트리밍
											   // - never: 스냅샷을 수행하지 않음. 오직 바이너리 로그에서 변경을 읽음 (로그 위치 필요)
											   // - schema_only: 데이터 행 스냅샷 없이 스키마(테이블 구조)만 캡처
											   // - schema_only_recovery: 복구 상황에서 스키마만 캡처하고 이후 로그에서 계속 진행 (특수 상황용)
											   .with("snapshot.mode", "schema_only_recovery")

											   // 스키마 변경 감지 및 허용
											   .with("include.schema.changes", "true")
											   // 스키마 변경 이벤트에서 특정 테이블만 포함
											   .with("schema.history.internal.ddl.filter",
												   "DROP TEMPORARY TABLE.*|.*\\.(?!" + database + "\\.parameter_data).*")
											   .build();
	}
}