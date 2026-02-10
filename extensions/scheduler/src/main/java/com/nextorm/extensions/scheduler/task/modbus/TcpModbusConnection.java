package com.nextorm.extensions.scheduler.task.modbus;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransaction;
import com.ghgande.j2mod.modbus.msg.WriteCoilRequest;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.net.InetAddress;

@RequiredArgsConstructor
public class TcpModbusConnection implements ModbusConnection {
	private final String host;
	private final int port;

	private TCPMasterConnection connection;

	@Override
	public void connect() throws IOException {
		try {
			connection = new TCPMasterConnection(InetAddress.getByName(host));
			connection.setPort(port);
			connection.connect();
		} catch (Exception e) {
			String info = String.format("(ip: %s, port: %d)", host, port);
			throw new IOException("Modbus TCP server 연결 실패 " + info, e);
		}
	}

	@Override
	public void executeCoilWrite(
		int address,
		boolean value,
		int unitId
	) throws ModbusException {
		WriteCoilRequest request = new WriteCoilRequest(address, value);
		request.setUnitID(unitId);

		ModbusTCPTransaction transaction = new ModbusTCPTransaction(connection);
		transaction.setRequest(request);
		transaction.execute();
	}

	@Override
	public void close() {
		if (connection != null && connection.isConnected()) {
			connection.close();
		}
	}

	@Override
	public boolean isConnected() {
		return connection != null && connection.isConnected();
	}
}
