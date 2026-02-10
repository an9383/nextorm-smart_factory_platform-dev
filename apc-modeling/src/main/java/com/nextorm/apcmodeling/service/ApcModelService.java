package com.nextorm.apcmodeling.service;

import com.nextorm.apcmodeling.common.exception.apcmodel.ApcModelNameDuplicateException;
import com.nextorm.apcmodeling.common.exception.apcmodel.ApcModelNotFoundException;
import com.nextorm.apcmodeling.dto.*;
import com.nextorm.common.apc.entity.ApcModel;
import com.nextorm.common.apc.entity.ApcModelVersion;
import com.nextorm.common.apc.repository.ApcModelRepository;
import com.nextorm.common.apc.repository.ApcModelVersionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApcModelService {
	private final ApcModelRepository apcModelRepository;
	private final ApcModelVersionRepository apcModelVersionRepository;

	public List<ApcModelCreateResponseDto> getApcModels() {
		List<ApcModelVersion> ApcModelVersions = apcModelVersionRepository.findAllActiveApcModelVersion();
		return ApcModelVersions.stream()
							   .map(ApcModelCreateResponseDto::from)
							   .toList();
	}

	@Transactional
	public ApcModelCreateResponseDto createApcModel(ApcModelCreateRequestDto apcModelCreateRequestDto) {

		modelNameDuplicationCheck(null, apcModelCreateRequestDto.getModelName());
		ApcModel apcModel = apcModelCreateRequestDto.toEntity();
		ApcModelVersion apcModelVersion = apcModelCreateRequestDto.toVersionEntity(apcModel);

		apcModelRepository.save(apcModel);
		apcModelVersionRepository.save(apcModelVersion);

		return ApcModelCreateResponseDto.from(apcModelVersion);
	}

	public List<ApcModelVersionListResponseDto> getModelVersionsByModelId(Long apcModelId) {
		List<ApcModelVersion> apcModelVersions = apcModelVersionRepository.findAllVersionByApcModelId(apcModelId);
		return apcModelVersions.stream()
							   .map(ApcModelVersionListResponseDto::from)
							   .toList();
	}

	public ApcModelVersionResponseDto getApcModelVersion(
		Long apcModelVersionId
	) {
		ApcModelVersion apcModelVersion = apcModelVersionRepository.findByApcModelVersionId(apcModelVersionId);
		return ApcModelVersionResponseDto.from(apcModelVersion);
	}

	@Transactional
	public ApcModelUpdateResponseDto modifyApcModel(
		Long apcModelId,
		ApcModelUpdateRequestDto apcModelUpdateRequestDto
	) {
		modelNameDuplicationCheck(apcModelId, apcModelUpdateRequestDto.getModelName());
		ApcModel apcModel = apcModelRepository.findById(apcModelId)
											  .orElseThrow(ApcModelNotFoundException::new);
		boolean apcModelIsUse = apcModelUpdateRequestDto.isUse();
		String modelName = apcModelUpdateRequestDto.getModelName();
		apcModel.modifyApcModel(modelName, apcModelIsUse);

		if (apcModelUpdateRequestDto.isActive()) {
			inActiveApcModelVersion(apcModelId);

		}

		ApcModelVersion updateApcModelVersion = apcModelVersionRepository.findByApcModelVersionId(
			apcModelUpdateRequestDto.getVersionId());
		ApcModelVersion versionEntity = apcModelUpdateRequestDto.toVersionEntity(apcModel);
		updateApcModelVersion.modifyApcModelVersion(versionEntity);

		return ApcModelUpdateResponseDto.from(apcModelIsUse,
			modelName,
			updateApcModelVersion.isUseNotify(),
			updateApcModelVersion.getId(),
			apcModelUpdateRequestDto.getDescription());
	}

	@Transactional
	public ResponseEntity<String> deleteApcModel(Long apcModelId) {
		ApcModel apcModel = apcModelRepository.findById(apcModelId)
											  .orElseThrow(ApcModelNotFoundException::new);
		apcModel.deleteApcModel();
		return ResponseEntity.ok("Delete" + apcModel.getId() + "Success !");
	}

	@Transactional
	public ApcModelVersionCreateResponseDto createApcModelVersion(
		Long apcModelId,
		ApcModelVersionCreateRequestDto apcModelRequest
	) {
		Integer version = apcModelVersionRepository.findMaxVersionByApcModelId(apcModelId);

		ApcModel apcModel = apcModelRepository.findById(apcModelId)
											  .orElseThrow(ApcModelNotFoundException::new);
		apcModel.modifyApcModel(apcModelRequest.getModelName(), apcModelRequest.isUse());

		if (apcModelRequest.isActive()) {
			inActiveApcModelVersion(apcModelId);
		}

		ApcModelVersion savedApcModelVersion = apcModelVersionRepository.save(apcModelRequest.toVersionEntity(apcModel,
			version));
		return ApcModelVersionCreateResponseDto.from(savedApcModelVersion, apcModel.isUse());
	}

	private void modelNameDuplicationCheck(
		Long apcModelId,
		String modelName
	) {

		ApcModel apcModelDupCheck = apcModelRepository.findByModelNameAndIsDelete(modelName, false);

		if (apcModelDupCheck != null && !apcModelDupCheck.getId()
														 .equals(apcModelId)) {
			throw new ApcModelNameDuplicateException(modelName);
		}
	}

	private void inActiveApcModelVersion(Long apcModelId) {
		ApcModelVersion inActiveApcModelVersion = apcModelVersionRepository.findActiveVersionByModelId(apcModelId);
		inActiveApcModelVersion.inActiveApcModelVersion();
	}
}
