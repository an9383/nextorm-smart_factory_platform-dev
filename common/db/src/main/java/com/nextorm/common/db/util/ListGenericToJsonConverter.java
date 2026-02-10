package com.nextorm.common.db.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextorm.common.db.exception.jsonconverter.JsonConverterException;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Converter
public class ListGenericToJsonConverter<T> implements AttributeConverter<List<T>, String> {
	private final ObjectMapper objectMapper;
	private final TypeReference<List<T>> typeReference;

	@Override
	public String convertToDatabaseColumn(List<T> genericList) {
		if (genericList == null || genericList.isEmpty()) {
			return null;
		}
		try {
			return objectMapper.writeValueAsString(genericList);
		} catch (Exception e) {
			log.debug("객체 리스트를 JSON 문자열로 변환 실패", e);
			throw new JsonConverterException();
		}
	}

	@Override
	public List<T> convertToEntityAttribute(String entityFiled) {
		if (entityFiled == null || entityFiled.isEmpty()) {
			return new ArrayList<>();
		}
		try {
			return objectMapper.readValue(entityFiled, typeReference);
		} catch (Exception e) {
			log.debug("JSON 문자열을 객체로 변환 실패", e);
			throw new JsonConverterException();
		}
	}
}
