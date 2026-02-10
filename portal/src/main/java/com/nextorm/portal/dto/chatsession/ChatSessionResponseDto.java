package com.nextorm.portal.dto.chatsession;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nextorm.portal.entity.ChatSession;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatSessionResponseDto {
	private String sessionId;
	private String chatFlowId;
	private String userId;
	private String title;
	@JsonProperty("isFavorite")
	private boolean isFavorite;
	private LocalDateTime createAt;
	private LocalDateTime updateAt;

	public static ChatSessionResponseDto of(ChatSession chatSession) {
		ChatSessionResponseDto dto = new ChatSessionResponseDto();

		dto.sessionId = chatSession.getSessionId();
		dto.chatFlowId = chatSession.getChatFlowId();
		dto.userId = chatSession.getUserId();
		dto.title = chatSession.getTitle();
		dto.isFavorite = chatSession.isFavorite();
		dto.createAt = chatSession.getCreateAt();
		dto.updateAt = chatSession.getUpdateAt();

		return dto;
	}
}
