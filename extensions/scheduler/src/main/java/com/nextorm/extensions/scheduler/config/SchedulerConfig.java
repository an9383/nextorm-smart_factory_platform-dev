package com.nextorm.extensions.scheduler.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class SchedulerConfig {

	@Bean
	public TaskScheduler taskScheduler(SchedulerProperties schedulerProperties) {
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(schedulerProperties.getPoolSize());
		scheduler.setThreadNamePrefix(schedulerProperties.getThreadNamePrefix());
		scheduler.initialize();
		return scheduler;
	}
	
}
