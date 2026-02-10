package com.nextorm.portal.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatMessageRequestDto {
	private String threadId;
	private String message;
}
