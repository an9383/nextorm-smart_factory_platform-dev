package com.nextorm.portal.controller;

import com.nextorm.portal.dto.meta.CollectorTypeMetaResponseDto;
import com.nextorm.portal.dto.meta.LanguageSetResponseDto;
import com.nextorm.portal.dto.meta.MetaDataResponseDto;
import com.nextorm.portal.enums.SummaryDataKind;
import com.nextorm.portal.service.MetaDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/meta")
@RequiredArgsConstructor
public class MetaDataController {
	private final MetaDataService metaDataService;

	@GetMapping("/summary-data-kinds")
	public MetaDataResponseDto getSummaryDataKinds() {
		return MetaDataResponseDto.of(Arrays.stream(SummaryDataKind.values())
											.map(it -> new MetaDataResponseDto.Item(it.name(), it.getDisplayName()))
											.toList());

	}

	@GetMapping("/collector-types")
	public List<CollectorTypeMetaResponseDto> getCollectorTypes() {
		return metaDataService.getCollectorTypes();
	}

	@GetMapping("/i18n/languages")
	public List<String> getI18nLanguages() {
		return metaDataService.getI18nLanguages();
	}

	@GetMapping("/i18n/messages")
	public LanguageSetResponseDto getI18nMessages(@RequestParam(name = "lang") Locale locale) {
		return metaDataService.getI18nMessages(locale);
	}

	/**
	 * 시스템 데이터를 Map 형태로 반환한다.
	 */
	@GetMapping("/system-info")
	public Map<String, String> getSystemInfo() {
		return metaDataService.getSystemInfo();
	}

	@PutMapping("/system-info")
	public ResponseEntity<List<Map<String, String>>> modifySystemInfo(@RequestBody List<Map<String, String>> systemInfoMap) throws
		Exception {
		return ResponseEntity.ok(metaDataService.modifySystemInfo(systemInfoMap));
	}
}
