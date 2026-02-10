package com.nextorm.portal.controller;

import com.nextorm.portal.dto.InfraBylocationReponseDto;
import com.nextorm.portal.dto.ReservoirLastResponseDto;
import com.nextorm.portal.service.MapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/map-mgt")
@RequiredArgsConstructor
public class MapController {
	private final MapService mapService;

	@GetMapping(path = "/maps")
	public ResponseEntity<List<ReservoirLastResponseDto>> getMaps() {
		List<ReservoirLastResponseDto> reservoirLasts = mapService.getMaps();
		return ResponseEntity.ok(reservoirLasts);
	}

	@GetMapping(path = "/infra-location")
	public List<InfraBylocationReponseDto> getInfraByLocation(
		@RequestParam(name = "lat") double lat,
		@RequestParam(name = "lng") double lng,
		@RequestParam(name = "radius") long radius
	) {
		return mapService.getInfraByLocation(lat, lng, radius);
	}
}