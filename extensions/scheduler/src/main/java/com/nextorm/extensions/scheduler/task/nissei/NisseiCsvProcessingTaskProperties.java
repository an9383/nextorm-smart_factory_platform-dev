package com.nextorm.extensions.scheduler.task.nissei;

import lombok.Getter;

import java.util.Map;

@Getter
public class NisseiCsvProcessingTaskProperties {
	private final String csvFilePath;
	private final String toolName;

	public NisseiCsvProcessingTaskProperties(Map<String, Object> properties) {
		this.csvFilePath = properties.get("csvFilePath")
									 .toString();
		this.toolName = properties.get("toolName")
								  .toString();
	}
}
