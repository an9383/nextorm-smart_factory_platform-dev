package com.nextorm.portal.service;

import com.nextorm.common.db.entity.CollectorConfig;
import com.nextorm.common.db.entity.Tool;
import com.nextorm.common.db.repository.CollectorConfigRepository;
import com.nextorm.common.db.repository.ToolRepository;
import com.nextorm.portal.common.exception.collectorconfig.CollectorConfigNameDuplicationException;
import com.nextorm.portal.dto.collectorconfig.CollectorConfigCreateRequestDto;
import com.nextorm.portal.dto.collectorconfig.CollectorConfigResponseDto;
import com.nextorm.portal.dto.collectorconfig.CollectorConfigUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class CollectorConfigService {
	private final CollectorConfigRepository collectorConfigRepository;
	private final ToolRepository toolRepository;

	public List<CollectorConfigResponseDto> getAll() {
		return collectorConfigRepository.findAll()
										.stream()
										.map(collectorConfig -> CollectorConfigResponseDto.from(collectorConfig,
											collectorConfig.getTools()))
										.toList();
	}

	public CollectorConfigResponseDto create(
		CollectorConfigCreateRequestDto collectorConfigCreateRequestDto
	) {
		List<Tool> tools = toolRepository.findAllById(collectorConfigCreateRequestDto.getToolIds());
		Optional<CollectorConfig> optionalSummaryConfig = collectorConfigRepository.findOneByName(
			collectorConfigCreateRequestDto.getName());
		if (optionalSummaryConfig.isPresent()) {
			throw new CollectorConfigNameDuplicationException(collectorConfigCreateRequestDto.getName());
		}
		CollectorConfig collectorConfig = CollectorConfig.create(collectorConfigCreateRequestDto.toEntity());

		collectorConfig.addTools(tools);
		collectorConfigRepository.save(collectorConfig);

		return CollectorConfigResponseDto.from(collectorConfig, tools);
	}

	public CollectorConfigResponseDto modify(
		Long configId,
		CollectorConfigUpdateRequestDto collectorConfigUpdateRequestDto
	) {
		CollectorConfig collectorConfig = collectorConfigRepository.findByIdWithTools(configId);
		List<Tool> tools = toolRepository.findAllById(collectorConfigUpdateRequestDto.getToolIds());

		//수정시에도 이름 중복 예외처리 필요 (중복 로직으로 메서드로 빼도 되는지?)
		//수정을 누른 후 변경하지 않고 저장하면 원래 이름이 존재하기 때문에 에러처리가 됨
		Optional<CollectorConfig> optionalSummaryConfig = collectorConfigRepository.findOneByName(
			collectorConfigUpdateRequestDto.getName());
		if (optionalSummaryConfig.isPresent() && !collectorConfig.getId()
																 .equals(optionalSummaryConfig.get()
																							  .getId())) {
			throw new CollectorConfigNameDuplicationException(collectorConfigUpdateRequestDto.getName());
		}

		collectorConfig.modify(collectorConfigUpdateRequestDto.toEntity(), tools);

		return CollectorConfigResponseDto.from(collectorConfig, tools);
	}

	public void delete(Long id) {
		collectorConfigRepository.deleteById(id);
	}

}
