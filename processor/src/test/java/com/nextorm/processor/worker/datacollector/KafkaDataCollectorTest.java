package com.nextorm.processor.worker.datacollector;

import com.nextorm.processor.service.ConsumerFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * KafkaDataCollector 단위 테스트
 * Mockito를 사용하여 Kafka 의존성을 제거하고 테스트
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("KafkaDataCollector 단위 테스트")
class KafkaDataCollectorTest {

	@Mock
	private ConsumerFactory consumerFactory;

	@Mock
	private KafkaConsumer<String, Map<String, Object>> mockConsumer;

	private KafkaDataCollector kafkaDataCollector;

	private static final String BOOTSTRAP_SERVERS = "localhost:9092";
	private static final String TOPIC = "test-topic";

	@BeforeEach
	void setUp() {
		// KafkaDataCollector 인스턴스 생성 (생성자 주입)
		kafkaDataCollector = new KafkaDataCollector(consumerFactory, BOOTSTRAP_SERVERS, TOPIC);
	}

	@Test
	@DisplayName("최초 호출 시 컨슈머가 생성되고 데이터를 수집한다")
	void collectData_최초호출_컨슈머생성및데이터수집() throws Exception {
		// Given: ConsumerFactory가 mockConsumer를 반환하도록 설정
		when(consumerFactory.createConsumer(BOOTSTRAP_SERVERS, TOPIC)).thenReturn(mockConsumer);

		// Given: mockConsumer.poll()이 빈 레코드를 반환하도록 설정
		ConsumerRecords<String, Map<String, Object>> emptyRecords = createEmptyConsumerRecords();
		when(mockConsumer.poll(Duration.ofSeconds(1))).thenReturn(emptyRecords);

		// When: collectData 호출
		List<CollectData> result = kafkaDataCollector.collectData();

		// Then: ConsumerFactory.createConsumer()가 호출되었는지 검증
		verify(consumerFactory, times(1)).createConsumer(BOOTSTRAP_SERVERS, TOPIC);

		// Then: mockConsumer.poll()이 호출되었는지 검증
		verify(mockConsumer, times(1)).poll(Duration.ofSeconds(1));

		// Then: 빈 리스트가 반환되는지 확인
		assertThat(result).isNotNull()
						  .isEmpty();

		// Then: consumer 필드가 null이 아닌지 리플렉션으로 확인
		Field consumerField = KafkaDataCollector.class.getDeclaredField("consumer");
		consumerField.setAccessible(true);
		Object consumer = consumerField.get(kafkaDataCollector);
		assertThat(consumer).isNotNull();
		log.info("최초 호출 시 컨슈머가 정상적으로 생성되었습니다.");
	}

	@Test
	@DisplayName("컨슈머가 이미 생성된 경우 재사용하고 데이터를 수집한다")
	void collectData_컨슈머재사용() {
		// Given: 첫 번째 호출로 컨슈머 생성
		when(consumerFactory.createConsumer(BOOTSTRAP_SERVERS, TOPIC)).thenReturn(mockConsumer);
		ConsumerRecords<String, Map<String, Object>> emptyRecords = createEmptyConsumerRecords();
		when(mockConsumer.poll(Duration.ofSeconds(1))).thenReturn(emptyRecords);

		kafkaDataCollector.collectData(); // 첫 번째 호출

		// When: 두 번째 호출
		kafkaDataCollector.collectData();

		// Then: ConsumerFactory.createConsumer()가 한 번만 호출되었는지 검증 (재사용)
		verify(consumerFactory, times(1)).createConsumer(BOOTSTRAP_SERVERS, TOPIC);

		// Then: mockConsumer.poll()은 두 번 호출되었는지 검증
		verify(mockConsumer, times(2)).poll(Duration.ofSeconds(1));

		log.info("컨슈머가 재사용되어 두 번 poll이 호출되었습니다.");
	}

	@Test
	@DisplayName("데이터가 없는 경우 빈 리스트를 반환한다")
	void collectData_데이터없음_빈리스트반환() {
		// Given: ConsumerFactory가 mockConsumer를 반환하도록 설정
		when(consumerFactory.createConsumer(BOOTSTRAP_SERVERS, TOPIC)).thenReturn(mockConsumer);

		// Given: poll() 결과가 빈 레코드
		ConsumerRecords<String, Map<String, Object>> emptyRecords = createEmptyConsumerRecords();
		when(mockConsumer.poll(Duration.ofSeconds(1))).thenReturn(emptyRecords);

		// When: collectData 호출
		List<CollectData> result = kafkaDataCollector.collectData();

		// Then: 빈 리스트 반환 확인
		assertThat(result).isNotNull()
						  .isEmpty();

		log.info("데이터가 없을 때 빈 리스트가 정상적으로 반환되었습니다.");
	}

	@Test
	@DisplayName("단일 레코드를 수집하여 CollectData로 변환한다")
	void collectData_단일레코드_정상변환() {
		// Given: ConsumerFactory가 mockConsumer를 반환하도록 설정
		when(consumerFactory.createConsumer(BOOTSTRAP_SERVERS, TOPIC)).thenReturn(mockConsumer);

		// Given: 단일 레코드 생성
		long testTimestamp = System.currentTimeMillis();
		Map<String, Object> recordValue = createRecordValue(1L,
			testTimestamp,
			Map.of("temperature", 25.5, "humidity", 60.0));
		ConsumerRecords<String, Map<String, Object>> records = createConsumerRecords(List.of(new ConsumerRecord<>(TOPIC,
			0,
			0L,
			"key1",
			recordValue)));
		when(mockConsumer.poll(Duration.ofSeconds(1))).thenReturn(records);

		// When: collectData 호출
		List<CollectData> result = kafkaDataCollector.collectData();

		// Then: 결과 검증
		assertThat(result).isNotNull()
						  .hasSize(1);

		CollectData collectData = result.get(0);
		assertThat(collectData.getDcpId()).isEqualTo(1L);

		// Then: 시간 정보 검증 - 타임스탬프를 LocalDateTime으로 변환하여 비교
		LocalDateTime expectedTraceAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(testTimestamp),
			ZoneId.systemDefault());
		assertThat(collectData.getTraceAt()).isEqualTo(expectedTraceAt);

		assertThat(collectData.getValues()).containsEntry("temperature", 25.5);
		assertThat(collectData.getValues()).containsEntry("humidity", 60.0);

		log.info("단일 레코드가 CollectData로 정상 변환되었습니다: dcpId={}, traceAt={}",
			collectData.getDcpId(),
			collectData.getTraceAt());
	}

	@Test
	@DisplayName("여러 레코드를 수집하여 CollectData 리스트로 변환한다")
	void collectData_여러레코드_정상변환() {
		// Given: ConsumerFactory가 mockConsumer를 반환하도록 설정
		when(consumerFactory.createConsumer(BOOTSTRAP_SERVERS, TOPIC)).thenReturn(mockConsumer);

		// Given: 여러 레코드 생성
		long currentTime = System.currentTimeMillis();
		Map<String, Object> record1 = createRecordValue(1L, currentTime, Map.of("temperature", 25.5, "humidity", 60.0));
		Map<String, Object> record2 = createRecordValue(2L,
			currentTime + 1000,
			Map.of("temperature", 26.0, "humidity", 65.0));
		Map<String, Object> record3 = createRecordValue(3L,
			currentTime + 2000,
			Map.of("temperature", 24.5, "humidity", 55.0));

		ConsumerRecords<String, Map<String, Object>> records = createConsumerRecords(List.of(new ConsumerRecord<>(TOPIC,
				0,
				0L,
				"key1",
				record1),
			new ConsumerRecord<>(TOPIC, 0, 1L, "key2", record2),
			new ConsumerRecord<>(TOPIC, 0, 2L, "key3", record3)));
		when(mockConsumer.poll(Duration.ofSeconds(1))).thenReturn(records);

		// When: collectData 호출
		List<CollectData> result = kafkaDataCollector.collectData();

		// Then: 결과 검증
		assertThat(result).isNotNull()
						  .hasSize(3);

		// 각 레코드 검증
		assertThat(result.get(0)
						 .getDcpId()).isEqualTo(1L);
		assertThat(result.get(1)
						 .getDcpId()).isEqualTo(2L);
		assertThat(result.get(2)
						 .getDcpId()).isEqualTo(3L);

		// Then: 시간 정보 검증 - 타임스탬프를 LocalDateTime으로 변환하여 정확하게 비교
		LocalDateTime expectedTraceAt1 = LocalDateTime.ofInstant(Instant.ofEpochMilli(currentTime),
			ZoneId.systemDefault());
		LocalDateTime expectedTraceAt2 = LocalDateTime.ofInstant(Instant.ofEpochMilli(currentTime + 1000),
			ZoneId.systemDefault());
		LocalDateTime expectedTraceAt3 = LocalDateTime.ofInstant(Instant.ofEpochMilli(currentTime + 2000),
			ZoneId.systemDefault());

		assertThat(result.get(0)
						 .getTraceAt()).isEqualTo(expectedTraceAt1);
		assertThat(result.get(1)
						 .getTraceAt()).isEqualTo(expectedTraceAt2);
		assertThat(result.get(2)
						 .getTraceAt()).isEqualTo(expectedTraceAt3);

		// Then: 시간 순서 검증 - 두 번째 레코드가 첫 번째보다 이후, 세 번째가 두 번째보다 이후
		assertThat(result.get(1)
						 .getTraceAt()).isAfter(result.get(0)
													  .getTraceAt());
		assertThat(result.get(2)
						 .getTraceAt()).isAfter(result.get(1)
													  .getTraceAt());

		assertThat(result.get(0)
						 .getValues()).containsEntry("temperature", 25.5);
		assertThat(result.get(1)
						 .getValues()).containsEntry("temperature", 26.0);
		assertThat(result.get(2)
						 .getValues()).containsEntry("temperature", 24.5);

		log.info("여러 레코드가 CollectData 리스트로 정상 변환되었습니다. 레코드 수: {}, 시간 범위: {} ~ {}",
			result.size(),
			result.get(0)
				  .getTraceAt(),
			result.get(2)
				  .getTraceAt());
	}

	@Test
	@DisplayName("commitOffset 호출 시 consumer가 null이면 동작하지 않는다")
	void commitOffset_컨슈머null_동작안함() {
		// Given: consumer가 null인 상태 (collectData를 호출하지 않음)

		// When: commitOffset 호출
		kafkaDataCollector.commitOffset();

		// Then: mockConsumer의 commitSync()가 호출되지 않음
		verify(mockConsumer, never()).commitSync();

		log.info("컨슈머가 null일 때 commitOffset이 정상적으로 처리되었습니다.");
	}

	@Test
	@DisplayName("commitOffset 호출 시 정상적으로 커밋한다")
	void commitOffset_정상커밋() {
		// Given: 컨슈머가 생성된 상태
		when(consumerFactory.createConsumer(BOOTSTRAP_SERVERS, TOPIC)).thenReturn(mockConsumer);
		when(mockConsumer.poll(any(Duration.class))).thenReturn(createEmptyConsumerRecords());

		kafkaDataCollector.collectData(); // 컨슈머 생성

		// When: commitOffset 호출
		kafkaDataCollector.commitOffset();

		// Then: mockConsumer.commitSync()가 호출되었는지 검증
		verify(mockConsumer, times(1)).commitSync();

		log.info("commitOffset이 정상적으로 호출되었습니다.");
	}

	@Test
	@DisplayName("commitOffset 중 예외 발생 시 로그를 남기고 계속 진행한다")
	void commitOffset_예외발생_로그남김() {
		// Given: 컨슈머가 생성된 상태
		when(consumerFactory.createConsumer(BOOTSTRAP_SERVERS, TOPIC)).thenReturn(mockConsumer);
		when(mockConsumer.poll(any(Duration.class))).thenReturn(createEmptyConsumerRecords());

		kafkaDataCollector.collectData(); // 컨슈머 생성

		// Given: commitSync()에서 예외 발생하도록 설정
		doThrow(new RuntimeException("커밋 실패 테스트")).when(mockConsumer)
												  .commitSync();

		// When & Then: 예외가 발생해도 메서드가 정상 종료됨
		kafkaDataCollector.commitOffset();

		verify(mockConsumer, times(1)).commitSync();
		log.info("commitOffset 예외 처리가 정상적으로 동작했습니다.");
	}

	@Test
	@DisplayName("close 호출 시 consumer가 null이면 동작하지 않는다")
	void close_컨슈머null_동작안함() {
		// Given: consumer가 null인 상태

		// When: close 호출
		kafkaDataCollector.close();

		// Then: mockConsumer의 close()가 호출되지 않음
		verify(mockConsumer, never()).close();

		log.info("컨슈머가 null일 때 close가 정상적으로 처리되었습니다.");
	}

	@Test
	@DisplayName("close 호출 시 정상적으로 컨슈머를 닫는다")
	void close_정상종료() {
		// Given: 컨슈머가 생성된 상태
		when(consumerFactory.createConsumer(BOOTSTRAP_SERVERS, TOPIC)).thenReturn(mockConsumer);
		when(mockConsumer.poll(any(Duration.class))).thenReturn(createEmptyConsumerRecords());

		kafkaDataCollector.collectData(); // 컨슈머 생성

		// When: close 호출
		kafkaDataCollector.close();

		// Then: mockConsumer.close()가 호출되었는지 검증
		verify(mockConsumer, times(1)).close();

		log.info("close가 정상적으로 호출되었습니다.");
	}

	@Test
	@DisplayName("close 중 예외 발생 시 로그를 남기고 계속 진행한다")
	void close_예외발생_로그남김() {
		// Given: 컨슈머가 생성된 상태
		when(consumerFactory.createConsumer(BOOTSTRAP_SERVERS, TOPIC)).thenReturn(mockConsumer);
		when(mockConsumer.poll(any(Duration.class))).thenReturn(createEmptyConsumerRecords());

		kafkaDataCollector.collectData(); // 컨슈머 생성

		// Given: close()에서 예외 발생하도록 설정
		doThrow(new RuntimeException("종료 실패 테스트")).when(mockConsumer)
												  .close();

		// When & Then: 예외가 발생해도 메서드가 정상 종료됨
		kafkaDataCollector.close();

		verify(mockConsumer, times(1)).close();
		log.info("close 예외 처리가 정상적으로 동작했습니다.");
	}

	// === 테스트 헬퍼 메서드 ===

	/**
	 * 빈 ConsumerRecords 생성
	 */
	private ConsumerRecords<String, Map<String, Object>> createEmptyConsumerRecords() {
		return new ConsumerRecords<>(Map.of());
	}

	/**
	 * 레코드 값(value) 생성
	 */
	private Map<String, Object> createRecordValue(
		Long dcpId,
		Long timestamp,
		Map<String, Object> values
	) {
		Map<String, Object> recordValue = new HashMap<>();
		recordValue.put("dcpId", dcpId);
		recordValue.put("dateTime", timestamp);
		recordValue.put("values", values);
		return recordValue;
	}

	/**
	 * ConsumerRecords 생성
	 */
	private ConsumerRecords<String, Map<String, Object>> createConsumerRecords(
		List<ConsumerRecord<String, Map<String, Object>>> records
	) {

		TopicPartition topicPartition = new TopicPartition(TOPIC, 0);
		Map<TopicPartition, List<ConsumerRecord<String, Map<String, Object>>>> recordsMap = new HashMap<>();
		recordsMap.put(topicPartition, records);

		return new ConsumerRecords<>(recordsMap);
	}
}
