package com.nextorm.apc.service;

import com.nextorm.apc.exception.BusinessException;
import com.nextorm.apc.scriptengine.EngineExecuteResult;
import com.nextorm.common.apc.entity.ApcModelSimulationData;
import com.nextorm.common.apc.entity.ApcRequest;
import com.nextorm.common.apc.entity.ApcRequestResult;
import com.nextorm.common.apc.repository.ApcModelSimulationDataRepository;
import com.nextorm.common.apc.repository.ApcRequestRepository;
import com.nextorm.common.apc.repository.ApcRequestResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class ResultHandler {
	private final ApcRequestRepository apcRequestRepository;
	private final ApcRequestResultRepository apcRequestResultRepository;
	private final ApcModelSimulationDataRepository apcModelSimulationDataRepository;

	public void handleSuccess(
		Long apcRequestId,
		EngineExecuteResult executeResult
	) {
		ApcRequest apcRequest = apcRequestRepository.findById(apcRequestId)
													.orElseThrow(() -> new IllegalArgumentException(
														"ApcRequest is not found. id: " + apcRequestId));
		List<ApcRequestResult> results = toRequestResults(apcRequestId, executeResult);

		apcRequestResultRepository.saveAll(results);
		apcRequest.updateSuccess();
	}

	private List<ApcRequestResult> toRequestResults(
		Long apcRequestId,
		EngineExecuteResult executeResult
	) {
		return executeResult.getValue()
							.entrySet()
							.stream()
							.map(entry -> createRequestResult(apcRequestId, entry))
							.toList();
	}

	private ApcRequestResult createRequestResult(
		Long apcRequestId,
		Map.Entry<String, Object> resultEntry
	) {
		String value = resultEntry.getValue() == null
					   ? null
					   : resultEntry.getValue()
									.toString();

		return ApcRequestResult.create(apcRequestId, resultEntry.getKey(), value);
	}

	public void handleSimulationSuccess(
		Long simulationDataId,
		Long apcRequestId,
		EngineExecuteResult executeResult
	) {
		handleSuccess(apcRequestId, executeResult);
		apcModelSimulationDataRepository.findById(simulationDataId)
										.ifPresent(simulationData -> simulationData.setStatus(ApcModelSimulationData.Status.COMPLETE));
	}

	public Long handleRequestInitBeforeError(
		ApcRequest.Type requestType,
		Long simulationDataId,
		BusinessException businessException

	) {
		Long requestId = handleRequestInitBeforeError(requestType, businessException);
		apcModelSimulationDataRepository.findById(simulationDataId)
										.ifPresent(simulationData -> {
											simulationData.setStatus(ApcModelSimulationData.Status.FAIL);
											simulationData.initRequestId(requestId);
											simulationData.setErrorCode(businessException.getErrorCode());
										});
		return requestId;
	}

	/**
	 * @param requestType:       API REQUEST TYPE
	 * @param businessException: Business Exception
	 * @return: 새로 생성된 ApcRequest ID
	 */
	public Long handleRequestInitBeforeError(
		ApcRequest.Type requestType,
		BusinessException businessException
	) {
		log.error("handleRequestInitBeforeError", businessException.getCause());
		return apcRequestRepository.save(ApcRequest.createErrorRequest(requestType, businessException.getErrorCode()))
								   .getId();
	}

	public void handleError(
		Long apcRequestId,
		BusinessException businessException
	) {
		apcModelSimulationDataRepository.findByApcRequestId(apcRequestId)
										.ifPresent(simulationData -> {
											simulationData.setStatus(ApcModelSimulationData.Status.FAIL);
											simulationData.setErrorCode(businessException.getErrorCode());
										});

		apcRequestRepository.findById(apcRequestId)
							.ifPresentOrElse(apcRequest -> apcRequest.updateError(businessException.getErrorCode()),
								() -> {
									throw new IllegalArgumentException("ApcRequest is not found. id: " + apcRequestId,
										businessException);
								});
	}
}
