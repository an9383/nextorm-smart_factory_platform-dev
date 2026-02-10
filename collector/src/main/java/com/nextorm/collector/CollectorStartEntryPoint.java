package com.nextorm.collector;

import com.nextorm.failover.ProjectStartupEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CollectorStartEntryPoint implements ProjectStartupEntryPoint {
	private final ApplicationContext applicationContext;
	private final CollectorRunner collectorRunner;

	@Override
	public void start() {
		ApplicationArguments args = applicationContext.getBean(ApplicationArguments.class);
		collectorRunner.executeTasks();
	}
}
