package com.nextorm.portal.service.prompt;

import com.nextorm.portal.dto.prompt.UserPromptRequestDto;
import com.nextorm.portal.dto.prompt.UserPromptResponseDto;
import com.nextorm.portal.entity.prompt.UserPrompt;
import com.nextorm.portal.repository.prompt.UserPromptRepository;
import com.nextorm.portal.service.auth.SessionService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class UserPromptService {

	private final UserPromptRepository userPromptRepository;
	private final SessionService sessionService;

	public UserPromptService(
		UserPromptRepository userPromptRepository,
		SessionService sessionService
	) {
		this.userPromptRepository = userPromptRepository;
		this.sessionService = sessionService;
	}

	public List<UserPromptResponseDto> getUserPrompt() {

		return userPromptRepository.findByUserIdOrderBySort(sessionService.getLoginId())
								   .stream()
								   .map(UserPromptResponseDto::of)
								   .toList();
	}

	public void modifyUserPrompt(
		UserPromptRequestDto requestDto
	) {

		// 삭제
		if (requestDto.getRemovedPromptIds() != null && !requestDto.getRemovedPromptIds()
																   .isEmpty()) {
			userPromptRepository.deleteAllById(requestDto.getRemovedPromptIds());
		}

		// 추가
		if (requestDto.getAddedPrompts() != null) {
			List<UserPrompt> newPrompts = requestDto.getAddedPrompts()
													.stream()
													.map(item -> (UserPrompt.create(item.getMessage(),
														sessionService.getLoginId(),
														item.getSort())))
													.toList();

			userPromptRepository.saveAll(newPrompts);
		}

		// 수정
		if (requestDto.getUpdatedPrompts() != null) {
			for (UserPromptRequestDto.PromptItem item : requestDto.getUpdatedPrompts()) {
				userPromptRepository.findById(item.getId())
									.ifPresent(userPrompt -> {
										userPrompt.updateMessage(item.getMessage());
										userPrompt.setSort(item.getSort());
									});
			}
		}
	}
}
