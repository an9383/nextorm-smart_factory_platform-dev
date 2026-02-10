package com.nextorm.processor.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;
import java.util.Map;

/**
 * 카프카 메시지를 Map으로 역질렬화 하는 클래스
 *
 * @param <K>
 * @param <V>
 */
@RequiredArgsConstructor
public class MapDeserializer<K, V> implements Deserializer<Map<K, V>> {
	private final ObjectMapper mapper;

	private static class MapDeserializeException extends RuntimeException {
		public MapDeserializeException(
			String message,
			Throwable cause
		) {
			super(message, cause);
		}
	}

	@Override
	public Map<K, V> deserialize(
		String topic,
		byte[] data
	) {
		try {
			return mapper.readValue(data, new TypeReference<Map<K, V>>() {
			});
		} catch (IOException e) {
			String message = "Failed to deserialize the topic / message: %s, %s".formatted(topic, new String(data));
			throw new MapDeserializeException(message, e);
		}
	}
}
