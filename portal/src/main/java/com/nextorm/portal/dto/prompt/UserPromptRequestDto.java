package com.nextorm.portal.dto.prompt;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserPromptRequestDto {
	private List<PromptItem> addedPrompts = new ArrayList<>();
	private List<PromptItem> updatedPrompts = new ArrayList<>();
	private List<Long> removedPromptIds = new ArrayList<>();

	@Data
	public static class PromptItem {
		private Long id;
		private String message;
		private Integer sort;
	}
}
