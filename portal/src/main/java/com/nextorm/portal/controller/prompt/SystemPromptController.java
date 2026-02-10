package com.nextorm.portal.controller.prompt;

import com.nextorm.portal.dto.prompt.SystemPromptResponseDto;
import com.nextorm.portal.service.prompt.SystemPromptService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/system-prompt")
@RequiredArgsConstructor
public class SystemPromptController {

	private final SystemPromptService systemPromptService;

	@GetMapping
	public List<SystemPromptResponseDto> getSystemPrompt() {
		return systemPromptService.getSystemPrompt();
	}
}
