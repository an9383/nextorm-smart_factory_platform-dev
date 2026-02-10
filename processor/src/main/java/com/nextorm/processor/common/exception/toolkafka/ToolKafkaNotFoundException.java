package com.nextorm.processor.common.exception.toolkafka;

import com.nextorm.processor.common.BusinessException;

public class ToolKafkaNotFoundException extends BusinessException {
	public ToolKafkaNotFoundException() {
		super(ToolKafkaErrorCode.TOOL_KAFKA_NOT_FOUND);
	}

}
