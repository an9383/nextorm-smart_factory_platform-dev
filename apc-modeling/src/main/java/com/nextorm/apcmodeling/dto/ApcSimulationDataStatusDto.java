package com.nextorm.apcmodeling.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextorm.common.apc.constant.ApcErrorCode;
import com.nextorm.common.apc.entity.ApcModelSimulationData;
import com.nextorm.common.apc.entity.ApcRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@Builder
public class ApcSimulationDataStatusDto {

	public enum Status {
		WAITING, RUNNING, ERROR, SUCCESS, CANCEL, NOT_FOUND
	}

	private Long id;
	private Long apcSimulationId;
	private Map<String, Object> data = new HashMap<>();
	private ApcErrorCode errorCode;
	private Status status;
	private LocalDateTime traceAt;
	private Long apcRequestId;

	public static ApcSimulationDataStatusDto from(
		ApcModelSimulationData apcSimulationData,
		ApcRequest apcRequest
	) {
		ApcSimulationDataStatusDtoBuilder builder = ApcSimulationDataStatusDto.builder()
																			  .id(apcSimulationData.getId())
																			  .apcSimulationId(apcSimulationData.getApcModelSimulation()
																												.getId())
																			  .errorCode(apcSimulationData.getErrorCode())
																			  .data(toMap(apcSimulationData.getDataJson()))
																			  .status(convertStatus(apcSimulationData.getStatus(),
																				  null))
																			  .traceAt(apcSimulationData.getTraceAt());

		if (apcRequest != null) {
			builder.apcRequestId(apcSimulationData.getApcRequestId())
				   .status(convertStatus(apcSimulationData.getStatus(), apcRequest.getStatus()));

			if (apcRequest.getStatus() == ApcRequest.Status.ERROR) {
				builder.errorCode(apcRequest.getErrorCode());
			}
		}

		return builder.build();
	}

	private static Status convertStatus(
		ApcModelSimulationData.Status simulationDataStatus,
		ApcRequest.Status requestStatus
	) {
		if (simulationDataStatus == ApcModelSimulationData.Status.WAITING) {
			return Status.WAITING;
		} else if (simulationDataStatus == ApcModelSimulationData.Status.SENT) {
			return Status.RUNNING;
		} else if (simulationDataStatus == ApcModelSimulationData.Status.CANCEL) {
			return Status.CANCEL;
		} else if (simulationDataStatus == ApcModelSimulationData.Status.FAIL) {
			if (requestStatus == ApcRequest.Status.NOT_FOUND) {
				return Status.NOT_FOUND;
			}
			return Status.ERROR;
		}

		return Status.SUCCESS;
	}

	private static Map<String, Object> toMap(String jsonStr) {
		Map<String, Object> data = null;
		if (jsonStr != null) {
			ObjectMapper objectMapper = new ObjectMapper();
			try {
				data = objectMapper.readValue(jsonStr, Map.class);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
		return data;
	}
}
