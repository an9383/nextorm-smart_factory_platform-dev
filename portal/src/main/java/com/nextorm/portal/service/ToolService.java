package com.nextorm.portal.service;

import com.nextorm.common.db.entity.*;
import com.nextorm.common.db.repository.*;
import com.nextorm.common.db.repository.dto.ToolSearchParam;
import com.nextorm.portal.common.exception.ConstraintViloationException;
import com.nextorm.portal.common.exception.location.LocationNotFoundException;
import com.nextorm.portal.common.exception.tool.RelateToolNotFoundException;
import com.nextorm.portal.common.exception.tool.ToolNameDuplicationException;
import com.nextorm.portal.common.exception.tool.ToolNotFoundException;
import com.nextorm.portal.common.exception.toolkafka.ToolKafkaAlreadyExistsException;
import com.nextorm.portal.common.exception.toolkafka.ToolKafkaNotFoundException;
import com.nextorm.portal.dto.common.ConstraintViloationDto;
import com.nextorm.portal.dto.tool.*;
import com.nextorm.portal.entity.system.RefreshEvent;
import com.nextorm.portal.event.RefreshMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ToolService {
	private final ApplicationEventPublisher eventPublisher;
	private final ToolRepository toolRepository;
	private final ParameterRepository parameterRepository;
	private final ParameterDataRepository parameterDataRepository;
	private final DcpConfigRepository dcpConfigRepository;
	private final LocationRepository locationRepository;
	private final ToolKafkaRepository toolKafkaRepository;
	private final RefreshMessageService refreshMessageService;

	public List<ToolResponseDto> getTools(ToolSearchRequestDto toolSearchRequestDto) {
		ToolSearchParam toolSearchParam = ToolSearchParam.builder()
														 .locationId(toolSearchRequestDto.getLocationId())
														 .build();
		return toolRepository.findBySearchParams(toolSearchParam)
							 .stream()
							 .map(ToolResponseDto::from)
							 .toList();
	}

	public ToolResponseDto getTool(Long toolId) {
		return toolRepository.findById(toolId)
							 .map(ToolResponseDto::from)
							 .orElseThrow(ToolNotFoundException::new);
	}

	public ToolResponseDto createTool(ToolCreateRequestDto toolCreateRequestDto) {
		Optional<Tool> optionalTool = toolRepository.findByLocationIdAndName(toolCreateRequestDto.getLocationId(),
			toolCreateRequestDto.getName());

		if (optionalTool.isPresent()) {
			throw new ToolNameDuplicationException(toolCreateRequestDto.getName());
		}
		Location location = locationRepository.findById(toolCreateRequestDto.getLocationId())
											  .orElseThrow(LocationNotFoundException::new);

		ToolResponseDto toolResponseDto = ToolResponseDto.from(toolRepository.save(toolCreateRequestDto.toEntity(
			location)));
		eventPublisher.publishEvent(new RefreshEvent(toolResponseDto.getId(), "CREATE_TOOL"));
		return toolResponseDto;
	}

	public ToolResponseDto modifyTool(
		Long toolId,
		ToolUpdateRequestDto toolUpdateRequestDto
	) {
		Tool tool = toolRepository.findById(toolId)
								  .orElseThrow(ToolNotFoundException::new);

		boolean isNameChanged = !tool.getName()
									 .equals(toolUpdateRequestDto.getName());
		if (isNameChanged) {
			toolRepository.findByLocationIdAndName(toolUpdateRequestDto.getLocationId(), toolUpdateRequestDto.getName())
						  .ifPresent(duplicate -> {
							  throw new ToolNameDuplicationException(toolUpdateRequestDto.getName());
						  });
		}
		Location location = locationRepository.findById(toolUpdateRequestDto.getLocationId())
											  .orElseThrow(LocationNotFoundException::new);
		tool.modify(toolUpdateRequestDto.toEntity(), location);
		eventPublisher.publishEvent(new RefreshEvent(toolId, "MODIFY_TOOL"));
		return ToolResponseDto.from(tool);
	}

	public void deleteTool(Long toolId) {
		List<ConstraintViloationDto> exists = new ArrayList<>();

		List<Parameter> parameters = parameterRepository.findByToolId(toolId);
		if (!parameters.isEmpty()) {
			exists.addAll(parameters.stream()
									.map(parameter -> new ConstraintViloationDto("Modeling > Parameter",
										parameter.getId(),
										parameter.getName(),
										null,
										null))
									.toList());
		}

		List<DcpConfig> dcpConfigs = dcpConfigRepository.findByToolId(toolId);
		if (!dcpConfigs.isEmpty()) {
			exists.addAll(dcpConfigs.stream()
									.map(dcpConfig -> new ConstraintViloationDto("Modeling > Dcp Config",
										dcpConfig.getId(),
										dcpConfig.getTopic(),
										null,
										null))
									.toList());
		}

		if (!exists.isEmpty()) {
			throw new ConstraintViloationException(exists);
		}

		toolRepository.deleteById(toolId);
		eventPublisher.publishEvent(new RefreshEvent(toolId, "DELETE_TOOL"));
	}

	public void deleteTools(List<Long> toolIds) throws ConstraintViloationException {
		List<ConstraintViloationDto> constraintDatas = new ArrayList<>();
		for (Long toolId : toolIds) {
			try {
				this.deleteTool(toolId);
			} catch (ConstraintViloationException e) {
				constraintDatas.addAll(e.getData());
			}
		}
		if (!constraintDatas.isEmpty()) {
			throw new ConstraintViloationException(constraintDatas);
		}
	}

	public ToolKafkaResponseDto getToolKafka(Long toolId) {
		ToolKafka toolKafka = toolKafkaRepository.findByToolId(toolId)
												 .orElseThrow(ToolKafkaNotFoundException::new);
		return ToolKafkaResponseDto.from(toolKafka);
	}

	public boolean checkExistsToolKafka(Long toolId) {
		Optional<ToolKafka> toolKafka = toolKafkaRepository.findByToolId(toolId);
		return toolKafka.isPresent();
	}

	public void createToolKafka(
		Long toolId,
		ToolKafkaCreateRequestDto toolKafkaCreateRequestDto
	) {
		Tool tool = toolRepository.findById(toolId)
								  .orElseThrow(RelateToolNotFoundException::new);

		String topic = "TOOL" + "_" + tool.getId() + "_" + UUID.randomUUID()
															   .toString()
															   .substring(0, 6);

		Optional<ToolKafka> isExistsToolKafka = toolKafkaRepository.findByToolId(toolId);
		if (isExistsToolKafka.isPresent()) {
			throw new ToolKafkaAlreadyExistsException();
		}

		toolKafkaRepository.save(ToolKafka.create(toolId, toolKafkaCreateRequestDto.getBootstrapServer(), topic));
	}

	public void modifyToolKafka(
		Long toolId,
		ToolKafkaUpdateRequestDto toolKafkaUpdateRequestDto
	) {
		ToolKafka toolKafka = toolKafkaRepository.findByToolId(toolId)
												 .orElseThrow(ToolKafkaNotFoundException::new);
		toolKafka.modify(toolKafkaUpdateRequestDto.getBootstrapServer());
	}

	public List<ToolCollectStatusResponseDto> getToolCollectStatus() {
		LocalDateTime currentDateTime = LocalDateTime.now();

		List<Tool> tools = toolRepository.findAll();

		List<Long> toolIds = tools.stream()
								  .map(Tool::getId)
								  .collect(Collectors.toList());

		List<DcpConfig> allDcpConfigs = dcpConfigRepository.findByToolIdIn(toolIds);
		Map<Tool, List<DcpConfig>> toolIdToDcpConfigsMap = allDcpConfigs.stream()
																		.collect(Collectors.groupingBy(DcpConfig::getTool));

		List<Parameter> allParameters = parameterRepository.findByToolIdIn(toolIds);
		Map<Tool, List<Parameter>> toolIdToParametersMap = allParameters.stream()
																		.collect(Collectors.groupingBy(Parameter::getTool));

		List<Long> allParameterIds = allParameters.stream()
												  .map(Parameter::getId)
												  .toList();

		//최신 1시간 ParameterData를 배치로 조회
		Map<Long, ParameterData> parameterDataMap = fetchLatestParameterDataInBatches(allParameterIds,
			currentDateTime.minusHours(1));

		//DcpConfig와 Parameter 간의 매핑 생성 (Parameter ID -> DcpConfig)
		Map<Long, DcpConfig> parameterIdToDcpConfigMap = new HashMap<>();
		for (DcpConfig dcpConfig : allDcpConfigs) {
			List<Parameter> parameters = dcpConfig.getParameters();
			if (parameters != null) {
				for (Parameter parameter : parameters) {
					parameterIdToDcpConfigMap.put(parameter.getId(), dcpConfig);
				}
			}
		}

		List<ToolCollectStatusResponseDto> toolCollectStatusResponseDtos = new ArrayList<>();

		for (Tool tool : tools) {
			List<DcpConfig> dcpConfigs = toolIdToDcpConfigsMap.getOrDefault(tool, Collections.emptyList());
			List<Parameter> parameters = toolIdToParametersMap.getOrDefault(tool, Collections.emptyList());

			DcpCollectStatus dcpCollectStatus = calculateDcpCollectStatus(dcpConfigs, currentDateTime);

			ParameterCollectStatus parameterCollectStatus = calculateParameterCollectStatus(parameters,
				parameterDataMap,
				parameterIdToDcpConfigMap,
				currentDateTime);

			ToolCollectStatusResponseDto dto = ToolCollectStatusResponseDto.builder()
																		   .toolId(tool.getId())
																		   .toolName(tool.getName())
																		   .dcpIds(dcpConfigs.stream()
																							 .map(DcpConfig::getId)
																							 .toList())
																		   .dcpGoodCnt(dcpCollectStatus.getGoodCount())
																		   .dcpBadCnt(dcpCollectStatus.getBadCount())
																		   .parameterGoodCnt(parameterCollectStatus.getGoodParameters()
																												   .size())
																		   .parameterBadCnt(parameterCollectStatus.getBadParameters()
																												  .size())
																		   .lastCollectedAtList(dcpConfigs.stream()
																										  .map(DcpConfig::getLastCollectedAt)
																										  .toList())
																		   .goodCollectedParameterIds(
																			   parameterCollectStatus.getGoodParameters()
																									 .stream()
																									 .map(Parameter::getId)
																									 .toList())
																		   .badCollectedParameterIds(
																			   parameterCollectStatus.getBadParameters()
																									 .stream()
																									 .map(Parameter::getId)
																									 .toList())
																		   .goodCollectedParameterNames(
																			   parameterCollectStatus.getGoodParameters()
																									 .stream()
																									 .map(Parameter::getName)
																									 .toList())
																		   .badCollectedParameterNames(
																			   parameterCollectStatus.getBadParameters()
																									 .stream()
																									 .map(Parameter::getName)
																									 .toList())
																		   .build();

			toolCollectStatusResponseDtos.add(dto);
		}

		return toolCollectStatusResponseDtos;
	}

	private Map<Long, ParameterData> fetchLatestParameterDataInBatches(
		List<Long> parameterIds,
		LocalDateTime oneHourAgo
	) {
		Map<Long, ParameterData> parameterDataMap = new HashMap<>();

		int batchSize = 1000; // 배치 크기 설정
		for (int i = 0; i < parameterIds.size(); i += batchSize) {
			List<Long> batch = parameterIds.subList(i, Math.min(i + batchSize, parameterIds.size()));
			List<ParameterData> latestParameterData = parameterDataRepository.findLatestParameterData(batch,
				oneHourAgo);
			Map<Long, ParameterData> batchDataMap = latestParameterData.stream()
																	   .collect(Collectors.toMap(ParameterData::getParameterId,
																		   Function.identity()));
			parameterDataMap.putAll(batchDataMap);
		}

		return parameterDataMap;
	}

	private DcpCollectStatus calculateDcpCollectStatus(
		List<DcpConfig> dcpConfigs,
		LocalDateTime currentDateTime
	) {
		int goodCount = 0;
		int badCount = 0;

		for (DcpConfig dcpConfig : dcpConfigs) {
			if (dcpConfig != null && isDcpConfigRecent(dcpConfig, currentDateTime)) {
				goodCount++;
			} else {
				badCount++;
			}
		}

		return new DcpCollectStatus(goodCount, badCount);
	}

	private boolean isDcpConfigRecent(
		DcpConfig dcpConfig,
		LocalDateTime currentDateTime
	) {
		if (dcpConfig == null || dcpConfig.getLastCollectedAt() == null) {
			return false;
		}
		return currentDateTime.minusSeconds(dcpConfig.getDataInterval() * 3L)
							  .isBefore(dcpConfig.getLastCollectedAt());
	}

	private ParameterCollectStatus calculateParameterCollectStatus(
		List<Parameter> parameters,
		Map<Long, ParameterData> parameterDataMap,
		Map<Long, DcpConfig> parameterIdToDcpConfigMap,
		LocalDateTime currentDateTime
	) {

		List<Parameter> goodParameters = new ArrayList<>();
		List<Parameter> badParameters = new ArrayList<>();

		for (Parameter parameter : parameters) {
			ParameterData data = parameterDataMap.get(parameter.getId());
			DcpConfig dcpConfig = parameterIdToDcpConfigMap.get(parameter.getId());

			if (data != null && dcpConfig != null && isDataRecent(data, dcpConfig, currentDateTime)) {
				goodParameters.add(parameter);
			} else {
				badParameters.add(parameter);
			}
		}

		return new ParameterCollectStatus(goodParameters, badParameters);
	}

	private boolean isDataRecent(
		ParameterData data,
		DcpConfig dcpConfig,
		LocalDateTime currentDateTime
	) {
		return currentDateTime.minusSeconds(dcpConfig.getDataInterval() * 3L)
							  .isBefore(data.getTraceAt());
	}

	// 헬퍼 클래스 정의
	public static class DcpCollectStatus {
		private final int goodCount;
		private final int badCount;

		public DcpCollectStatus(
			int goodCount,
			int badCount
		) {
			this.goodCount = goodCount;
			this.badCount = badCount;
		}

		public int getGoodCount() {
			return goodCount;
		}

		public int getBadCount() {
			return badCount;
		}
	}

	public static class ParameterCollectStatus {
		private final List<Parameter> goodParameters;
		private final List<Parameter> badParameters;

		public ParameterCollectStatus(
			List<Parameter> goodParameters,
			List<Parameter> badParameters
		) {
			this.goodParameters = goodParameters;
			this.badParameters = badParameters;
		}

		public List<Parameter> getGoodParameters() {
			return goodParameters;
		}

		public List<Parameter> getBadParameters() {
			return badParameters;
		}
	}
}
