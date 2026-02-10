package com.nextorm.extensions.misillan.alarm.service;

import com.nextorm.extensions.misillan.alarm.dto.AlarmConditionRequestDto;
import com.nextorm.extensions.misillan.alarm.dto.AlarmConditionResponseDto;
import com.nextorm.extensions.misillan.alarm.entity.AlarmCondition;
import com.nextorm.extensions.misillan.alarm.entity.ProductAlarmCondition;
import com.nextorm.extensions.misillan.alarm.entity.ToolParameterMapping;
import com.nextorm.extensions.misillan.alarm.event.AlarmConditionChangedEvent;
import com.nextorm.extensions.misillan.alarm.repository.AlarmConditionRepository;
import com.nextorm.extensions.misillan.alarm.repository.ProductAlarmConditionRepository;
import com.nextorm.extensions.misillan.alarm.repository.ToolParameterMappingRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlarmConditionService {
	private final AlarmConditionRepository alarmConditionRepository;
	private final ToolParameterMappingRepository toolParameterMappingRepository;
	private final ProductAlarmConditionRepository productAlarmConditionRepository;
	private final ApplicationEventPublisher eventPublisher;

	public List<AlarmConditionResponseDto> getAlarmConditions() {
		List<AlarmCondition> alarmConditions = alarmConditionRepository.findAllWithToolParameterMappingAndProductAlarmCondition();
		return alarmConditions.stream()
							  .map(AlarmConditionResponseDto::from)
							  .toList();
	}

	public AlarmConditionResponseDto getAlarmConditionById(Long id) {
		AlarmCondition alarmCondition = alarmConditionRepository.findById(id)
																.orElseThrow(() -> new EntityNotFoundException(
																	"AlarmCondition not found: " + id));
		return AlarmConditionResponseDto.from(alarmCondition);
	}

	@Transactional
	public AlarmConditionResponseDto createAlarmCondition(AlarmConditionRequestDto alarmConditionRequest) {
		ToolParameterMapping toolParameterMapping = toolParameterMappingRepository.findById(alarmConditionRequest.getToolParameterMappingId())
																				  .orElseThrow(() -> new EntityNotFoundException(
																					  "ToolParameterMapping not found: " + alarmConditionRequest.getToolParameterMappingId()));
		ProductAlarmCondition productAlarmCondition = null;
		if (alarmConditionRequest.getProductAlarmConditionId() != null) {
			productAlarmCondition = productAlarmConditionRepository.findById(alarmConditionRequest.getProductAlarmConditionId())
																   .orElseThrow(() -> new EntityNotFoundException(
																	   "ProductAlarmCondition not found: " + alarmConditionRequest.getProductAlarmConditionId()));
		}
		AlarmCondition alarmCondition = alarmConditionRequest.toEntity(toolParameterMapping, productAlarmCondition);
		return AlarmConditionResponseDto.from(alarmConditionRepository.save(alarmCondition));
	}

	@Transactional
	public AlarmConditionResponseDto modifyAlarmCondition(
		Long id,
		AlarmConditionRequestDto alarmConditionRequest
	) {
		AlarmCondition alarmCondition = alarmConditionRepository.findById(id)
																.orElseThrow(() -> new EntityNotFoundException(
																	"AlarmCondition not found: " + id));

		ToolParameterMapping toolParameterMapping = toolParameterMappingRepository.findById(alarmConditionRequest.getToolParameterMappingId())
																				  .orElseThrow(() -> new EntityNotFoundException(
																					  "ToolParameterMapping not found: " + alarmConditionRequest.getToolParameterMappingId()));

		ProductAlarmCondition productAlarmCondition = null;
		if (alarmConditionRequest.getProductAlarmConditionId() != null) {
			productAlarmCondition = productAlarmConditionRepository.findById(alarmConditionRequest.getProductAlarmConditionId())
																   .orElseThrow(() -> new EntityNotFoundException(
																	   "ProductAlarmCondition not found: " + alarmConditionRequest.getProductAlarmConditionId()));
		}

		alarmCondition.modify(alarmConditionRequest.getTemperature(),
			alarmConditionRequest.getPressure(),
			alarmConditionRequest.getTimer(),
			toolParameterMapping,
			productAlarmCondition);

		// 이벤트 발행
		Long parameterId = alarmCondition.getToolParameterMapping()
										 .getParameter()
										 .getId();
		eventPublisher.publishEvent(AlarmConditionChangedEvent.update(alarmCondition.getId(), parameterId));

		return AlarmConditionResponseDto.from(alarmCondition);
	}

	@Transactional
	public void deleteAlarmCondition(Long id) {
		// 삭제 전에 parameterId 조회
		AlarmCondition alarmCondition = alarmConditionRepository.findById(id)
																.orElseThrow(() -> new IllegalArgumentException(
																	"존재하지 않는 ID입니다. id=" + id));
		Long parameterId = alarmCondition.getToolParameterMapping()
										 .getParameter()
										 .getId();

		alarmConditionRepository.deleteById(id);

		// 이벤트 발행
		eventPublisher.publishEvent(AlarmConditionChangedEvent.delete(id, parameterId));
	}

	@Transactional
	public void bulkDeleteAlarmCondition(List<Long> ids) {
		if (ids == null || ids.isEmpty()) {
			return;
		}

		// 삭제 전에 parameterId들 조회
		List<AlarmCondition> alarmConditions = alarmConditionRepository.findAllById(ids);

		alarmConditionRepository.deleteAllByIdInBatch(ids);

		// 각각에 대해 이벤트 발행
		alarmConditions.forEach(condition -> {
			Long parameterId = condition.getToolParameterMapping()
										.getParameter()
										.getId();
			eventPublisher.publishEvent(AlarmConditionChangedEvent.delete(condition.getId(), parameterId));
		});
	}

	/**
	 * 활성화 플래그만 수정하는 전용 메서드
	 */
	@Transactional
	public AlarmConditionResponseDto updateAlarmConditionActive(
		Long id,
		boolean isActive
	) {
		AlarmCondition alarmCondition = alarmConditionRepository.findById(id)
																.orElseThrow(() -> new EntityNotFoundException(
																	"AlarmCondition not found: " + id));
		alarmCondition.toggleActive(isActive);

		// 이벤트 발행
		Long parameterId = alarmCondition.getToolParameterMapping()
										 .getParameter()
										 .getId();
		eventPublisher.publishEvent(AlarmConditionChangedEvent.update(alarmCondition.getId(), parameterId));

		return AlarmConditionResponseDto.from(alarmCondition);
	}

}