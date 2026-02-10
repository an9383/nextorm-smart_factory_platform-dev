package com.nextorm.collector.collector.opcuacollector.connectivity;

import com.nextorm.collector.collector.opcuacollector.OpcUaCollectorHandler;

public interface OpcUaMessageRecvHandler {
	void onMessage(
		DEFINE_OPCUA_COMMAND command,
		String datas,
		String strToolIp,
		String strToolId
	);

	void onDisconnected(
		String ip,
		int port,
		String toolName
	);

	void onConnected(
		OpcUaCollectorHandler handler,
		String hostAddress,
		int port,
		String toolName
	);

	boolean isTryConnection(String toolName);

	void putOpcConnectionOn(String toolName);

	void putOpcConnectionOff(String toolName);

	boolean isOpcDisConnection(String toolName);
}
