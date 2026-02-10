package com.nextorm.portal.service;

import com.nextorm.common.db.entity.*;
import com.nextorm.common.db.repository.*;
import com.nextorm.common.define.collector.CollectorTypeDefineRequestDto;
import com.nextorm.common.define.collector.DataCollectPlan;
import com.nextorm.processor.common.exception.toolkafka.ToolKafkaNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ConfigService {
	private final CollectorConfigRepository collectorConfigRepository;
	private final DcpConfigRepository dcpConfigRepository;
	private final CollectorDefineRepository collectorDefineRepository;
	private final ToolKafkaRepository toolKafkaRepository;
	private final ParameterExtraDataRepository parameterExtraDataRepository;

	public List<DataCollectPlan> getCollectorConfigByName(String configName) {
		CollectorConfig collectorConfig = collectorConfigRepository.findByName(configName);
		if (collectorConfig == null) {
			log.info("CollectorConfig not found. name={}", configName);
			return List.of();
		}

		List<Long> toolIds = collectorConfig.getTools()
											.stream()
											.map(Tool::getId)
											.toList();

		if (toolIds.isEmpty()) {
			return List.of();
		}

		return dcpConfigRepository.findByToolIdIn(toolIds)
								  .stream()
								  .map(dcpConfig -> {
									  ToolKafka toolKafka = toolKafkaRepository.findByToolId(dcpConfig.getTool()
																									  .getId())
																			   .orElseThrow(ToolKafkaNotFoundException::new);
									  return this.convert(dcpConfig, toolKafka, dcpConfig.getParameters());
								  })
								  .toList();
	}

	private DataCollectPlan convert(
		DcpConfig dcpConfig,
		ToolKafka toolKafka,
		List<Parameter> parameters
	) {
		List<DataCollectPlan.Parameter> convertedParameters = toDataCollectPlanParameter(parameters);

		return DataCollectPlan.builder()
							  .dcpId(dcpConfig.getId())
							  .toolId(dcpConfig.getTool()
											   .getId())
							  .toolName(dcpConfig.getTool()
												 .getName())
							  .topic(toolKafka.getTopic())
							  .bootstrapServer(toolKafka.getBootstrapServer())
							  .command(dcpConfig.getCommand())
							  .dataInterval(dcpConfig.getDataInterval())
							  .parameters(convertedParameters)
							  .collectorType(dcpConfig.getCollectorType())
							  .collectorArguments(dcpConfig.getCollectorArguments())
							  .isGeoDataType(dcpConfig.isGeoDataType())
							  .latitudeParameterName(dcpConfig.getLatitudeParameterName() != null
													 ? dcpConfig.getLatitudeParameterName()
													 : null)
							  .longitudeParameterName(dcpConfig.getLongitudeParameterName() != null
													  ? dcpConfig.getLongitudeParameterName()
													  : null)
							  .build();
	}

	private List<DataCollectPlan.Parameter> toDataCollectPlanParameter(List<Parameter> parameters) {
		List<Long> parameterIds = parameters.stream()
											.map(Parameter::getId)
											.toList();

		Map<Long, Map<String, Object>> parameterExtraDataMap = parameterExtraDataRepository.findByParameterIdIn(
																							   parameterIds)
																						   .stream()
																						   .collect(Collectors.toMap(
																							   ParameterExtraData::getParameterId,
																							   ParameterExtraData::getExtraData));

		return parameters.stream()
						 .filter(v -> !v.isVirtual())
						 .sorted(Comparator.comparing(Parameter::getOrder))
						 .map(it -> {
							 Map<String, Object> extraData = parameterExtraDataMap.getOrDefault(it.getId(), Map.of());
							 return new DataCollectPlan.Parameter(it.getId(),
								 it.getName(),
								 it.isMetaParameter(),
								 it.getMetaValue(),
								 it.getOrder(),
								 extraData);
						 })
						 .toList();
	}

	public void updateCollectorTypeDefinition(CollectorTypeDefineRequestDto requestDto) {
		Optional<CollectorDefine> first = collectorDefineRepository.findFirstBy();
		if (first.isEmpty()) {    // 저장된 컬렉터 타입이 없으면 새로 저장
			saveCollectorDefines(requestDto);
			return;
		}

		CollectorDefine collectorDefined = first.get();
		int requestVersion = requestDto.getVersion();

		// 요청 버전이 더 높으면 모든 컬렉터 타입을 삭제하고 새로 저장
		if (requestVersion > collectorDefined.getVersion()) {
			collectorDefineRepository.deleteAll();
			saveCollectorDefines(requestDto);
		}
	}

	private void saveCollectorDefines(CollectorTypeDefineRequestDto requestDto) {
		int version = requestDto.getVersion();
		requestDto.getCollectorTypes()
				  .forEach(collectorType -> {
					  String type = collectorType.getType();
					  String displayName = collectorType.getDisplayName();
					  List<Map<String, Object>> arguments = collectorType.getArguments()
																		 .stream()
																		 .map(this::convertArgument)
																		 .toList();
					  collectorDefineRepository.save(CollectorDefine.create(type, version, displayName, arguments));
				  });
	}

	private Map<String, Object> convertArgument(CollectorTypeDefineRequestDto.Argument argument) {
		return Map.of("key", argument.getKey(), "type", argument.getType());
	}
}
