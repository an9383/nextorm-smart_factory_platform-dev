package com.nextorm.portal.dto.dcpconfig;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nextorm.common.db.entity.DcpConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DcpConfigCreateRequestDto {
	private Long toolId;
	private String topic;

	private String bootstrapServer;
	private String command;

	private Integer dataInterval;

	private List<Long> parameterIds;
	private List<Long> ruleIds;

	private String collectorType;
	private Map<String, Object> collectorArguments;
	private Map<Long, Map<String, Object>> parameterCollectExtraData = Map.of();

	@JsonProperty("isGeoDataType")
	private boolean isGeoDataType;
	private String latitudeParameterName;
	private String longitudeParameterName;

	public DcpConfig toEntity() {
		return DcpConfig.builder()
						.topic(topic)
						.bootstrapServer(bootstrapServer)
						.command(command)
						.dataInterval(dataInterval)
						.collectorType(collectorType)
						.collectorArguments(collectorArguments)
						.isGeoDataType(isGeoDataType)
						.latitudeParameterName(latitudeParameterName)
						.longitudeParameterName(longitudeParameterName)
						.build();
	}
}
