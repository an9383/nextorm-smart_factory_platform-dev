package com.nextorm.portal.controller;

import com.nextorm.portal.ai.openai.dto.RecievedMessageDto;
import com.nextorm.portal.dto.chat.ChatMessageRequestDto;
import com.nextorm.portal.service.OpenAIChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/open-ai/chat")
public class OpenAIChatController {

	private final OpenAIChatService openAIChatService;

	@PostMapping("/message")
	public ResponseEntity<RecievedMessageDto> sendMessage(@RequestBody ChatMessageRequestDto chatMessageRequest) {
		return ResponseEntity.ok(openAIChatService.sendMessage(chatMessageRequest.getThreadId(),
			chatMessageRequest.getMessage()));
	}
}
