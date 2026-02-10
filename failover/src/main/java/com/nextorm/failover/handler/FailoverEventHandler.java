package com.nextorm.failover.handler;

public interface FailoverEventHandler {
	/**
	 * Framework이 Standby 모드로 구동되었다가, Active Mode로 전환되었습니다.
	 */
	void onStatusChanged();

	/**
	 * Framework이 Active Mode로 Start됩니다.
	 */
	void onStartAsActive();

	/**
	 * Framework이 Standby Mode로 Start 됩니다.
	 */
	void onStartAsStandby();

	/**
	 * Failover Agent와의 연결이 종료되었습니다.
	 * 이 경우, Framework이 강제 종료될 수 있습니다.
	 */
	void onDisconnected();

	/**
	 * Agent와의 연결이 수립되었습니다.
	 */
	void onConnected();

	void onErrorOccurs(
		String p_method,
		Exception ex
	);
}
