package com.nextorm.portal.common.exception.toolkafka;

import com.nextorm.portal.common.exception.BusinessException;

public class ToolKafkaNotFoundException extends BusinessException {
	public ToolKafkaNotFoundException() {
		super(ToolKafkaErrorCode.TOOL_KAFKA_NOT_FOUND);
	}
}
