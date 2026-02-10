package com.nextorm.portal.dto.prompt;

import com.nextorm.portal.entity.prompt.UserPrompt;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserPromptResponseDto {
	private Long id;
	private String message;
	private String userId;
	private Integer sort;

	public static UserPromptResponseDto of(UserPrompt userPrompt) {
		return new UserPromptResponseDto(userPrompt.getId(),
			userPrompt.getMessage(),
			userPrompt.getUserId(),
			userPrompt.getSort());
	}
}
