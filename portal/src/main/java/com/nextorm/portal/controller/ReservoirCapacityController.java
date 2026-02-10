package com.nextorm.portal.controller;

import com.nextorm.portal.dto.reservoircapacity.*;
import com.nextorm.portal.service.ReservoirCapacityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reservoir-capacity")
@RequiredArgsConstructor
public class ReservoirCapacityController {

	public enum Type {
		YEAR, MONTH, DAY, HOUR
	}

	private final ReservoirCapacityService reservoirCapacityService;

	@GetMapping
	public ResponseEntity<List<ReservoirCapacityResponseDto>> getReservoirCapacities(
		ReservoirCapacitySearchDto reservoirCapacitySearch
	) {
		return ResponseEntity.ok()
							 .body(reservoirCapacityService.getReservoirCapacities(reservoirCapacitySearch));

	}

	@GetMapping("/{reservoirCapacityId}")
	public ResponseEntity<ReservoirCapacityResponseDto> getReservoirCapacity(@PathVariable(name = "reservoirCapacityId") Long reservoirCapacityId) {
		return ResponseEntity.ok()
							 .body(reservoirCapacityService.getReservoirCapacity(reservoirCapacityId));
	}

	@PostMapping
	public ReservoirCapacityResponseDto createReservoirCapacity(@RequestBody ReservoirCapacityCreateRequestDto reservoirCapacityCreateRequestDto) {
		return reservoirCapacityService.createReservoirCapacity(reservoirCapacityCreateRequestDto);
	}

	@PutMapping("/{reservoirCapacityId}")
	public ReservoirCapacityResponseDto modifyReservoirCapacity(
		@PathVariable(name = "reservoirCapacityId") Long reservoirCapacityId,
		@RequestBody ReservoirCapacityUpdateRequestDto reservoirCapacityUpdateRequestDto
	) {
		return reservoirCapacityService.modifyReservoirCapacity(reservoirCapacityId, reservoirCapacityUpdateRequestDto);
	}

	@DeleteMapping("/{reservoirCapacityId}")
	public ResponseEntity<String> deleteReservoirCapacity(@PathVariable(name = "reservoirCapacityId") Long reservoirCapacityId) {
		reservoirCapacityService.deleteReservoirCapacity(reservoirCapacityId);
		return ResponseEntity.ok("delete success!!");
	}

	@PostMapping("/bulk-delete")
	public ResponseEntity<String> bulkDeleteReservoirCapacities(@RequestBody ReservoirCapacityBulkDeleteRequestDto requestDto) {
		reservoirCapacityService.deleteReservoirCapacities(requestDto.getIds());
		return ResponseEntity.ok("delete success!!");
	}

	@GetMapping("/trend")
	public ResponseEntity<List<ReservoirCapacityResponseTrendDto>> getReservoirCapacityTrend(
		@RequestParam(name = "type") Type type,
		@RequestParam(name = "startDt") LocalDateTime startDt,
		@RequestParam(name = "endDt") LocalDateTime endDt,
		@RequestParam(name = "locationId") Long locationId
	) {
		return ResponseEntity.ok()
							 .body(reservoirCapacityService.getReservoirCapacityTrend(type,
								 startDt,
								 endDt,
								 locationId));
	}

}