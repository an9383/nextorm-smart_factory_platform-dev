package com.nextorm.portal.common.exception.dcpconfig;

import com.nextorm.portal.common.exception.BusinessErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DcpConfigErrorCode implements BusinessErrorCode {

	DCP_CONFIG_NOT_FOUND(HttpStatus.NOT_FOUND,
		"ERROR_DCP_CONFIG_NOT_FOUND",
		"해당 DCP 설정을 찾지 못하였습니다."), RELATE_DCP_CONFIG_NOT_FOUND(HttpStatus.UNPROCESSABLE_ENTITY,
		"ERROR_RELATE_DCP_CONFIG_NOT_FOUND",
		"DCP 설정을 찾지 못하였습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
