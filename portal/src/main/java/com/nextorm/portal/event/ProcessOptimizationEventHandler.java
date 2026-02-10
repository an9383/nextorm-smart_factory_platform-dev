package com.nextorm.portal.event;

import com.nextorm.portal.client.processoptimization.ProcessOptimizationClient;
import com.nextorm.portal.event.message.ProcessOptimizationRequestEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessOptimizationEventHandler {
	private final ProcessOptimizationClient client;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleParameterEvent(ProcessOptimizationRequestEvent event) {
		client.requestProcessOptimization(event.getOptimizationId());
	}
}
