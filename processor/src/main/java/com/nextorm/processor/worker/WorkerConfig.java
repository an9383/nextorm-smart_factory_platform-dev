package com.nextorm.processor.worker;

import lombok.Value;

@Value
public class WorkerConfig {
	private final Long toolId;
	private final String topic;
	private final String bootstrapServers;

	public static WorkerConfig of(
		Long toolId,
		String topic,
		String bootstrapServers
	) {
		return new WorkerConfig(toolId, topic, bootstrapServers);
	}
}
