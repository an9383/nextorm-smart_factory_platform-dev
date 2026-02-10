package com.nextorm.processor;

import com.nextorm.failover.ProjectStartupEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/*
	FailOver에서 Active 상태가 되었을때는 Processor를 시작 시키는 지점이다.
 */
@Component
@RequiredArgsConstructor
public class ProcessStartEntryPoint implements ProjectStartupEntryPoint {
	private final ProcessorHandler processorHandler;
	private final ApplicationContext applicationContext;

	@Override
	public void start() {
		ApplicationArguments args = applicationContext.getBean(ApplicationArguments.class);
		processorHandler.execute(args);
	}
}
