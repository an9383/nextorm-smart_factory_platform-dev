package com.nextorm.portal.restapi.dto.deepl;

import lombok.Data;

import java.util.List;

@Data
public class TranslationResponseDto {
	private List<TranslationDto> translations;
}
