package com.nextorm.apcmodeling.service;

import com.nextorm.apcmodeling.common.exception.apcmodelsimulationdata.ApcSimulationDataNotFoundException;
import com.nextorm.apcmodeling.dto.ApcRequestResultDto;
import com.nextorm.common.apc.entity.ApcModelSimulationData;
import com.nextorm.common.apc.entity.ApcRequestResult;
import com.nextorm.common.apc.repository.ApcModelSimulationDataRepository;
import com.nextorm.common.apc.repository.ApcRequestResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ApcSimulationDataService {

	private final ApcModelSimulationDataRepository apcModelSimulationDataRepository;
	private final ApcRequestResultRepository apcRequestResultRepository;

	public List<ApcRequestResultDto> getApcSimulationDataRequestResult(Long apcModelSimulationDataId) {

		ApcModelSimulationData apcModelSimulationData = apcModelSimulationDataRepository.findById(
																							apcModelSimulationDataId)
																						.orElseThrow(
																							ApcSimulationDataNotFoundException::new);
		Long apcRequestId = apcModelSimulationData.getApcRequestId();

		if (apcRequestId == null) {
			return List.of();
		}

		List<ApcRequestResult> apcRequestResult = apcRequestResultRepository.findByApcRequestId(apcRequestId);

		return apcRequestResult.stream()
							   .map(ApcRequestResultDto::from)
							   .toList();
	}

	public void sentApcSimulationData(
		Long apcModelSimulationDataId
	) {
		ApcModelSimulationData apcModelSimulationData = apcModelSimulationDataRepository.findById(
																							apcModelSimulationDataId)
																						.orElseThrow(
																							ApcSimulationDataNotFoundException::new);
		if (apcModelSimulationData != null) {
			apcModelSimulationData.setStatus(ApcModelSimulationData.Status.SENT);
			apcModelSimulationDataRepository.save(apcModelSimulationData);
		}
	}

}
