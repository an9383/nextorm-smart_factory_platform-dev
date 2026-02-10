package com.nextorm.extensions.scheduler.task.modbus;

import java.io.IOException;

public class TcpModbusConnectionFactory implements ModbusConnectionFactory {
	@Override
	public ModbusConnection createConnection(
		String host,
		int port
	) throws IOException {
		return new TcpModbusConnection(host, port);
	}
}
