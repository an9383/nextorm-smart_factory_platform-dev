package com.nextorm.processor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Properties;

@Service
@RequiredArgsConstructor
public class ConsumerFactory {
	private final ObjectMapper objectMapper;

	/**
	 * 컨슈머 생성 및 토픽 구독
	 */
	public KafkaConsumer<String, Map<String, Object>> createConsumer(
		String bootstrapServers,
		String topic
	) {
		var keyDeserializer = new StringDeserializer();
		var valueDeserializer = new MapDeserializer<String, Object>(objectMapper);

		var consumer = new KafkaConsumer<>(createProperties(bootstrapServers, topic),
			keyDeserializer,
			valueDeserializer);
		consumer.subscribe(List.of(topic));
		return consumer;
	}

	private Properties createProperties(
		String bootstrapServers,
		String topic
	) {
		var groupId = topic + "_group";
		var configs = new Properties();
		configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		configs.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		configs.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
		return configs;
	}
}
