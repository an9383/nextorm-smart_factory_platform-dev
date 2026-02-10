package com.nextorm.collector.common.exception.toolkafka;

import com.nextorm.collector.common.BusinessException;

public class ToolKafkaNotFoundException extends BusinessException {
	public ToolKafkaNotFoundException() {super(ToolKafkaErrorCode.TOOL_KAFKA_NOT_FOUND);}

}
