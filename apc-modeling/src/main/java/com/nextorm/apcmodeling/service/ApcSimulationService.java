package com.nextorm.apcmodeling.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextorm.apcmodeling.common.exception.CommonException;
import com.nextorm.apcmodeling.constant.ErrorCode;
import com.nextorm.apcmodeling.dto.ApcSimulationDto;
import com.nextorm.apcmodeling.dto.ApcSimulationStatusDto;
import com.nextorm.apcmodeling.dto.ApcTargetDataDto;
import com.nextorm.common.apc.constant.ApcErrorCode;
import com.nextorm.common.apc.entity.ApcModelSimulation;
import com.nextorm.common.apc.entity.ApcModelSimulationData;
import com.nextorm.common.apc.entity.ApcModelVersion;
import com.nextorm.common.apc.entity.ApcRequest;
import com.nextorm.common.apc.repository.ApcModelSimulationRepository;
import com.nextorm.common.apc.repository.ApcModelVersionRepository;
import com.nextorm.common.apc.repository.ApcRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ApcSimulationService {

	private final ApcModelVersionRepository apcModelVersionRepository;
	private final ApcModelSimulationRepository apcModelSimulationRepository;
	private final ApcRequestRepository apcRequestRepository;

	public ApcSimulationStatusDto getApcSimulationById(Long apcModelSimulationId) {
		ApcModelSimulation apcModelSimulation = apcModelSimulationRepository.findByIdWithSimulationData(
																				apcModelSimulationId)
																			.orElseThrow(() -> new CommonException(
																				ErrorCode.UNPROCESSABLE_ENTITY));

		List<Long> apcRequestIds = apcModelSimulation.getApcModelSimulationDataList()
													 .stream()
													 .map(ApcModelSimulationData::getApcRequestId)
													 .filter(Objects::nonNull)
													 .toList();

		List<ApcRequest> apcRequests = apcRequestRepository.findAllById(apcRequestIds);

		return ApcSimulationStatusDto.from(apcModelSimulation, apcRequests);
	}

	public ApcSimulationDto saveApcSimulation(
		Long apcModelVersionId,
		List<ApcTargetDataDto> apcTargetDatas
	) {
		ApcModelVersion apcModelVersion = apcModelVersionRepository.findById(apcModelVersionId)
																   .orElseThrow(() -> new CommonException(ErrorCode.UNPROCESSABLE_ENTITY));

		//APC Simulation 저장
		final ApcModelSimulation apcModelSimulation = ApcModelSimulation.builder()
																		.apcModelVersion(apcModelVersion)
																		.status(ApcModelSimulation.Status.RUNNING)
																		.build();
		//Apc Target Data => Simulation Data 변환
		apcTargetDatas.forEach(it -> {
			String dataJson = null;
			ApcErrorCode errorCode = null;
			ApcModelSimulationData.Status status = ApcModelSimulationData.Status.WAITING;

			if (it.getData()
				  .isEmpty()) {
				errorCode = ApcErrorCode.INVALID_REQUEST_DATA;
			} else {
				try {
					dataJson = mapToJsonString(it.getData());
				} catch (JsonProcessingException | NullPointerException e) {
					//Exception 발생 시 에러 처리 및 로그 저장
					status = ApcModelSimulationData.Status.FAIL;
					errorCode = ApcErrorCode.INVALID_REQUEST_DATA;
				}
			}
			apcModelSimulation.addSimulationData(dataJson, errorCode, status, it.getTraceAt());
		});

		apcModelSimulationRepository.save(apcModelSimulation);

		return ApcSimulationDto.from(apcModelSimulation);
	}

	public ApcSimulationDto cancelApcSimulation(Long apcSimulationId) {

		ApcModelSimulation apcModelSimulation = apcModelSimulationRepository.findByIdWithSimulationData(apcSimulationId)
																			.orElseThrow(() -> new CommonException(
																				ErrorCode.UNPROCESSABLE_ENTITY));

		apcModelSimulation.setStatus(ApcModelSimulation.Status.CANCEL);

		List<ApcModelSimulationData> apcModelSimulationDataList = apcModelSimulation.getApcModelSimulationDataList()
																					.stream()
																					.filter(it -> it.getStatus() == ApcModelSimulationData.Status.WAITING)
																					.toList();

		apcModelSimulationDataList.forEach(it -> it.setStatus(ApcModelSimulationData.Status.CANCEL));

		apcModelSimulationRepository.save(apcModelSimulation);

		return ApcSimulationDto.from(apcModelSimulation);
	}

	public void completeApcSimulation(Long apcModelSimulationId) {
		ApcModelSimulation apcModelSimulation = apcModelSimulationRepository.findById(apcModelSimulationId)
																			.orElse(null);
		if (apcModelSimulation != null) {
			apcModelSimulation.setStatus(ApcModelSimulation.Status.COMPLETE);
			apcModelSimulationRepository.save(apcModelSimulation);
		}
	}

	private String mapToJsonString(
		Map<String, Object> data
	) throws JsonProcessingException, NullPointerException {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.writeValueAsString(data);
	}
}
