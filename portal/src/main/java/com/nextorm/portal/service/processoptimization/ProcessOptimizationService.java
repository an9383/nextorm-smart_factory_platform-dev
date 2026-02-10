package com.nextorm.portal.service.processoptimization;

import com.nextorm.common.db.entity.Tool;
import com.nextorm.common.db.entity.ai.AiModel;
import com.nextorm.common.db.repository.AiModelRepository;
import com.nextorm.common.db.repository.ToolRepository;
import com.nextorm.portal.common.exception.ConstraintViloationException;
import com.nextorm.portal.common.exception.ai.AiModelNotFoundException;
import com.nextorm.portal.common.exception.processoptimization.ProcessOptimizationNotFoundException;
import com.nextorm.portal.dto.base.BaseTreeItem;
import com.nextorm.portal.dto.common.ConstraintViloationDto;
import com.nextorm.portal.dto.processoptimization.ProcessOptimizationCreateDto;
import com.nextorm.portal.dto.processoptimization.ProcessOptimizationResponseDto;
import com.nextorm.portal.entity.processoptimization.ProcessOptimization;
import com.nextorm.portal.event.message.ProcessOptimizationRequestEvent;
import com.nextorm.portal.repository.processoptimization.ProcessOptimizationRepository;
import com.nextorm.portal.util.TreeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ProcessOptimizationService {
	private final ProcessOptimizationRepository processOptimizationRepository;
	private final ProcessOptimizationAnalysisService processOptimizationAnalysisService;
	private final AiModelRepository aiModelRepository;
	private final ToolRepository toolRepository;

	private final ApplicationEventPublisher eventPublisher;

	@Transactional(readOnly = true)
	public ProcessOptimizationResponseDto getOptimization(Long id) {
		Optional<ProcessOptimization> optimization = processOptimizationRepository.findByIdWithAiModel(id);
		if (optimization.isEmpty()) {
			throw new ProcessOptimizationNotFoundException();
		}
		return ProcessOptimizationResponseDto.from(optimization.get());
	}

	@Transactional(readOnly = true)
	public List<ProcessOptimizationResponseDto> getOptimizations() {
		return processOptimizationRepository.findAllWithAiModel(Sort.by(Sort.Direction.DESC, "id"))
											.stream()
											.map(ProcessOptimizationResponseDto::from)
											.toList();
	}

	@Transactional
	public ProcessOptimization saveProcess(ProcessOptimizationCreateDto optimizationCreateDto) {
		AiModel aiModel = aiModelRepository.findById(optimizationCreateDto.getAiModelId())
										   .orElseThrow(AiModelNotFoundException::new);

		ProcessOptimization optimization = ProcessOptimizationCreateDto.toEntity(optimizationCreateDto, aiModel);

		ProcessOptimization optimizationResult = processOptimizationRepository.save(optimization);
		eventPublisher.publishEvent(new ProcessOptimizationRequestEvent(optimizationResult.getId()));
		return optimizationResult;
	}

	public void deleteProcess(List<Long> ids) throws ConstraintViloationException {
		processOptimizationAnalysisService.deleteAnalyses(ids);
		this.deleteOptimizations(ids);
	}

	public void deleteOptimization(Long id) throws ConstraintViloationException {
		Optional<ProcessOptimization> findOptimization = processOptimizationRepository.findById(id);
		findOptimization.ifPresent(processOptimizationRepository::delete);
	}

	public void deleteOptimizations(List<Long> ids) throws ConstraintViloationException {
		List<ConstraintViloationDto> constraintDataList = new ArrayList<>();
		for (Long id : ids) {
			try {
				this.deleteOptimization(id);
			} catch (ConstraintViloationException e) {
				constraintDataList.addAll(e.getData());
			}
		}
		if (!constraintDataList.isEmpty()) {
			throw new ConstraintViloationException(constraintDataList);
		}
	}

	@Transactional(readOnly = true)
	public List<BaseTreeItem> getAiModelTree() {
		List<AiModel> aiModelList = aiModelRepository.findByStatus(AiModel.Status.COMPLETE);
		List<Long> toolNos = aiModelList.stream()
										.map(AiModel::getToolId)
										.filter(Objects::nonNull)
										.distinct()
										.toList();

		List<Tool> toolList = toolRepository.findAll();
		if (!toolList.isEmpty()) {
			toolList = toolList.stream()
							   .filter(k -> toolNos.contains(k.getId()))
							   .toList();
		}

		List<BaseTreeItem> treeItems = new ArrayList<>();
		treeItems.addAll(toolList.stream()
								 .map(item -> new BaseTreeItem.Builder(item.getId(), item.getName(), null).type("TOOL")
																										  .parentType(
																											  null)
																										  .build())
								 .toList());

		treeItems.addAll(aiModelList.stream()
									.map(model -> new BaseTreeItem.Builder(model.getId(),
										model.getName(),
										model.getToolId()).type("MODEL")
														  .parentType("TOOL")
														  .object(model)
														  .build())
									.toList());

		return TreeUtil.generateTreeHierarchy(treeItems);
	}

	public void updateOptimizationFailureStatus(
		Long processOptimizationId,
		String failureReason
	) {
		processOptimizationRepository.findById(processOptimizationId)
									 .ifPresent(model -> model.updateFailure(failureReason));
	}
}
