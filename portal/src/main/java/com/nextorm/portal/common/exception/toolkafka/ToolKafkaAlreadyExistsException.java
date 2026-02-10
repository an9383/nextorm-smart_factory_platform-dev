package com.nextorm.portal.common.exception.toolkafka;

import com.nextorm.portal.common.exception.BusinessException;

public class ToolKafkaAlreadyExistsException extends BusinessException {
	public ToolKafkaAlreadyExistsException() {
		super(ToolKafkaErrorCode.TOOL_KAFKA_ALREADY_EXISTS);
	}
}
