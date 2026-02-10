package com.nextorm.extensions.misillan.alarm.service;

import com.nextorm.extensions.misillan.alarm.dto.toolparametermapping.ToolParameterMappingCreateDto;
import com.nextorm.extensions.misillan.alarm.dto.toolparametermapping.ToolParameterMappingModifyDto;
import com.nextorm.extensions.misillan.alarm.dto.toolparametermapping.ToolParameterMappingResponseDto;
import com.nextorm.extensions.misillan.alarm.entity.EqmsTool;
import com.nextorm.extensions.misillan.alarm.entity.SfpParameter;
import com.nextorm.extensions.misillan.alarm.entity.ToolParameterMapping;
import com.nextorm.extensions.misillan.alarm.exception.eqms.EqmsToolNotfoundException;
import com.nextorm.extensions.misillan.alarm.exception.sfp.SfpParameterNotFoundException;
import com.nextorm.extensions.misillan.alarm.exception.toolparametermapping.ToolParameterMappingNotFoundException;
import com.nextorm.extensions.misillan.alarm.repository.EqmsToolRepository;
import com.nextorm.extensions.misillan.alarm.repository.SfpParameterRepository;
import com.nextorm.extensions.misillan.alarm.repository.ToolParameterMappingRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ToolParameterMappingService {

	private final ToolParameterMappingRepository toolParameterMappingRepository;
	private final EqmsToolRepository eqmsToolRepository;
	private final SfpParameterRepository sfpParameterRepository;

	@Transactional(readOnly = true)
	public List<ToolParameterMappingResponseDto> getMappingList() {
		return toolParameterMappingRepository.findAll()
											 .stream()
											 .map(ToolParameterMappingResponseDto::from)
											 .toList();
	}

	@Transactional
	public void createMapping(ToolParameterMappingCreateDto toolParameterMappingCreateDto) {
		EqmsTool eqmsTool = eqmsToolRepository.findById(toolParameterMappingCreateDto.getToolId())
											  .orElseThrow(EqmsToolNotfoundException::new);

		SfpParameter sfpParameter = sfpParameterRepository.findById(toolParameterMappingCreateDto.getParameterId())
														  .orElseThrow(SfpParameterNotFoundException::new);

		ToolParameterMapping entity = ToolParameterMapping.toEntity(eqmsTool,
			sfpParameter, toolParameterMappingCreateDto.getConditionType());

		toolParameterMappingRepository.save(entity);
	}

	@Transactional
	public void modifyToolParameterMapping(Long mappingId,  ToolParameterMappingModifyDto toolParameterMappingModifyDto) {
		EqmsTool eqmsTool = eqmsToolRepository.findById(toolParameterMappingModifyDto.getToolId())
											  .orElseThrow(EqmsToolNotfoundException::new);

		SfpParameter sfpParameter = sfpParameterRepository.findById(toolParameterMappingModifyDto.getParameterId())
														  .orElseThrow(SfpParameterNotFoundException::new);

		ToolParameterMapping mapping = toolParameterMappingRepository.findById(mappingId)
																	 .orElseThrow(ToolParameterMappingNotFoundException::new);
		mapping.modify(eqmsTool, sfpParameter,
			toolParameterMappingModifyDto.getConditionType());

	}

	@Transactional
	public void deleteMapping(Long id) {
		ToolParameterMapping mapping = toolParameterMappingRepository.findById(id)
																	 .orElseThrow(ToolParameterMappingNotFoundException::new);
		toolParameterMappingRepository.delete(mapping);
	}


}
