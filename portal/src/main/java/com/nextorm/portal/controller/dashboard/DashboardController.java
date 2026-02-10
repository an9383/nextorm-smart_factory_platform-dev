package com.nextorm.portal.controller.dashboard;

import com.nextorm.portal.dto.dashboard.*;
import com.nextorm.portal.service.dashboard.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboards")
@RequiredArgsConstructor
public class DashboardController {

	private final DashboardService dashboardService;

	@GetMapping(value = "")
	public ResponseEntity<List<DashboardListDto>> getDashboards(@RequestParam(name = "isHide", required = false) Boolean isHide) {
		List<DashboardListDto> dashboards = dashboardService.getDashboards(isHide);
		return ResponseEntity.ok()
							 .body(dashboards);
	}

	@GetMapping(value = "/{id}")
	public DashboardResponseDto getDashboard(@PathVariable(name = "id") long id) {
		return dashboardService.getDashboard(id);
	}

	@PostMapping(value = "")
	public ResponseEntity<DashboardResponseDto> createDashboard(@RequestBody DashboardCreateRequestDto dashboardCreateRequestDto) {
		return ResponseEntity.ok()
							 .body(dashboardService.createDashboard(dashboardCreateRequestDto));
	}

	@PutMapping(value = "/{id}")
	public ResponseEntity<DashboardResponseDto> modifyDashboard(
		@PathVariable(name = "id") Long id,
		@RequestBody DashboardUpdateRequestDto dashboardUpdateRequestDto
	) {
		return ResponseEntity.ok()
							 .body(dashboardService.modifyDashboard(id, dashboardUpdateRequestDto));
	}

	@DeleteMapping(value = "/{id}")
	public ResponseEntity<Object> deleteDashboard(@PathVariable(name = "id") Long id) {
		dashboardService.deleteDashboard(id);
		return ResponseEntity.ok()
							 .body("delete success!");
	}

	@PutMapping
	public void updateDashboardsSortAndIsHide(@RequestBody List<DashboardUpdateRequestDto> dashboards) {
		dashboardService.updateDashboardSortAndIsHide(dashboards);
	}

	@PutMapping("/widgets/{id}")
	public void updateDashboardWidget(
		@PathVariable(name = "id") Long id,
		@RequestBody DashboardWIdgetUpdateRequestDto updateRequestDto
	) {
		dashboardService.updateDashboardWidget(id, updateRequestDto);
	}
}
