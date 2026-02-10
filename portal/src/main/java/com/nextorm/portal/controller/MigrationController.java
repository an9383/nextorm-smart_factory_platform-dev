package com.nextorm.portal.controller;

import com.nextorm.portal.service.migration.MigrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/api/migration")
@RequiredArgsConstructor
public class MigrationController {
	private final MigrationService migrationService;

	@PostMapping(value = "")
	public void migrate(
		@RequestPart(name = "file") MultipartFile file,
		@RequestPart(name = "headerParameterIdMap") Map<String, Long> headerParameterIdMap,
		@RequestPart(name = "isIncludeGeoData") boolean isIncludeGeoData
	) {
		migrationService.migrate(headerParameterIdMap, file, isIncludeGeoData);
	}
}
