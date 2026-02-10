package com.nextorm.extensions.scheduler.task.modbus;

import java.io.IOException;

public interface ModbusConnectionFactory {
	ModbusConnection createConnection(
		String host,
		int port
	) throws IOException;
}
