package com.nextorm.failover.executor;

import com.nextorm.failover.handler.FailoverEventHandler;

public class NonFailoverExecutor implements FailoverExecuter {
	private FailoverEventHandler eventHandler;

	public NonFailoverExecutor(FailoverEventHandler eventHandler) {
		this.eventHandler = eventHandler;
	}

	@Override
	public void start() {
		eventHandler.onStartAsActive();
	}
}
