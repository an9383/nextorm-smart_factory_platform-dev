package com.nextorm.apcmodeling.dto;

import com.nextorm.common.apc.entity.ApcModelSimulation;
import com.nextorm.common.apc.entity.ApcModelVersion;
import com.nextorm.common.apc.entity.ApcRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@Builder
public class ApcSimulationStatusDto {
	private Long id;
	private String apcModelCondition;
	private Long apcModelVersionId;
	private Integer apcModelVersion;
	private String formulaScript;
	private String formulaWorkspace;
	private String description;
	private ApcModelSimulation.Status status;
	private List<ApcSimulationDataStatusDto> apcSimulationDatas;

	public static ApcSimulationStatusDto from(
		ApcModelSimulation apcModelSimulation,
		List<ApcRequest> apcRequests
	) {

		Map<Long, ApcRequest> apcRequestMap = apcRequests.stream()
														 .collect(Collectors.toMap(ApcRequest::getId, it -> it));

		ApcModelVersion apcModelVersion = apcModelSimulation.getApcModelVersion();

		return ApcSimulationStatusDto.builder()
									 .id(apcModelSimulation.getId())
									 .apcModelCondition(apcModelVersion.getApcModel()
																	   .getCondition())
									 .apcModelVersionId(apcModelVersion.getId())
									 .apcModelVersion(apcModelVersion.getVersion())
									 .formulaScript(apcModelVersion.getFormulaScript())
									 .formulaWorkspace(apcModelVersion.getFormulaWorkspace())
									 .description(apcModelVersion.getDescription())
									 .status(apcModelSimulation.getStatus())
									 .apcSimulationDatas(apcModelSimulation.getApcModelSimulationDataList()
																		   .stream()
																		   .map(it -> ApcSimulationDataStatusDto.from(it,
																			   apcRequestMap.get(it.getApcRequestId())))
																		   .toList())
									 .build();
	}
}
