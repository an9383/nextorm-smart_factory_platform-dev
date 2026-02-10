package com.nextorm.portal.controller;

import com.nextorm.portal.dto.parameterdata.RobotPhotoResponseDto;
import com.nextorm.portal.service.PhotoDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/photo-data")
@RequiredArgsConstructor
public class PhotoDataController {
	private final PhotoDataService photoDataService;

	@GetMapping("/robot-shoot-photo")
	public List<RobotPhotoResponseDto> getRobotPhotoData(
		@RequestParam(name = "toolId") Long toolId,
		@RequestParam(name = "fromDate") LocalDateTime fromDate,
		@RequestParam(name = "toDate") LocalDateTime toDate
	) {
		return photoDataService.getRobotPhotoData(toolId, fromDate, toDate);
	}
}
