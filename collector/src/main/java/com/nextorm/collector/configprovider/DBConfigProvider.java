package com.nextorm.collector.configprovider;

import com.nextorm.collector.collector.CollectorType;
import com.nextorm.collector.common.exception.toolkafka.ToolKafkaNotFoundException;
import com.nextorm.common.db.entity.*;
import com.nextorm.common.db.repository.*;
import com.nextorm.common.define.collector.CollectorArgument;
import com.nextorm.common.define.collector.DataCollectPlan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class DBConfigProvider implements ConfigProvider {
	private final ConfigProviderArguments configProviderArguments;
	private final CollectorDefineRepository collectorDefineRepository;
	private final DcpConfigRepository dcpConfigRepository;
	private final CollectorConfigRepository collectorConfigRepository;
	private final ToolKafkaRepository toolKafkaRepository;
	private final ParameterExtraDataRepository parameterExtraDataRepository;

	public DBConfigProvider(
		ConfigProviderArguments configProviderArguments,
		CollectorDefineRepository collectorDefineRepository,
		DcpConfigRepository dcpConfigRepository,
		CollectorConfigRepository collectorConfigRepository,
		ToolKafkaRepository toolKafkaRepository,
		ParameterExtraDataRepository parameterExtraDataRepository
	) {
		this.configProviderArguments = configProviderArguments;
		this.collectorDefineRepository = collectorDefineRepository;
		this.dcpConfigRepository = dcpConfigRepository;
		this.collectorConfigRepository = collectorConfigRepository;
		this.toolKafkaRepository = toolKafkaRepository;
		this.parameterExtraDataRepository = parameterExtraDataRepository;

		this.updateCollectorDefinitions();
	}

	@Transactional
	protected void updateCollectorDefinitions() {
		int currentVersion = CollectorType.DEFINITION_VERSION;

		Optional<CollectorDefine> first = collectorDefineRepository.findFirstBy();
		if (first.isEmpty()) {    // 저장된 컬렉터 타입이 없으면 새로 저장
			saveCollectorDefines();
			return;
		}
		CollectorDefine collectorDefined = first.get();

		// DB에 저장된 버전보다 현재 버전이 높으면 모두 삭제하고 새로 저장
		if (currentVersion > collectorDefined.getVersion()) {
			collectorDefineRepository.deleteAll();
			saveCollectorDefines();
		}
	}

	private void saveCollectorDefines() {
		for (CollectorType type : CollectorType.values()) {
			List<Map<String, Object>> arguments = type.getArguments()
													  .stream()
													  .map(this::convertArgument)
													  .toList();

			collectorDefineRepository.save(CollectorDefine.create(type.name(),
				CollectorType.DEFINITION_VERSION,
				type.getDisplayName(),
				arguments));
		}

	}

	private Map<String, Object> convertArgument(CollectorArgument argument) {
		return Map.of("key",
			argument.getKey(),
			"type",
			argument.getType(),
			"required",
			argument.isRequired(),
			"extraDataDefines",
			argument.getExtraDataDefines());
	}

	@Override
	public List<DataCollectPlan> getConfig() {
		return getCollectorConfigByName(this.configProviderArguments.getConfigName());
	}

	@Override
	public List<DataCollectPlan> getConfigByToolId(Long toolId) {
		return getConfig().stream()
						  .filter(config -> config.getToolId()
												  .equals(toolId))
						  .toList();
	}

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
}
