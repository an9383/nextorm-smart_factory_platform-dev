package com.nextorm.portal.controller;

import com.nextorm.portal.dto.reservoirLayout.ReservoirLayoutCreateRequestDto;
import com.nextorm.portal.dto.reservoirLayout.ReservoirLayoutResponseDto;
import com.nextorm.portal.dto.reservoirLayout.ReservoirLayoutUpdateRequestDto;
import com.nextorm.portal.dto.reservoirLayout.ReservoirLayoutUpdateResponseDto;
import com.nextorm.portal.service.ReservoirLayoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservoir-layouts")
@RequiredArgsConstructor
public class ReservoirLatLayoutController {
	private final ReservoirLayoutService reservoirLayoutService;

	@GetMapping(path = "")
	public ResponseEntity<List<ReservoirLayoutResponseDto>> getReservoirLayout(@RequestParam(name = "toolIds") List<Long> toolIds) {
		List<ReservoirLayoutResponseDto> reservoirLayoutResponseDto = reservoirLayoutService.getReservoirLayout(toolIds);
		return ResponseEntity.ok(reservoirLayoutResponseDto);
	}

	@PostMapping(path = "")
	public ResponseEntity<ReservoirLayoutResponseDto> createReservoirLayout(@RequestBody ReservoirLayoutCreateRequestDto reservoirLayoutCreateRequestDto) {
		ReservoirLayoutResponseDto ReservoirLayoutResponseDto = reservoirLayoutService.createReservoirLayout(
			reservoirLayoutCreateRequestDto);
		return ResponseEntity.ok(ReservoirLayoutResponseDto);
	}

	@PutMapping(path = "/{reservoirLayoutId}")
	public ReservoirLayoutUpdateResponseDto modifyReservoirLayout(
		@PathVariable(name = "reservoirLayoutId") Long reservoirLayoutId,
		@RequestBody ReservoirLayoutUpdateRequestDto reservoirLayout
	) {
		return reservoirLayoutService.modifyReservoirLayout(reservoirLayoutId, reservoirLayout.getData());
	}

	@DeleteMapping(path = "/{reservoirLayoutId}")
	public void deleteReservoirLayout(
		@PathVariable(name = "reservoirLayoutId") Long reservoirLayoutId
	) {
		reservoirLayoutService.deleteReservoirLayout(reservoirLayoutId);
	}
}