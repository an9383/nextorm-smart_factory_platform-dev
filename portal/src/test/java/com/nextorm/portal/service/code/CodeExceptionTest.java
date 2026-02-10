package com.nextorm.portal.service.code;

import com.nextorm.common.db.entity.system.code.CodeCategory;
import com.nextorm.common.db.repository.system.code.CodeCategoryRepository;
import com.nextorm.common.db.repository.system.code.CodeRepository;
import com.nextorm.portal.common.exception.code.CodeErrorCode;
import com.nextorm.portal.common.exception.code.CodeNotFoundException;
import com.nextorm.portal.common.exception.codecategory.CodeCategoryDuplicationException;
import com.nextorm.portal.common.exception.codecategory.CodeCategoryErrorCode;
import com.nextorm.portal.common.exception.codecategory.CodeCategoryNotFoundException;
import com.nextorm.portal.common.exception.codecategory.RelateCodeCategoryNotFoundException;
import com.nextorm.portal.dto.system.CodeCategoryCreateRequestDto;
import com.nextorm.portal.dto.system.CodeCategoryUpdateRequestDto;
import com.nextorm.portal.dto.system.CodeCreateRequestDto;
import com.nextorm.portal.dto.system.CodeUpdateRequestDto;
import com.nextorm.portal.service.system.CodeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CodeExceptionTest {
	@InjectMocks
	CodeService codeService;

	@Mock
	CodeRepository codeRepository;

	@Mock
	CodeCategoryRepository codeCategoryRepository;

	@DisplayName("getCode 요청 시 잘못된 codeId를 요청했을 경우: CodeNotFoundException")
	@Test
	void givenCodeIdThenCodeNotFoundExceptionWhenGetCode() {
		assertThatThrownBy(() -> codeService.getCode(10000000L)).isInstanceOf(CodeNotFoundException.class)
																.hasMessage(CodeErrorCode.CODE_NOT_FOUND.getMessage());

	}

	@DisplayName("getCodeCategory 요청 시 잘못된 codeCategoryId를 요청했을 경우: CodeCategoryNotFoundException")
	@Test
	void givenCodeCategoryIdThenCodeCategoryNotFoundExceptionWhenGetCodeCategory() {
		assertThatThrownBy(() -> codeService.getCodeCategory(10000000L)).isInstanceOf(CodeCategoryNotFoundException.class)
																		.hasMessage(CodeCategoryErrorCode.CODE_CATEGORY_NOT_FOUND.getMessage());

	}

	@DisplayName("createCategory 요청 시 codeCategory의 Category가 중복된 경우: CodeCategoryDuplicationException")
	@Test
	void givenCodeCategoryThenCodeCategoryDuplicationExceptionWhenCreateCategory() {

		CodeCategory codeCategory = CodeCategory.builder()
												.category("test")
												.name("test")
												.build();

		when(codeCategoryRepository.findByCategory(codeCategory.getCategory())).thenReturn(Optional.of(codeCategory));

		CodeCategoryCreateRequestDto request = new CodeCategoryCreateRequestDto("test", "test", "test");

		assertThatThrownBy(() -> codeService.createCategory(request)).isInstanceOf(CodeCategoryDuplicationException.class)
																	 .hasMessage(CodeCategoryErrorCode.CODE_CATEGORY_DUPLICATION.getMessage());
	}

	@DisplayName("updateCategory 요청 시 잘못된 CodeCategoryId를 요청했을 경우: CodeCategoryNotFoundException")
	@Test
	void givenCodeCategoryIdThenCodeCategoryNotFoundExceptionWhenUpdateCategory() {
		CodeCategoryUpdateRequestDto request = new CodeCategoryUpdateRequestDto("test", "test");

		assertThatThrownBy(() -> codeService.updateCategory(111111L, request)).isInstanceOf(
																				  CodeCategoryNotFoundException.class)
																			  .hasMessage(CodeCategoryErrorCode.CODE_CATEGORY_NOT_FOUND.getMessage());
	}

	@DisplayName("deleteCategory 요청 시 잘못된 CodeCategoryId를 요청했을 경우: CodeCategoryNotFoundException")
	@Test
	void givenCodeCategoryIdThenCodeCategoryNotFoundExceptionWhenDeleteCategory() {
		assertThatThrownBy(() -> codeService.deleteCategory(11111111L)).isInstanceOf(CodeCategoryNotFoundException.class)
																	   .hasMessage(CodeCategoryErrorCode.CODE_CATEGORY_NOT_FOUND.getMessage());
	}

	@DisplayName("createCode 요청 시 잘못된 CategoryId를 요청했을 경우: CodeCategoryNotFoundException")
	@Test
	void givenCodeCategoryIdThenCodeCategoryNotFoundExceptionWhenCreateCode() {
		CodeCreateRequestDto request = new CodeCreateRequestDto(1111L,
			"test",
			"test",
			"test",
			"test",
			1111L,
			List.of());

		assertThatThrownBy(() -> codeService.createCode(request)).isInstanceOf(RelateCodeCategoryNotFoundException.class)
																 .hasMessage(CodeCategoryErrorCode.RELATE_CODE_CATEGORY_NOT_FOUND.getMessage());
	}

	@DisplayName("updateCode 요청 시 잘못된 CodeId를 요청했을 경우: CodeNotFoundException")
	@Test
	void givenCodeIdThenCodeNotFoundExceptionWhenUpdateCode() {
		CodeUpdateRequestDto request = new CodeUpdateRequestDto("test", "test", "test", "test", 111111L, List.of());

		assertThatThrownBy(() -> codeService.updateCode(111111L, request)).isInstanceOf(CodeNotFoundException.class)
																		  .hasMessage(CodeErrorCode.CODE_NOT_FOUND.getMessage());
	}

	@DisplayName("deleteCode 요청 시 잘못된 codeId를 요청했을 경우: CodeNotFoundException")
	@Test
	void givenCodeIdThenCodeNotFoundExceptionWhenDeleteCode() {
		assertThatThrownBy(() -> codeService.deleteCode(11111L)).isInstanceOf(CodeNotFoundException.class)
																.hasMessage(CodeErrorCode.CODE_NOT_FOUND.getMessage());
	}
}
