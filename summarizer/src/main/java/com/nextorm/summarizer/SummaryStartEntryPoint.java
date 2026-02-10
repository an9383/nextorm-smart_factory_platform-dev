package com.nextorm.summarizer;

import com.nextorm.failover.ProjectStartupEntryPoint;
import com.nextorm.failover.config.FailoverConfig;
import com.nextorm.summarizer.service.summaryprocessor.MissedSummaryProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/*
	FailOver에서 Active 상태가 되었을때는 Summarizer를 시작 시키는 지점이다.
 */
@Component
@RequiredArgsConstructor
public class SummaryStartEntryPoint implements ProjectStartupEntryPoint {
	private final FailoverConfig failoverConfig;
	private final SummaryTask summaryTask;
	private final ApplicationContext applicationContext;

	private final MissedSummaryProcessor missedSummaryProcessor;

	@Override
	public void start() {
		ApplicationArguments args = applicationContext.getBean(ApplicationArguments.class);
		if (failoverConfig.isUse()) {
			//failover시 누락된 서머리가 있는지 확인하여 누락된 서머리를 서머리
			missedSummaryProcessor.initilaizeMissedSummaryProcessor(args);
		}

		summaryTask.initializeSummaryTask(args);
		summaryTask.enableScheduler();
	}
}
