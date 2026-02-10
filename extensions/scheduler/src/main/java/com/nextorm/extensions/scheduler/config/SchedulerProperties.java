package com.nextorm.extensions.scheduler.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "scheduler.config")
public class SchedulerProperties {
	private final int poolSize;
	private final String threadNamePrefix;
}
