package com.nextorm.apc.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextorm.apc.exception.InvalidConditionException;
import com.nextorm.apc.exception.InvalidRequestDataException;
import com.nextorm.common.apc.ApcModels;
import com.nextorm.common.apc.constant.ApcConstant;
import com.nextorm.common.apc.entity.*;
import com.nextorm.common.apc.repository.ApcModelRepository;
import com.nextorm.common.apc.repository.ApcModelSimulationDataRepository;
import com.nextorm.common.apc.repository.ApcModelVersionRepository;
import com.nextorm.common.apc.repository.ApcRequestRepository;
import com.nextorm.common.db.repository.system.code.CodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ApcRequestService {
	private static final String CONDITIONS_CATEGORY = ApcConstant.CATEGORY_CODE;
	private static final String DELIMITER = ApcConstant.JOIN_DELIMITER;

	private final ObjectMapper objectMapper;
	private final ApcRequestRepository apcRequestRepository;
	private final ApcModelSimulationDataRepository simulationDataRepository;
	private final ApcModelRepository apcModelRepository;
	private final ApcModelVersionRepository apcModelVersionRepository;
	private final CodeRepository codeRepository;

	public ApcRequest initSimulationRequest(Long simulationDataId) {
		ApcModelSimulationData simulationData = simulationDataRepository.findById(simulationDataId)
																		.orElseThrow(() -> new IllegalArgumentException(
																			"ApcModelSimulationData is not found. id: " + simulationDataId));

		ApcModelSimulation simulation = simulationData.getApcModelSimulation();
		ApcModelVersion modelVersion = simulation.getApcModelVersion();
		ApcModel simulationDataRelateModel = modelVersion.getApcModel();

		Map<String, Object> simulationSource = toMap(simulationData.getDataJson());

		ApcRequest initRequest = null;
		if (checkAllowModel(simulationDataRelateModel.getId(), simulationSource)) {
			initRequest = ApcRequest.createSimulationInit(modelVersion.getId(), simulationData.getDataJson());
		} else {
			simulationData.setStatus(ApcModelSimulationData.Status.FAIL);
			initRequest = ApcRequest.createSimulationModelNotFound(simulationData.getDataJson());
		}
		apcRequestRepository.save(initRequest);
		simulationData.initRequestId(initRequest.getId());

		return initRequest;
	}

	private boolean checkAllowModel(
		Long simulationModelId,
		Map<String, Object> source
	) {
		Optional<ApcModel> foundModel = apcModelRepository.findById(simulationModelId);
		if (foundModel.isEmpty()) {
			return false;
		}
		String condition = sourceToCondition(source);
		List<ApcModel> models = List.of(foundModel.get());
		return new ApcModels(models).findMostSpecificMatch(condition)
									.isPresent();
	}

	public ApcRequest initProcessRequest(Map<String, Object> source) {
		Optional<ApcModel> optionalApcModel = findProcessModel(source);
		if (optionalApcModel.isEmpty()) {
			return apcRequestRepository.save(ApcRequest.createProcessModelNotFound(toJsonString(source)));
		}

		ApcModel apcModel = optionalApcModel.get();
		ApcModelVersion activeModelVersion = apcModelVersionRepository.findActiveVersionByModelId(apcModel.getId());

		return apcRequestRepository.save(ApcRequest.createProcessInit(activeModelVersion.getId(),
			toJsonString(source)));
	}

	public Optional<ApcModel> findProcessModel(Map<String, Object> source) {
		String condition = sourceToCondition(source);
		ApcModels apcModels = new ApcModels(apcModelRepository.findAllActiveModels());
		return apcModels.findMostSpecificMatch(condition);
	}

	private String sourceToCondition(Map<String, Object> source) {
		List<String> conditionSources = codeRepository.findByCategory(CONDITIONS_CATEGORY)
													  .stream()
													  .map(code -> source.getOrDefault(code.getCode(), "")
																		 .toString())
													  .toList();

		boolean allConditionsExist = conditionSources.stream()
													 .noneMatch(String::isBlank);
		if (!allConditionsExist) {
			throw new InvalidConditionException();
		}

		return String.join(DELIMITER, conditionSources);
	}

	private String toJsonString(Map<String, Object> source) {
		try {
			return objectMapper.writeValueAsString(source);
		} catch (Exception e) {
			throw new InvalidRequestDataException(e);
		}
	}

	private Map<String, Object> toMap(String dataJsonString) {
		try {
			return objectMapper.readValue(dataJsonString, new TypeReference<Map<String, Object>>() {
			});
		} catch (Exception e) {
			throw new InvalidRequestDataException(e);
		}
	}
}
