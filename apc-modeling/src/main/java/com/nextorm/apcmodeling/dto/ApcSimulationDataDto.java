package com.nextorm.apcmodeling.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextorm.common.apc.constant.ApcErrorCode;
import com.nextorm.common.apc.entity.ApcModelSimulationData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@Builder
public class ApcSimulationDataDto {

	private Long id;
	private Long apcSimulationId;
	private Long apcRequestId;
	private Map<String, Object> data = new HashMap<>();
	private ApcErrorCode errorCode;
	private ApcModelSimulationData.Status status;
	private LocalDateTime traceAt;

	public static ApcSimulationDataDto from(ApcModelSimulationData apcSimulationData) {
		Map<String, Object> data = null;
		if (apcSimulationData.getDataJson() != null) {
			ObjectMapper objectMapper = new ObjectMapper();
			try {
				data = objectMapper.readValue(apcSimulationData.getDataJson(), Map.class);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}

		return ApcSimulationDataDto.builder()
								   .id(apcSimulationData.getId())
								   .apcSimulationId(apcSimulationData.getApcModelSimulation()
																	 .getId())
								   .apcRequestId(apcSimulationData.getApcRequestId())
								   .data(data)
								   .errorCode(apcSimulationData.getErrorCode())
								   .status(apcSimulationData.getStatus())
								   .traceAt(apcSimulationData.getTraceAt())
								   .build();
	}
}
