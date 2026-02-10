package com.nextorm.apcmodeling.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextorm.apcmodeling.dto.ApcSimulationDataDto;
import com.nextorm.apcmodeling.dto.ApcSimulationListResponseDto;
import com.nextorm.apcmodeling.dto.ApcSimulationListSearchRequestDto;
import com.nextorm.common.apc.ApcModels;
import com.nextorm.common.apc.entity.ApcModelSimulation;
import com.nextorm.common.apc.entity.ApcModelSimulationData;
import com.nextorm.common.apc.entity.ApcModelVersion;
import com.nextorm.common.apc.repository.ApcModelSimulationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ApcSimulationListService {
	private final ApcModelSimulationRepository apcModelSimulationRepository;

	public List<ApcSimulationListResponseDto> getApcSimulationList(ApcSimulationListSearchRequestDto searchRequestDto) {
		LocalDateTime from = searchRequestDto.getFrom();
		LocalDateTime to = searchRequestDto.getTo();

		List<ApcSimulationListResponseDto> apcSimulationListResponseDtos = new ArrayList<>();

		String requestDtoCondition = searchRequestDto.getCondition();

		List<ApcModelSimulation> apcModelSimulations = apcModelSimulationRepository.findWithSimulationDataByCreateAtBetweenOrderByCreateAtDesc(
			from,
			to);

		List<ApcModelSimulation> filteredSimulations = apcModelSimulations.stream()
																		  .filter(v -> apcModelConditionFiltering(v.getApcModelVersion()
																												   .getApcModel()
																												   .getCondition(),
																			  requestDtoCondition))
																		  .toList();
		for (ApcModelSimulation apcModelSimulation : filteredSimulations) {
			ApcSimulationListResponseDto apcSimulationListResponseDto = buildResponseDto(apcModelSimulation);
			apcSimulationListResponseDtos.add(apcSimulationListResponseDto);
		}
		return apcSimulationListResponseDtos;
	}

	private boolean apcModelConditionFiltering(
		String modelCondition,
		String requestCondition
	) {

		return ApcModels.calculateSpecificityScore(modelCondition, requestCondition) >= 0;
	}

	private ApcSimulationListResponseDto buildResponseDto(
		ApcModelSimulation apcModelSimulation
	) {
		if (Objects.isNull(apcModelSimulation)) {
			return null;
		}

		ObjectMapper objectMapper = new ObjectMapper();

		String conditions = apcModelSimulation.getApcModelVersion()
											  .getApcModel()
											  .getCondition();
		ApcModelVersion apcModelVersion = apcModelSimulation.getApcModelVersion();

		List<ApcModelSimulationData> simulationDataList = apcModelSimulation.getApcModelSimulationDataList();

		List<ApcSimulationDataDto> simulationDataDtos = new ArrayList<>();

		for (ApcModelSimulationData simulationData : simulationDataList) {
			Map<String, Object> dataMap = new HashMap<>();
			try {
				dataMap = objectMapper.readValue(simulationData.getDataJson(), Map.class);
			} catch (Exception e) {
				e.printStackTrace();
			}

			ApcSimulationDataDto simulationDataDto = ApcSimulationDataDto.builder()
																		 .id(simulationData.getId())
																		 .apcSimulationId(simulationData.getApcModelSimulation()
																										.getId())
																		 .apcRequestId(simulationData.getApcRequestId())
																		 .data(dataMap)
																		 .errorCode(simulationData.getErrorCode())
																		 .status(simulationData.getStatus())
																		 .traceAt(simulationData.getTraceAt())
																		 .build();
			simulationDataDtos.add(simulationDataDto);
		}

		return ApcSimulationListResponseDto.builder()
										   .apcSimulationId(apcModelSimulation.getId())
										   .condition(conditions)
										   .formula(apcModelVersion.getFormulaWorkspace())
										   .version(apcModelVersion.getVersion())
										   .status(apcModelSimulation.getStatus())
										   .apcSimulationDatas(simulationDataDtos)
										   .createBy(apcModelSimulation.getCreateBy())
										   .createAt(apcModelSimulation.getCreateAt())
										   .build();
	}
}