package com.nextorm.portal.common.exception.reservoircapacity;

import com.nextorm.portal.common.exception.BusinessErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReservoirCapacityErrorCode implements BusinessErrorCode {
	RESERVOIR_CAPACITY_NOT_FOUND(HttpStatus.NOT_FOUND,
		"ERROR_RESERVOIR_CAPACITY_NOT_FOUND",
		"요청한 저수량을 찾을 수 없습니다."), RESERVOIR_CAPACITY_DUPLICATION(HttpStatus.CONFLICT,
		"ERROR_RESERVOIR_CAPACITY_DUPLICATION",
		"이미 입력된 날짜의 저수량 입니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
