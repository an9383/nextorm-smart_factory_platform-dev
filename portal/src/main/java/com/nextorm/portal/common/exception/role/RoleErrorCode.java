package com.nextorm.portal.common.exception.role;

import com.nextorm.portal.common.exception.BusinessErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RoleErrorCode implements BusinessErrorCode {
	ROLE_NOT_FOUND_EXCEPTION(HttpStatus.NOT_FOUND,
		"ERROR_ROLE_NOT_FOUND",
		"해당 Role을 찾을 수 없습니다."), ROLE_NAME_DUPLICATION(HttpStatus.CONFLICT,
		"ERROR_ROLE_NAME_DUPLICATION",
		"이름이 중복됩니다. 중복된 이름: {roleName}"), ROLE_IN_USE(HttpStatus.CONFLICT,
		"ERROR_ROLE_IN_USE",
		"권한이 사용 중 입니다. 권한 해제 후 삭제해주세요.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
