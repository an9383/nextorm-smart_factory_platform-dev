package com.nextorm.portal.ai.openai.restapi.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RunCreateRequestDto {
	private String assistantId;
}
