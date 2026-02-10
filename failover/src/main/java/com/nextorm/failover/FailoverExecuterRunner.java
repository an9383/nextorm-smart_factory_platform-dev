package com.nextorm.failover;

import com.nextorm.failover.executor.FailoverExecuter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FailoverExecuterRunner implements ApplicationListener<ApplicationReadyEvent> {
	private final FailoverExecuter executer;

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		executer.start();
	}
}
