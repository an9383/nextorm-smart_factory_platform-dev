package com.nextorm.collector.common.exception.toolkafka;

import com.nextorm.collector.common.BusinessErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
@Getter
@RequiredArgsConstructor
public enum ToolKafkaErrorCode implements BusinessErrorCode {
	TOOL_KAFKA_NOT_FOUND(HttpStatus.NOT_FOUND,
		"ERROR_TOOL_KAFKA_NOT_FOUND",
		"요청하신 설비의 카프카 설정을 찾을 수 없습니다");


	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
