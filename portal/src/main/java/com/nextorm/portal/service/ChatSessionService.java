package com.nextorm.portal.service;

import com.nextorm.portal.dto.chatsession.ChatSessionCreateRequestDto;
import com.nextorm.portal.dto.chatsession.ChatSessionResponseDto;
import com.nextorm.portal.dto.chatsession.ChatSessionUpdateRequestDto;
import com.nextorm.portal.entity.ChatSession;
import com.nextorm.portal.repository.ChatSessionRepository;
import com.nextorm.portal.service.auth.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatSessionService {
	private final ChatSessionRepository chatSessionRepository;
	private final SessionService sessionService;

	public List<ChatSessionResponseDto> getAllByLoginUser() {
		String loginUserId = sessionService.getLoginId();

		return chatSessionRepository.findByUserIdOrderByUpdateAtDesc(loginUserId)
									.stream()
									.map(ChatSessionResponseDto::of)
									.toList();
	}

	public ChatSessionResponseDto create(ChatSessionCreateRequestDto request) {
		ChatSession chatSession = ChatSession.create(request.getSessionId(),
			request.getChatFlowId(),
			request.getTitle(),
			sessionService.getLoginId());

		return ChatSessionResponseDto.of(chatSessionRepository.save(chatSession));
	}

	public ChatSessionResponseDto update(
		String sessionId,
		ChatSessionUpdateRequestDto request
	) {
		ChatSession chatSession = chatSessionRepository.findById(sessionId)
													   .orElseThrow(() -> new IllegalArgumentException(
														   "채팅 세션을 찾을 수 없습니다."));
		chatSession.updateTitle(request.getTitle());
		return ChatSessionResponseDto.of(chatSession);
	}

	public ChatSessionResponseDto updateFavorite(
		String sessionId,
		boolean isFavorite
	) {
		ChatSession chatSession = chatSessionRepository.findById(sessionId)
													   .orElseThrow(() -> new IllegalArgumentException(
														   "채팅 세션을 찾을 수 없습니다."));
		chatSession.updateFavorite(isFavorite);
		return ChatSessionResponseDto.of(chatSession);
	}

	public void delete(String sessionId) {
		chatSessionRepository.deleteById(sessionId);
	}
}
