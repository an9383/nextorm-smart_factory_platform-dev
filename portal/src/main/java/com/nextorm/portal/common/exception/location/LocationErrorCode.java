package com.nextorm.portal.common.exception.location;

import com.nextorm.portal.common.exception.BusinessErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum LocationErrorCode implements BusinessErrorCode {
	LOCATION_NOT_FOUND(HttpStatus.NOT_FOUND,
		"ERROR_LOCATION_NOT_FOUND",
		"위치 정보를 찾을 수 없습니다."), LOCATION_DUPLICATION_NAME_TYPE_PARENT(HttpStatus.CONFLICT,
		"ERROR_LOCATION_DUPLICATION_NAME_TYPE_PARENT",
		"이름이 중복 됩니다. 중복된 이름: {name}"), PARENT_LOCATION_NOT_FOUND(HttpStatus.UNPROCESSABLE_ENTITY,
		"ERROR_PARENT_LOCATION_NOT_FOUND",
		"상위 위치정보를 찾을 수 없습니다."), RELATE_LOCATION_NOT_FOUND(HttpStatus.UNPROCESSABLE_ENTITY,
		"ERROR_RELATE_LOCATION_NOT_FOUND",
		"해당 위치 정보를 찾을 수 없습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
