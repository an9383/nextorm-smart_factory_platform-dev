package com.nextorm.portal.ai.openai.restapi.dto;

import lombok.Data;

import java.util.List;

@Data
public class MessageResponseDto {

	@Data
	public static class Content {
		private String type;
		private Text text;
	}

	@Data
	public static class Text {
		private String value;
	}

	private String id;
	private Long createdAt;
	private String threadId;
	private String role;
	private List<Content> content;
}
