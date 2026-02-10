package com.nextorm.portal.common.exception.toolkafka;

import com.nextorm.portal.common.exception.BusinessErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ToolKafkaErrorCode implements BusinessErrorCode {
	TOOL_KAFKA_NOT_FOUND(HttpStatus.NOT_FOUND,
		"ERROR_TOOL_KAFKA_NOT_FOUND",
		"요청하신 설비의 카프카 설정을 찾을 수 없습니다"), TOOL_KAFKA_ALREADY_EXISTS(HttpStatus.CONFLICT,
		"ERROR_TOOL_KAFKA_ALREADY_EXSITS",
		"이미 존재하는 카프카 설정 입니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
