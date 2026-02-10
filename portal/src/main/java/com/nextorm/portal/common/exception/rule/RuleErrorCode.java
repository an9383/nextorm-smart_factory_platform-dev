package com.nextorm.portal.common.exception.rule;

import com.nextorm.portal.common.exception.BusinessErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RuleErrorCode implements BusinessErrorCode {
	RULE_NAME_DUPLICATION(HttpStatus.CONFLICT,
		"ERROR_RULE_NAME_DUPLICATION",
		"이름이 중복됩니다. 중복되는 이름: {ruleName}"), RULE_NOT_FOUND_EXCEPTION(HttpStatus.NOT_FOUND,
		"ERROR_RULE_NOT_FOUND",
		"규칙을 찾을 수 없습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
