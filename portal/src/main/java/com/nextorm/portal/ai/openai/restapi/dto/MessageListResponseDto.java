package com.nextorm.portal.ai.openai.restapi.dto;

import lombok.Data;

import java.util.List;

@Data
public class MessageListResponseDto {
	List<MessageResponseDto> data;
}
