package com.nextorm.portal.service;

import com.nextorm.common.db.entity.ProcessConfig;
import com.nextorm.common.db.entity.Tool;
import com.nextorm.common.db.repository.ProcessConfigRepository;
import com.nextorm.common.db.repository.ToolRepository;
import com.nextorm.portal.common.exception.processconfig.ProcessConfigNameDuplicationException;
import com.nextorm.portal.common.exception.processconfig.ProcessConfigNotFoundException;
import com.nextorm.portal.dto.processconfig.ProcessConfigCreateRequestDto;
import com.nextorm.portal.dto.processconfig.ProcessConfigResponseDto;
import com.nextorm.portal.dto.processconfig.ProcessConfigUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ProcessConfigService {
	private final ProcessConfigRepository processConfigRepository;
	private final ToolRepository toolRepository;

	public List<ProcessConfigResponseDto> getProcessConfigs() {
		return processConfigRepository.findAll()
									  .stream()
									  .map(processConfig -> ProcessConfigResponseDto.from(processConfig,
										  processConfig.getTools()))
									  .toList();
	}

	public ProcessConfigResponseDto createProcessConfig(
		ProcessConfigCreateRequestDto processConfigCreateRequestDto
	) {
		List<Tool> tools = toolRepository.findAllById(processConfigCreateRequestDto.getToolIds());
		Optional<ProcessConfig> optionalProcessConfig = processConfigRepository.findOneByName(
			processConfigCreateRequestDto.getName());
		if (optionalProcessConfig.isPresent()) {
			throw new ProcessConfigNameDuplicationException(processConfigCreateRequestDto.getName());
		}
		ProcessConfig processConfig = ProcessConfig.create(processConfigCreateRequestDto.toEntity());

		processConfig.addTools(tools);
		processConfigRepository.save(processConfig);

		return ProcessConfigResponseDto.from(processConfig, tools);
	}

	public ProcessConfigResponseDto modifyProcessConfig(
		Long configId,
		ProcessConfigUpdateRequestDto processConfigUpdateRequestDto
	) {
		ProcessConfig processConfig = processConfigRepository.findByIdWithTools(configId);
		if (processConfig == null) {
			throw new ProcessConfigNotFoundException();
		}
		boolean isNameChanged = !processConfig.getName()
											  .equals(processConfigUpdateRequestDto.getName());
		if (isNameChanged) {
			processConfigRepository.findOneByName(processConfigUpdateRequestDto.getName())
								   .ifPresent(duplicate -> {
									   throw new ProcessConfigNameDuplicationException(processConfigUpdateRequestDto.getName());
								   });
		}

		List<Tool> tools = toolRepository.findAllById(processConfigUpdateRequestDto.getToolIds());

		processConfig.modify(processConfigUpdateRequestDto.toEntity(), tools);

		return ProcessConfigResponseDto.from(processConfig, tools);
	}

	public void deleteProcessConfig(Long id) {
		processConfigRepository.deleteById(id);
	}

}
