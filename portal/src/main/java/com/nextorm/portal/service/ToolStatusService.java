package com.nextorm.portal.service;

import com.nextorm.common.db.repository.ToolStatusRepository;
import com.nextorm.portal.dto.tool.ToolStatusRequestDto;
import com.nextorm.portal.dto.tool.ToolStatusResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ToolStatusService {
	private final ToolStatusRepository toolStatusRepository;

	public List<ToolStatusResponseDto> getToolStatus(ToolStatusRequestDto toolStatusRequestDto) {
		Long toolId = toolStatusRequestDto.getToolId();
		return toolStatusRepository.findByToolId(toolId)
								   .stream()
								   .map(ToolStatusResponseDto::from)
								   .toList();
	}
}
