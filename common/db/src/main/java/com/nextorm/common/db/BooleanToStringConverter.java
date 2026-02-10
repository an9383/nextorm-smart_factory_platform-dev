package com.nextorm.common.db;

import jakarta.persistence.AttributeConverter;
import org.springframework.stereotype.Component;

@Component
public class BooleanToStringConverter implements AttributeConverter<Boolean, String> {

	@Override
	public String convertToDatabaseColumn(Boolean value) {
		return (value != null && value)
			   ? "Y"
			   : "N";
	}

	@Override
	public Boolean convertToEntityAttribute(String value) {
		return "Y".equals(value);
	}
}