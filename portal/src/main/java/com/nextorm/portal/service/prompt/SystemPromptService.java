package com.nextorm.portal.service.prompt;

import com.nextorm.portal.dto.prompt.SystemPromptResponseDto;
import com.nextorm.portal.repository.prompt.SystemPromptRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class SystemPromptService {
	private final SystemPromptRepository systemPromptRepository;

	public List<SystemPromptResponseDto> getSystemPrompt() {

		return systemPromptRepository.findAll()
									 .stream()
									 .map(SystemPromptResponseDto::of)
									 .toList();
	}
}
