package com.nextorm.apcmodeling.dto;

import com.nextorm.common.apc.entity.ApcModelSimulation;
import com.nextorm.common.apc.entity.ApcModelVersion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class ApcSimulationDto {
	private Long id;
	private String apcModelCondition;
	private Long apcModelVersionId;
	private Integer apcModelVersion;
	private String formulaScript;
	private String formulaWorkspace;
	private String description;
	private ApcModelSimulation.Status status;
	private List<ApcSimulationDataDto> apcSimulationDatas;

	public static ApcSimulationDto from(ApcModelSimulation apcModelSimulation) {

		ApcModelVersion apcModelVersion = apcModelSimulation.getApcModelVersion();

		return ApcSimulationDto.builder()
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
																	 .map(ApcSimulationDataDto::from)
																	 .toList())
							   .build();
	}
}
