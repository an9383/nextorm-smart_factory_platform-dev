package com.nextorm.portal.service;

import com.nextorm.common.db.entity.SummaryConfig;
import com.nextorm.common.db.entity.Tool;
import com.nextorm.common.db.repository.SummaryConfigRepository;
import com.nextorm.common.db.repository.ToolRepository;
import com.nextorm.portal.common.exception.summaryconfig.SummaryConfigNameDuplicationException;
import com.nextorm.portal.dto.summaryconfig.SummaryConfigCreateRequestDto;
import com.nextorm.portal.dto.summaryconfig.SummaryConfigResponseDto;
import com.nextorm.portal.dto.summaryconfig.SummaryConfigUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class SummaryConfigService {
	private final SummaryConfigRepository summaryConfigRepository;
	private final ToolRepository toolRepository;

	public List<SummaryConfigResponseDto> getSummaryConfigs() {
		return summaryConfigRepository.findAll()
									  .stream()
									  .map(summaryConfig -> SummaryConfigResponseDto.from(summaryConfig,
										  summaryConfig.getTools()))
									  .toList();
	}

	public SummaryConfigResponseDto createSummaryConfig(
		SummaryConfigCreateRequestDto summaryConfigCreateRequestDto
	) {
		List<Tool> tools = toolRepository.findAllById(summaryConfigCreateRequestDto.getToolIds());
		Optional<SummaryConfig> optionalSummaryConfig = summaryConfigRepository.findOneByName(
			summaryConfigCreateRequestDto.getName());
		if (optionalSummaryConfig.isPresent()) {
			throw new SummaryConfigNameDuplicationException(summaryConfigCreateRequestDto.getName());
		}
		SummaryConfig summaryConfig = SummaryConfig.create(summaryConfigCreateRequestDto.toEntity());
		summaryConfig.addTools(tools);
		summaryConfigRepository.save(summaryConfig);

		return SummaryConfigResponseDto.from(summaryConfig, tools);
	}

	public SummaryConfigResponseDto modifySummaryConfig(
		Long configId,
		SummaryConfigUpdateRequestDto summaryConfigUpdateRequestDto
	) {
		SummaryConfig summaryConfig = summaryConfigRepository.findByIdWithTools(configId);
		boolean isNameChanged = !summaryConfig.getName()
											  .equals(summaryConfigUpdateRequestDto.getName());

		if (isNameChanged) {
			summaryConfigRepository.findOneByName(summaryConfigUpdateRequestDto.getName())
								   .ifPresent(duplicate -> {
									   throw new SummaryConfigNameDuplicationException(summaryConfigUpdateRequestDto.getName());
								   });
		}

		List<Tool> tools = toolRepository.findAllById(summaryConfigUpdateRequestDto.getToolIds());
		summaryConfig.modify(summaryConfigUpdateRequestDto.toEntity(), tools);

		return SummaryConfigResponseDto.from(summaryConfig, tools);
	}

	public void deleteSummaryConfig(Long id) {
		summaryConfigRepository.deleteById(id);
	}

}
