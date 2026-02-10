package com.nextorm.extensions.scheduler.task.modbus;

import com.ghgande.j2mod.modbus.ModbusException;
import com.nextorm.extensions.scheduler.task.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * Modbus Coil Pulse Task
 * <p>
 * 이 클래스는 Modbus 프로토콜을 사용하여 지정된 호스트와 포트에 연결하고,
 * 주어진 주소 목록에 대해 coil을 ON, OFF 요청을 연속으로 보내는 작업을 수행합니다.
 * <p>
 * Pulse: "펄스"는 짧은 시간 동안 신호를 ON했다가 다시 OFF하는 동작을 의미합니다. (LLM 참조)
 * </p>
 */
@Slf4j
@RequiredArgsConstructor
public class ModbusCoilPulseTask implements Task {
	private final ModbusCoilPulseProperties properties;
	private final ModbusConnectionFactory connectionFactory;

	@Override
	public void execute() {
		ModbusConnection connection = null;
		try {
			// Modbus TCP 연결 설정
			connection = connectionFactory.createConnection(properties.getHost(), properties.getPort());
			connection.connect();

			// 각 주소에 대해 coil을 활성화
			for (Integer address : properties.getAddresses()) {
				log.debug("코일 펄스 at address: {} on host: {}, port: {}, unitId: {}",
					address,
					properties.getHost(),
					properties.getPort(),
					properties.getUnitId());

				pulse(connection, address);
			}

		} catch (ModbusException | IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
	}

	private void pulse(
		ModbusConnection connection,
		int address
	) throws ModbusException {
		// 펄스 동작: Coil을 ON, OFF로 순차적으로 요청
		connection.executeCoilWrite(address, true, properties.getUnitId());
		connection.executeCoilWrite(address, false, properties.getUnitId());
	}
}
