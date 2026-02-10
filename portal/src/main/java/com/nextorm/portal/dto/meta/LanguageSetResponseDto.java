package com.nextorm.portal.dto.meta;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class LanguageSetResponseDto {
	private Map<String, String> messages;
}
