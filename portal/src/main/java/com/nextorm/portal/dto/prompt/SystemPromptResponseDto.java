package com.nextorm.portal.dto.prompt;

import com.nextorm.portal.entity.prompt.SystemPrompt;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SystemPromptResponseDto {
	private Long id;
	private String message;

	public static SystemPromptResponseDto of(SystemPrompt systemPrompt) {

		return new SystemPromptResponseDto(systemPrompt.getId(), systemPrompt.getMessage());
	}
}
