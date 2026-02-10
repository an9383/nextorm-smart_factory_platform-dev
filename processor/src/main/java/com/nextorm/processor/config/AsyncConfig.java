package com.nextorm.processor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncConfig {
	public static final String OCAP_EXECUTOR_BEAN_NAME = "ocapExecutor";

	@Bean(name = OCAP_EXECUTOR_BEAN_NAME)
	public ThreadPoolTaskExecutor ocapExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(1);
		executor.setMaxPoolSize(1);
		executor.setQueueCapacity(Integer.MAX_VALUE);
		executor.setThreadNamePrefix("OCAP_EXECUTOR-");
		executor.initialize();
		return executor;
	}
}
