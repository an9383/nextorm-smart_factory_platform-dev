package com.nextorm.processor.worker.datacollector;

import com.nextorm.processor.service.ConsumerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class KafkaDataCollector implements DataCollector {
	private final ConsumerFactory consumerFactory;
	private final String bootstrapServers;
	private final String topic;

	private KafkaConsumer<String, Map<String, Object>> consumer = null;

	@Override
	public List<CollectData> collectData() {
		if (consumer == null) {
			consumer = consumerFactory.createConsumer(bootstrapServers, topic);
		}
		ConsumerRecords<String, Map<String, Object>> records = consumer.poll(Duration.ofSeconds(1));
		return CollectData.listOfConsumerRecords(records);
	}

	@Override
	public void commitOffset() {
		if (consumer != null) {
			try {
				consumer.commitSync();
			} catch (Exception e) {
				log.error("카프카 컨슈머 commitOffset중 에러 발생", e);
			}
		}
	}

	@Override
	public void close() {
		if (consumer != null) {
			try {
				consumer.close();
			} catch (Exception e) {
				log.error("카프카 컨슈머 close중 에러 발생", e);
			}
		}
	}
}
