package com.nextorm.common.db.entity.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
@Converter
public class ListLongToJsonStringConverter implements AttributeConverter<List<Long>, String> {
	private final ObjectMapper objectMapper;

	@Override
	public String convertToDatabaseColumn(List<Long> stringObjectMap) {
		try {
			return objectMapper.writeValueAsString(stringObjectMap);
		} catch (Exception e) {
			log.debug("Failed to convert map to json string", e);
			return null;
		}
	}

	@Override
	public List<Long> convertToEntityAttribute(String s) {
		try {
			TypeReference<List<Long>> typeReference = new TypeReference<>() {
			};
			return objectMapper.readValue(s, typeReference);
		} catch (Exception e) {
			log.debug("Failed to convert json string to map", e);
			return new ArrayList<>();
		}
	}
}
