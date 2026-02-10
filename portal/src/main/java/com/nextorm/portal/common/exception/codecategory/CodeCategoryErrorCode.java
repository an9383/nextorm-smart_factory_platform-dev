package com.nextorm.portal.common.exception.codecategory;

import com.nextorm.portal.common.exception.BusinessErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CodeCategoryErrorCode implements BusinessErrorCode {
	CODE_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND,
		"ERROR_CODE_CATEGORY_NOT_FOUND",
		"코드 카테고리 정보를 찾을 수 없습니다."), CODE_CATEGORY_DUPLICATION(HttpStatus.CONFLICT,
		"ERROR_CODE_CATEGORY_DUPLICATION",
		"카테고리가 중복됩니다. 중복된 카테고리: {codeCategory}"), RELATE_CODE_CATEGORY_NOT_FOUND(HttpStatus.UNPROCESSABLE_ENTITY,
		"ERROR_RELATE_CODE_CATEGORY_NOT_FOUND",
		"코드 카테고리 정보를 찾을 수 없습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
