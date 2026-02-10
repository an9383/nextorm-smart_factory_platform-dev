package com.nextorm.portal.controller;

import com.nextorm.portal.dto.chatsession.ChatSessionCreateRequestDto;
import com.nextorm.portal.dto.chatsession.ChatSessionFavoriteUpdateRequestDto;
import com.nextorm.portal.dto.chatsession.ChatSessionResponseDto;
import com.nextorm.portal.dto.chatsession.ChatSessionUpdateRequestDto;
import com.nextorm.portal.service.ChatSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat-sessions")
@RequiredArgsConstructor
public class ChatSessionController {

	private final ChatSessionService chatSessionService;

	@GetMapping
	public List<ChatSessionResponseDto> getLoginUserChatSessions() {
		return chatSessionService.getAllByLoginUser();
	}

	@PostMapping
	public ChatSessionResponseDto createChatSession(@RequestBody ChatSessionCreateRequestDto request) {
		return chatSessionService.create(request);
	}

	@PutMapping("/{sessionId}")
	public ChatSessionResponseDto updateChatSession(
		@PathVariable String sessionId,
		@RequestBody ChatSessionUpdateRequestDto request
	) {
		return chatSessionService.update(sessionId, request);
	}

	@PutMapping("/{sessionId}/favorite")
	public ChatSessionResponseDto updateChatSessionFavorite(
		@PathVariable String sessionId,
		@RequestBody ChatSessionFavoriteUpdateRequestDto request
	) {
		return chatSessionService.updateFavorite(sessionId, request.isFavorite());
	}

	@DeleteMapping("/{sessionId}")
	public void deleteChatSession(@PathVariable String sessionId) {
		chatSessionService.delete(sessionId);
	}
}
