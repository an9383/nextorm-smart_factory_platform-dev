package com.nextorm.portal.controller;

import com.nextorm.common.db.entity.Location;
import com.nextorm.portal.dto.base.BaseTreeItem;
import com.nextorm.portal.dto.location.*;
import com.nextorm.portal.dto.tool.ToolResponseDto;
import com.nextorm.portal.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {
	private final LocationService locationService;

	@GetMapping(path = "/tree")
	public List<BaseTreeItem> getLocationsTree(@RequestParam(name = "typeUntils", required = false) Location.Type... typeUntils) {
		return locationService.getLocationsTree(typeUntils);
	}

	/**
	 * @return 로케이션과 툴의 트리 데이터를 반환
	 */
	@GetMapping("/tools/tree")
	public List<BaseTreeItem> getLocationsAndToolsTree() {
		return locationService.getLocationsAndToolsTree();
	}

	/**
	 * @return 해당 로케이션 하위의 모든 로케이션을 탐색하여 하위의 '모든' 툴 리스트 반환
	 */
	@GetMapping(path = "/{locationId}/tools")
	public ResponseEntity<List<ToolResponseDto>> getLocationUnderTools(@PathVariable(name = "locationId") Long locationId) {
		return ResponseEntity.ok()
							 .body(locationService.getLocationUnderTools(locationId));
	}

	@GetMapping(path = "/{id}/children")
	public List<LocationDto> getLocationChildren(@PathVariable(name = "id") Long id) {
		return locationService.getLocationChildren(id);
	}

	@GetMapping(path = "/{id}")
	public LocationDto getLocation(@PathVariable(name = "id") Long id) {
		return locationService.getLocation(id);
	}

	@PostMapping
	public LocationCreateResponseDto createLocation(@RequestBody LocationCreateRequestDto locationDto) {
		return locationService.create(locationDto);
	}

	@PutMapping(path = "/{id}")
	public LocationDto modify(
		@PathVariable(name = "id") Long id,
		@RequestBody LocationModifyRequestDto modifyRequestDto
	) {
		return locationService.modify(id, modifyRequestDto);
	}

	@DeleteMapping(path = "/{id}")
	public ResponseEntity<Void> deleteLocation(@PathVariable(name = "id") Long id) {
		locationService.delete(id);
		return ResponseEntity.ok()
							 .build();
	}

	@PostMapping("/bulk-delete")
	public ResponseEntity<Void> bulkDeleteLocation(@RequestBody LocationBulkDeleteRequestDto requestDto) {
		locationService.delete(requestDto.getIds());
		return ResponseEntity.ok()
							 .build();
	}

	@GetMapping("/line/{toolId}")
	public ResponseEntity<LocationDto> getLineTypeLocationByToolId(@PathVariable(name = "toolId") Long toolId) {
		return ResponseEntity.ok()
							 .body(locationService.getLineTypeLocationByToolId(toolId));
	}
}
