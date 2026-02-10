package com.nextorm.portal.restapi.dto.deepl;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TranslationRequestDto {
	private List<String> text;
	@JsonProperty("target_lang")
	private String targetLang;
}
