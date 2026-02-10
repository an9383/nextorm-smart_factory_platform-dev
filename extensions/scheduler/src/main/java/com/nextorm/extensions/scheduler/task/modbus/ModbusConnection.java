package com.nextorm.extensions.scheduler.task.modbus;

import com.ghgande.j2mod.modbus.ModbusException;

import java.io.IOException;

public interface ModbusConnection {
	void connect() throws IOException;

	void executeCoilWrite(
		int address,
		boolean value,
		int unitId
	) throws ModbusException;

	void close();

	boolean isConnected();
}
