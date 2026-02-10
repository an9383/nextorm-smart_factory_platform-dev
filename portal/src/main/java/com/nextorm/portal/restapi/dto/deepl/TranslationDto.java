package com.nextorm.portal.restapi.dto.deepl;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TranslationDto {
	@JsonProperty("detected_source_language")
	private String detectedSourceLanguage;
	private String text;
}
