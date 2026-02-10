package com.nextorm.portal.dto.chatsession;

import lombok.Data;

@Data
public class ChatSessionCreateRequestDto {
	private String sessionId;
	private String chatFlowId;
	private String title;
}
