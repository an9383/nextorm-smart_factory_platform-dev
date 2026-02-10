package com.nextorm.portal.controller.prompt;

import com.nextorm.portal.dto.prompt.UserPromptRequestDto;
import com.nextorm.portal.dto.prompt.UserPromptResponseDto;
import com.nextorm.portal.service.prompt.UserPromptService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-prompt")
@RequiredArgsConstructor
public class UserPromptController {

	private final UserPromptService userPromptService;

	@GetMapping
	public List<UserPromptResponseDto> getUserPrompt() {
		return userPromptService.getUserPrompt();
	}

	/**
	 * user prompt 추가, 수정 , 삭제 통합 API
	 *
	 * @param requestDto
	 */
	@PostMapping()
	public void modifyUserPrompt(@RequestBody UserPromptRequestDto requestDto) {
		userPromptService.modifyUserPrompt(requestDto);
	}
}
