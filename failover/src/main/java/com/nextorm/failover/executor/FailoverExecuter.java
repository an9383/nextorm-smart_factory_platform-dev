package com.nextorm.failover.executor;

public interface FailoverExecuter {

	/**
	 * Framework의 Clustering 동작을 시작합니다.
	 * Active / Standby Mode에 대해서는 Event로 처리됩니다.
	 */
	void start();
}
