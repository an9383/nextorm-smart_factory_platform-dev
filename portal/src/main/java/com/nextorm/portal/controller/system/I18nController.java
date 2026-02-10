package com.nextorm.portal.controller.system;

import com.nextorm.portal.dto.system.I18nModifyRequestDto;
import com.nextorm.portal.restapi.dto.deepl.TranslationResponseDto;
import com.nextorm.portal.service.system.I18nService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 다국어 데이터 관리를 위한 api
 */
@RequestMapping("/api/i18n")
@RestController
@RequiredArgsConstructor
public class I18nController {
	private final I18nService i18nService;

	@GetMapping()
	public Map<String, Map<String, String>> getAll() {
		return i18nService.getAll();
	}

	/**
	 * 다국어 데이터 수정
	 * 추가, 수정, 삭제등의 작업을 모두 처리한다
	 */
	@PostMapping()
	public void modify(@RequestBody I18nModifyRequestDto requestDto) {
		i18nService.modify(requestDto);
	}

	@GetMapping("/translation")
	public TranslationResponseDto translation(
		@RequestParam("targetLang") String targetLang,
		@RequestParam("text") List<String> text
	) {
		return i18nService.getTranslation(targetLang, text);

	}

}
