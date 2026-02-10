package com.nextorm.collector.sender;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.LocalDateTime;
import java.util.Properties;
import java.util.concurrent.Future;

@Slf4j
public class KafkaSender implements Sender {
	private final String CONNECTION_CHECK_SEND_TOPIC = "CONNECTION_TEST";
	/**
	 * 200ms 이내에 카프카에 연결이 되어야 한다
	 */
	private final Long CONNECTION_TEST_PRODUCER_MAX_BLOCK_MS = 500L;
	private final long BROKER_CONNECTION_RETRY_DELAY_MS = 5000L;    // 재연결 시도에 5초 대기
	private final int BROKER_CONNECTION_RETRY_COUNT = 3;

	private final ObjectMapper objectMapper;
	private final String topic;

	private final KafkaProducer<String, String> producer;

	public KafkaSender(
		ObjectMapper objectMapper,
		String bootstrapServers,
		String topic
	) {
		this.objectMapper = objectMapper;
		this.topic = topic;

		Properties properties = new Properties();
		properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

		int retryCount = 0;
		while (retryCount < BROKER_CONNECTION_RETRY_COUNT) {
			try {
				brokerConnectionTest(properties);
				break;
			} catch (KafkaConnectionFailException e) {
				retryCount++;
				log.info("카프카 브로커 연결 테스트 재시도 {}/{}", retryCount, BROKER_CONNECTION_RETRY_COUNT);
				if (retryCount >= BROKER_CONNECTION_RETRY_COUNT) {
					log.error("카프카 브로커 연결 테스트 실패 횟수 초과. 프로그램을 종료합니다.");
					System.exit(1);
				}
				try {
					Thread.sleep(BROKER_CONNECTION_RETRY_DELAY_MS);
				} catch (InterruptedException ex) {
					log.error("카프카 브로커 연결 테스트 재시도 대기중 InterruptedException 발생. 프로그램 종료");
					System.exit(1);
				}
			}
		}

		this.producer = new KafkaProducer<String, String>(properties);
	}

	private void brokerConnectionTest(Properties properties) {
		Properties testProductProperties = new Properties();
		testProductProperties.putAll(properties);
		testProductProperties.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, CONNECTION_TEST_PRODUCER_MAX_BLOCK_MS);

		// 메시지 전송에 에러가 발생하면, 브로커 연결에 문제가 있으니 프로그램을 종료한다
		try (KafkaProducer<String, String> testProducer = new KafkaProducer<>(testProductProperties)) {
			String message = LocalDateTime.now() + "__" + this.topic;
			Future<RecordMetadata> sendFuture = testProducer.send(new ProducerRecord<>(CONNECTION_CHECK_SEND_TOPIC,
				message));
			RecordMetadata metadata = sendFuture.get();

			log.info("카프카 브로커 연결 테스트 성공. Message sent to topic: {}, partition: {}, offset: {}",
				metadata.topic(),
				metadata.partition(),
				metadata.offset());

		} catch (Exception e) {
			throw new KafkaConnectionFailException("카프카 브로커 연결 테스트 실패", e);
		}
	}

	@Override
	public void send(SendMessage message) {
		this.send(this.topic, message);
	}

	@Override
	public void send(
		String topic,
		SendMessage message
	) {
		String messageString = messageToString(message);
		log.info("send To: {} - {}", topic, messageString);
		this.producer.send(new ProducerRecord<>(topic, messageString));
	}

	private String messageToString(SendMessage message) {
		try {
			return objectMapper.writeValueAsString(message);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}
