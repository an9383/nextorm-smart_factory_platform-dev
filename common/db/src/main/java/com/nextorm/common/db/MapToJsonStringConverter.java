package com.nextorm.common.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@Component
public class MapToJsonStringConverter implements AttributeConverter<Map<String, Object>, String> {
	private final ObjectMapper objectMapper;

	@Override
	public String convertToDatabaseColumn(Map<String, Object> stringObjectMap) {
		try {
			return objectMapper.writeValueAsString(stringObjectMap);
		} catch (Exception e) {
			log.debug("Failed to convert map to json string", e);
			return null;
		}
	}

	@Override
	public Map<String, Object> convertToEntityAttribute(String s) {
		try {
			return objectMapper.readValue(s, Map.class);
		} catch (Exception e) {
			log.debug("Failed to convert json string to map", e);
			return Map.of();
		}
	}
}
