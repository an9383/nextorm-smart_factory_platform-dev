package com.nextorm.processor.service;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ProcessingToolInfo {
	private final Long toolId;
	private final String toolName;
	private final String bootstrapServers;
	private final String topic;
	private final List<Long> dcpIds;
}
