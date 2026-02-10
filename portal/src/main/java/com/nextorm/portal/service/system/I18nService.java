package com.nextorm.portal.service.system;

import com.nextorm.common.db.entity.UiLanguage;
import com.nextorm.common.db.entity.UiLanguageId;
import com.nextorm.common.db.entity.system.code.Code;
import com.nextorm.common.db.repository.UiLanguageRepository;
import com.nextorm.common.db.repository.system.code.CodeRepository;
import com.nextorm.portal.constant.CodeCategory;
import com.nextorm.portal.dto.system.I18nModifyRequestDto;
import com.nextorm.portal.restapi.DeepLRestApi;
import com.nextorm.portal.restapi.dto.deepl.TranslationResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class I18nService {
	private final CodeRepository codeRepository;
	private final UiLanguageRepository uiLanguageRepository;

	private final DeepLRestApi deepLRestApi;

	/**
	 * @return: Map<' 메시지코드 ', Map < ' 언어코드 ', ' 메시지 '>>
	 */
	public Map<String, Map<String, String>> getAll() {
		Map<String, Map<String, String>> result = new HashMap<>();
		for (String lang : getI18nLanguages()) {
			uiLanguageRepository.findAllByLang(lang)
								.forEach(uiLanguage -> result.computeIfAbsent(uiLanguage.getKey(), k -> new HashMap<>())
															 .put(uiLanguage.getLang(), uiLanguage.getMessage()));

		}
		return result;
	}

	private List<String> getI18nLanguages() {
		return codeRepository.findByCategory(CodeCategory.I18N_LANGUAGE_CATEGORY)
							 .stream()
							 .map(Code::getCode)
							 .toList();
	}

	public void modify(I18nModifyRequestDto requestDto) {
		Set<String> supportLanguageSet = new HashSet<>(getI18nLanguages());

		if (!requestDto.getRemoveKeys()
					   .isEmpty()) {
			uiLanguageRepository.deleteAllByKeyIn(requestDto.getRemoveKeys());
		}

		requestDto.getNewMessages()
				  .forEach(item -> uiLanguageRepository.save(UiLanguage.create(item.getKey(),
					  item.getLang(),
					  item.getMessage())));

		for (I18nModifyRequestDto.Item item : requestDto.getUpdateMessages()) {
			uiLanguageRepository.findById(UiLanguageId.of(item.getKey(), item.getLang()))
								.ifPresentOrElse(uiLanguage -> uiLanguage.modifyMessage(item.getMessage()), () -> {
									if (supportLanguageSet.contains(item.getLang())) {
										uiLanguageRepository.save(UiLanguage.create(item.getKey(),
											item.getLang(),
											item.getMessage()));
									}
								});
		}
	}

	public TranslationResponseDto getTranslation(
		String targetLang,
		List<String> text
	) {
		return deepLRestApi.translate(targetLang, text);
	}
}
