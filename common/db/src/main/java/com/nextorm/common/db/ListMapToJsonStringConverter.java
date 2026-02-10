package com.nextorm.common.db;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class ListMapToJsonStringConverter implements AttributeConverter<List<Map<String, Object>>, String> {
	private final ObjectMapper objectMapper;

	@Override
	public String convertToDatabaseColumn(List<Map<String, Object>> stringObjectMap) {
		try {
			return objectMapper.writeValueAsString(stringObjectMap);
		} catch (Exception e) {
			log.debug("Failed to convert map to json string", e);
			return null;
		}
	}

	@Override
	public List<Map<String, Object>> convertToEntityAttribute(String s) {
		try {
			TypeReference<List<Map<String, Object>>> typeReference = new TypeReference<>() {
			};
			return objectMapper.readValue(s, typeReference);
		} catch (Exception e) {
			log.debug("Failed to convert json string to map", e);
			return new ArrayList<>();
		}
	}
}
