package com.nextorm.common.define.collector;

import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DataCollectPlan {
	private Long dcpId;
	private Long toolId;
	private String toolName;
	private String topic;
	private String bootstrapServer;
	private String command;
	private Integer dataInterval;
	@Getter(value = AccessLevel.PRIVATE)
	private List<Parameter> parameters = new ArrayList<>();
	private String collectorType;
	private Map<String, Object> collectorArguments;
	private Boolean isGeoDataType;
	private String latitudeParameterName;
	private String longitudeParameterName;

	/**
	 * 아래 두 필드는 빌더에 포함하지 않음
	 */
	private List<Parameter> collectParameters;
	private List<Parameter> metaParameters;

	@Builder
	public DataCollectPlan(
		Long dcpId,
		Long toolId,
		String toolName,
		String topic,
		String bootstrapServer,
		String command,
		Integer dataInterval,
		List<Parameter> parameters,
		String collectorType,
		Map<String, Object> collectorArguments,
		Boolean isGeoDataType,
		String latitudeParameterName,
		String longitudeParameterName
	) {
		this.dcpId = dcpId;
		this.toolId = toolId;
		this.toolName = toolName;
		this.topic = topic;
		this.bootstrapServer = bootstrapServer;
		this.command = command;
		this.dataInterval = dataInterval;
		this.parameters = parameters;
		this.collectorType = collectorType;
		this.collectorArguments = collectorArguments;
		this.isGeoDataType = isGeoDataType;
		this.latitudeParameterName = latitudeParameterName;
		this.longitudeParameterName = longitudeParameterName;
	}

	public List<Parameter> getCollectParameters() {
		if (collectParameters != null) {
			return collectParameters;
		}
		collectParameters = parameters.stream()
									  .filter(it -> !it.isMetaParameter)
									  .toList();
		return collectParameters;
	}

	public List<Parameter> getMetaParameters() {
		if (metaParameters != null) {
			return metaParameters;
		}
		metaParameters = parameters.stream()
								   .filter(Parameter::isMetaParameter)
								   .toList();
		return metaParameters;
	}

	@Getter
	@NoArgsConstructor(access = AccessLevel.PROTECTED)
	@AllArgsConstructor
	public static class Parameter {
		private Long id;
		private String name;
		private boolean isMetaParameter;
		private Object metaValue;
		private Integer order;
		private Map<String, Object> extraData = Map.of();
	}
}
