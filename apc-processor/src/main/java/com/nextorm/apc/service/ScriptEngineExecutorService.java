package com.nextorm.apc.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextorm.apc.exception.InvalidRequestDataException;
import com.nextorm.apc.scriptengine.DefaultScriptEngine;
import com.nextorm.apc.scriptengine.EngineExecuteResult;
import com.nextorm.apc.scriptengine.ExecutorFactory;
import com.nextorm.apc.scriptengine.ScriptEngine;
import com.nextorm.common.apc.entity.ApcModelSimulationData;
import com.nextorm.common.apc.entity.ApcModelVersion;
import com.nextorm.common.apc.entity.ApcRequest;
import com.nextorm.common.apc.repository.ApcModelSimulationDataRepository;
import com.nextorm.common.apc.repository.ApcModelVersionRepository;
import com.nextorm.common.apc.repository.ApcRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ScriptEngineExecutorService {
	private final ObjectMapper objectMapper;
	private final ApcModelVersionRepository apcModelVersionRepository;
	private final ApcRequestRepository apcRequestRepository;
	private final ApcModelSimulationDataRepository simulationDataRepository;
	private final ExecutorFactory executorFactory;

	public EngineExecuteResult execute(
		Long apcRequestId
	) {
		ApcRequest apcRequest = apcRequestRepository.findById(apcRequestId)
													.orElseThrow(() -> new IllegalArgumentException(
														"ApcRequest is not found. id: " + apcRequestId));

		ApcModelVersion modelVersion = apcModelVersionRepository.findById(apcRequest.getApcModelVersionId())
																.orElseThrow(() -> new IllegalArgumentException(
																	"ApcModelVersion is not found. id: " + apcRequest.getApcModelVersionId()));

		Map<String, Object> source = Map.of();
		try {
			source = objectMapper.readValue(apcRequest.getRequestDataJson(), new TypeReference<Map<String, Object>>() {
			});
		} catch (Exception e) {
			throw new InvalidRequestDataException(e);
		}

		LocalDateTime referenceTime = apcRequest.isSimulation()
									  ? getSimulateTime(apcRequestId)
									  : LocalDateTime.now();

		Map<String, Object> metaData = Map.of("isSimulation", apcRequest.isSimulation(), "time", referenceTime);

		ScriptEngine engine = new DefaultScriptEngine(modelVersion.getFormulaScript(),
			executorFactory.createExecutor(Map.of()));
		return engine.execute(new ScriptEngine.ExecuteArguments(source, metaData));
	}

	private LocalDateTime getSimulateTime(Long apcRequestId) {
		return simulationDataRepository.findByApcRequestId(apcRequestId)
									   .map(ApcModelSimulationData::getTraceAt)
									   .orElseThrow(() -> new IllegalArgumentException(
										   "ApcModelSimulationData is not found. apcRequestId: " + apcRequestId));
	}
}
