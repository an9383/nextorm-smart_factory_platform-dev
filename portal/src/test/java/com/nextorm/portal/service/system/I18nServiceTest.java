package com.nextorm.portal.service.system;

import com.nextorm.common.db.entity.UiLanguage;
import com.nextorm.common.db.entity.system.code.Code;
import com.nextorm.common.db.repository.UiLanguageRepository;
import com.nextorm.common.db.repository.system.code.CodeRepository;
import com.nextorm.portal.constant.CodeCategory;
import com.nextorm.portal.dto.system.I18nModifyRequestDto;
import com.nextorm.portal.restapi.DeepLRestApi;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@DisplayName("다국어 서비스 단위 테스트")
@Slf4j
class I18nServiceTest {
	private final CodeRepository codeRepository = Mockito.mock(CodeRepository.class);
	private final UiLanguageRepository uiLanguageRepository = Mockito.mock(UiLanguageRepository.class);
	private final DeepLRestApi deepLRestApi = Mockito.mock(DeepLRestApi.class);

	@DisplayName("모든 메시지를 조회")
	@Test
	void get_all() {
		// given
		String koCode = Locale.KOREAN.toString();
		String enCode = Locale.ENGLISH.toString();

		List<Code> mockCodes = List.of(Code.builder()
										   .code(koCode)
										   .build(),
			Code.builder()
				.code(enCode)
				.build());

		List<UiLanguage> koLangMock = List.of(UiLanguage.builder()
														.key("key")
														.lang(koCode)
														.message("메시지")
														.build(),
			UiLanguage.builder()
					  .key("key2")
					  .lang(koCode)
					  .message("메시지2")
					  .build());

		List<UiLanguage> enLangMock = List.of(UiLanguage.builder()
														.key("key")
														.lang(enCode)
														.message("message")
														.build(),
			UiLanguage.builder()
					  .key("key2")
					  .lang(enCode)
					  .message("message2")
					  .build());

		given(codeRepository.findByCategory(CodeCategory.I18N_LANGUAGE_CATEGORY)).willReturn(mockCodes);
		given(uiLanguageRepository.findAllByLang(koCode)).willReturn(koLangMock);
		given(uiLanguageRepository.findAllByLang(enCode)).willReturn(enLangMock);

		I18nService i18nService = new I18nService(codeRepository, uiLanguageRepository, deepLRestApi);

		// when
		Map<String, Map<String, String>> result = i18nService.getAll();

		// then
		then(codeRepository).should(times(1))
							.findByCategory(CodeCategory.I18N_LANGUAGE_CATEGORY);
		then(uiLanguageRepository).should(times(1))
								  .findAllByLang(koCode);
		then(uiLanguageRepository).should(times(1))
								  .findAllByLang(enCode);

		assertThat(result).hasSize(2);

		assertThat(result.get("key")).hasSize(2)
									 .containsAllEntriesOf(Map.of(koCode, "메시지", enCode, "message"));

		assertThat(result.get("key2")).hasSize(2)
									  .containsAllEntriesOf(Map.of(koCode, "메시지2", enCode, "message2"));
	}

	@DisplayName("다국어 수정할 때")
	@Nested
	class ModifyTest {
		@DisplayName("수정 메시지 데이터가 있다면 해당 메시지 수정")
		@Test
		void if_updateMessages_exists_then_modify() {
			// given
			I18nModifyRequestDto requestDto = new I18nModifyRequestDto();
			requestDto.getUpdateMessages()
					  .add(new I18nModifyRequestDto.Item());

			I18nService i18nService = new I18nService(codeRepository, uiLanguageRepository, deepLRestApi);

			// when
			i18nService.modify(requestDto);

			// then
			then(uiLanguageRepository).should(times(1))
									  .findById(any());
		}

		@DisplayName("수정 메시지 데이터가 있으나, DB에 해당 값이 없는 경우 신규로 생성함")
		@Test
		void if_updateMessages_exists_but_not_in_db_then_create_new() {
			// given
			I18nModifyRequestDto requestDto = new I18nModifyRequestDto();
			I18nModifyRequestDto.Item item = new I18nModifyRequestDto.Item();
			item.setKey("key");
			item.setLang("ko");
			item.setMessage("메시지");
			requestDto.getUpdateMessages()
					  .add(item);

			List<Code> mockCodes = List.of(Code.builder()
											   .code("ko")
											   .build());
			given(codeRepository.findByCategory(CodeCategory.I18N_LANGUAGE_CATEGORY)).willReturn(mockCodes);
			given(uiLanguageRepository.findById(any())).willReturn(Optional.empty(), any());

			I18nService i18nService = new I18nService(codeRepository, uiLanguageRepository, deepLRestApi);

			// when
			i18nService.modify(requestDto);

			// then
			then(uiLanguageRepository).should(times(1))
									  .save(any());
		}

		@DisplayName("추가 메시지 데이터가 있으면 메시지 추가")
		@Test
		void if_newMessages_exists_then_save() {
			// given
			I18nModifyRequestDto requestDto = new I18nModifyRequestDto();
			requestDto.getNewMessages()
					  .add(new I18nModifyRequestDto.Item());

			I18nService i18nService = new I18nService(codeRepository, uiLanguageRepository, deepLRestApi);

			// when
			i18nService.modify(requestDto);

			// then
			then(uiLanguageRepository).should(times(1))
									  .save(any());
		}

		@DisplayName("삭제 키 데이터가 있다면 해당 메시지 삭제")
		@Test
		void if_removeKey_exists_then_delete() {
			// given
			List<String> removeKeys = List.of("removeKey");
			I18nModifyRequestDto requestDto = new I18nModifyRequestDto();
			requestDto.setRemoveKeys(removeKeys);

			I18nService i18nService = new I18nService(codeRepository, uiLanguageRepository, deepLRestApi);

			// when
			i18nService.modify(requestDto);

			// then
			then(uiLanguageRepository).should(times(1))
									  .deleteAllByKeyIn(removeKeys);
		}

		@DisplayName("DTO에 아무런 정보도 없다면 아무일도 일어나지 않음")
		@Test
		void if_requestDto_is_empty_then_do_nothing() {
			// given
			I18nModifyRequestDto requestDto = new I18nModifyRequestDto();

			I18nService i18nService = new I18nService(codeRepository, uiLanguageRepository, deepLRestApi);

			// when
			i18nService.modify(requestDto);

			// then
			then(uiLanguageRepository).should(never())
									  .deleteAllByKeyIn(any());
			then(uiLanguageRepository).should(never())
									  .save(any());
			then(uiLanguageRepository).should(never())
									  .findById(any());
		}
	}
}