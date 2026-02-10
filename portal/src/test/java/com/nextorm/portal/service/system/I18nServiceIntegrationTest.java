package com.nextorm.portal.service.system;

import com.nextorm.common.db.entity.UiLanguage;
import com.nextorm.common.db.entity.system.code.Code;
import com.nextorm.common.db.entity.system.code.CodeCategory;
import com.nextorm.common.db.repository.UiLanguageRepository;
import com.nextorm.common.db.repository.system.code.CodeCategoryRepository;
import com.nextorm.common.db.repository.system.code.CodeRepository;
import com.nextorm.portal.DataJpaTestBase;
import com.nextorm.portal.dto.system.I18nModifyRequestDto;
import com.nextorm.portal.restapi.DeepLRestApi;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static com.nextorm.portal.constant.CodeCategory.I18N_LANGUAGE_CATEGORY;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("다국어 서비스 통합 테스트")
@Slf4j
class I18nServiceIntegrationTest extends DataJpaTestBase {
	@Autowired
	private CodeCategoryRepository codeCategoryRepository;

	@Autowired
	private CodeRepository codeRepository;

	@Autowired
	private DeepLRestApi deepLRestApi;

	@Autowired
	private UiLanguageRepository uiLanguageRepository;

	private static final String koCode = Locale.KOREAN.toString();
	private static final String enCode = Locale.ENGLISH.toString();

	@BeforeEach
	void beforeSetup() {
		CodeCategory codeCategory = codeCategoryRepository.save(CodeCategory.builder()
																			.category(I18N_LANGUAGE_CATEGORY)
																			.name("다국어")
																			.build());

		List<Code> codes = List.of(Code.builder()
									   .category(codeCategory)
									   .code(koCode)
									   .build(),
			Code.builder()
				.category(codeCategory)
				.code(enCode)
				.build());

		codeRepository.saveAll(codes);
	}

	@DisplayName("모든 메시지를 조회")
	@Test
	void get_all() {
		// given
		I18nService i18nService = new I18nService(codeRepository, uiLanguageRepository, deepLRestApi);

		List<UiLanguage> koMessages = koMessages();
		List<UiLanguage> enMessages = enMessages();

		uiLanguageRepository.saveAll(koMessages);
		uiLanguageRepository.saveAll(enMessages);

		// when
		Map<String, Map<String, String>> result = i18nService.getAll();

		// then
		assertThat(result).hasSize(2);

		assertThat(result.get("key")).hasSize(2)
									 .containsAllEntriesOf(Map.of(koCode, "메시지", enCode, "message"));

		assertThat(result.get("key2")).hasSize(2)
									  .containsAllEntriesOf(Map.of(koCode, "메시지2", enCode, "message2"));
	}

	private List<UiLanguage> koMessages() {
		return List.of(UiLanguage.create("key", koCode, "메시지"), UiLanguage.create("key2", koCode, "메시지2"));
	}

	private List<UiLanguage> enMessages() {
		return List.of(UiLanguage.create("key", enCode, "message"), UiLanguage.create("key2", enCode, "message2"));
	}

	@DisplayName("다국어 수정할 때")
	@Nested
	class ModifyTest {
		@DisplayName("추가 메시지 데이터가 있으면 메시지 추가")
		@Test
		void if_newMessages_exists_then_save() {
			// given
			I18nModifyRequestDto.Item newMessage = new I18nModifyRequestDto.Item();
			newMessage.setKey("newKey");
			newMessage.setLang(koCode);
			newMessage.setMessage("새로운 메시지");

			I18nModifyRequestDto requestDto = new I18nModifyRequestDto();
			requestDto.getNewMessages()
					  .add(newMessage);

			I18nService i18nService = new I18nService(codeRepository, uiLanguageRepository, deepLRestApi);

			// when
			i18nService.modify(requestDto);

			// then
			List<UiLanguage> expectMessages = uiLanguageRepository.findAllByLang(koCode);
			assertThat(expectMessages).hasSize(1);

			UiLanguage expectMessage = expectMessages.get(0);
			assertThat(expectMessage.getKey()).isEqualTo(newMessage.getKey());
			assertThat(expectMessage.getLang()).isEqualTo(newMessage.getLang());
			assertThat(expectMessage.getMessage()).isEqualTo(newMessage.getMessage());
		}

		@DisplayName("수정 메시지 데이터가 있으나, DB에 해당 값이 없는 경우 신규로 생성함")
		@Test
		void if_updateMessages_exists_but_not_in_db_then_create_new() {
			// given
			I18nModifyRequestDto requestDto = new I18nModifyRequestDto();
			I18nModifyRequestDto.Item item = new I18nModifyRequestDto.Item();
			item.setKey("key");
			item.setLang(koCode);
			item.setMessage("메시지");
			requestDto.getUpdateMessages()
					  .add(item);

			I18nService i18nService = new I18nService(codeRepository, uiLanguageRepository, deepLRestApi);

			// when
			i18nService.modify(requestDto);

			// then
			List<UiLanguage> expectMessages = uiLanguageRepository.findAllByLang(koCode);
			assertThat(expectMessages).hasSize(1);
			assertThat(expectMessages.get(0)
									 .getMessage()).isEqualTo(item.getMessage());
		}

		@DisplayName("수정 메시지 데이터가 있다면 해당 메시지 수정")
		@Test
		void if_updateMessages_exists_then_modify() {
			// given
			UiLanguage firstMessage = koMessages().get(0);
			uiLanguageRepository.save(firstMessage);

			I18nModifyRequestDto.Item updateMessage = new I18nModifyRequestDto.Item();
			updateMessage.setKey(firstMessage.getKey());
			updateMessage.setLang(firstMessage.getLang());
			updateMessage.setMessage(firstMessage.getMessage() + " 수정");

			I18nModifyRequestDto requestDto = new I18nModifyRequestDto();
			requestDto.getUpdateMessages()
					  .add(updateMessage);

			I18nService i18nService = new I18nService(codeRepository, uiLanguageRepository, deepLRestApi);

			// when
			i18nService.modify(requestDto);

			// then
			List<UiLanguage> expectMessages = uiLanguageRepository.findAllByLang(koCode);
			assertThat(expectMessages).hasSize(1);
			assertThat(expectMessages.get(0)
									 .getMessage()).isEqualTo(updateMessage.getMessage());
		}

		@DisplayName("삭제 키 데이터가 있다면 해당 메시지 삭제")
		@Test
		void if_removeKey_exists_then_delete() {
			// given
			I18nService i18nService = new I18nService(codeRepository, uiLanguageRepository, deepLRestApi);

			List<UiLanguage> givenMessages = koMessages();
			uiLanguageRepository.saveAll(givenMessages);

			UiLanguage firstMessage = givenMessages.get(0);

			List<String> removeKeys = List.of(firstMessage.getKey());
			I18nModifyRequestDto requestDto = new I18nModifyRequestDto();
			requestDto.setRemoveKeys(removeKeys);

			// when
			i18nService.modify(requestDto);

			// then
			List<UiLanguage> expectMessages = uiLanguageRepository.findAllByLang(koCode);
			Optional<UiLanguage> foundRemoveKeyMessage = expectMessages.stream()
																	   .filter(it -> it.getKey()
																					   .equals(firstMessage.getKey()))
																	   .findFirst();

			assertThat(expectMessages).hasSize(1);
			assertThat(foundRemoveKeyMessage).isNotPresent();
		}

		@DisplayName("DTO에 아무런 정보도 없다면 아무일도 일어나지 않음")
		@Test
		void if_requestDto_is_empty_then_do_nothing() {
			// given
			List<UiLanguage> koMessages = koMessages();
			uiLanguageRepository.saveAll(koMessages);

			I18nModifyRequestDto requestDto = new I18nModifyRequestDto();
			I18nService i18nService = new I18nService(codeRepository, uiLanguageRepository, deepLRestApi);

			// when
			i18nService.modify(requestDto);

			// then
			List<String> gaveMessages = koMessages.stream()
												  .map(UiLanguage::getMessage)
												  .toList();
			List<UiLanguage> expectMessages = uiLanguageRepository.findAll();
			assertThat(expectMessages).hasSize(2);
			assertThat(expectMessages).filteredOn(it -> gaveMessages.contains(it.getMessage()))
									  .hasSize(2);
		}
	}
}
