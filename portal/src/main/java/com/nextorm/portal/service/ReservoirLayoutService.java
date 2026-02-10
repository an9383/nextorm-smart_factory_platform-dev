package com.nextorm.portal.service;

import com.nextorm.common.db.entity.ReservoirLayout;
import com.nextorm.common.db.entity.Tool;
import com.nextorm.common.db.repository.ReservoirLayoutRepository;
import com.nextorm.common.db.repository.ToolRepository;
import com.nextorm.portal.dto.reservoirLayout.ReservoirLayoutCreateRequestDto;
import com.nextorm.portal.dto.reservoirLayout.ReservoirLayoutResponseDto;
import com.nextorm.portal.dto.reservoirLayout.ReservoirLayoutUpdateResponseDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ReservoirLayoutService {
	private final ReservoirLayoutRepository reservoirLayoutRepository;
	private final ToolRepository toolRepository;

	public List<ReservoirLayoutResponseDto> getReservoirLayout(List<Long> toolIds) {

		return ReservoirLayoutResponseDto.from(reservoirLayoutRepository.findByToolIdIn(toolIds));
	}

	public ReservoirLayoutResponseDto createReservoirLayout(ReservoirLayoutCreateRequestDto reservoirLayoutCreateRequestDto) {
		Tool tool = toolRepository.findById(reservoirLayoutCreateRequestDto.getTool()
																		   .getId())
								  .orElseThrow(() -> new EntityNotFoundException("Tool not found ID: " + reservoirLayoutCreateRequestDto.getTool()
																																		.getId()));
		ReservoirLayout reservoirLayout = reservoirLayoutCreateRequestDto.toEntity(tool);
		return ReservoirLayoutResponseDto.from(reservoirLayoutRepository.save(reservoirLayout));
	}

	public ReservoirLayoutUpdateResponseDto modifyReservoirLayout(
		Long reservoirLayoutId,
		String data
	) {
		ReservoirLayout reservoirLayout = reservoirLayoutRepository.findById(reservoirLayoutId)
																   .orElseThrow(() -> new IllegalArgumentException(
																	   "There is no reservoirLayout! (id: " + reservoirLayoutId + ")"));
		reservoirLayout.modify(data);
		return ReservoirLayoutUpdateResponseDto.from(reservoirLayout);
	}

	public void deleteReservoirLayout(Long id) {
		reservoirLayoutRepository.deleteById(id);
	}
}
